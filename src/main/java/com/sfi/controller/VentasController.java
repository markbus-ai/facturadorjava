package com.sfi.controller;

import com.sfi.App;
import com.sfi.model.*;
import com.sfi.service.AuthService;
import com.sfi.service.ClientService;
import com.sfi.service.InvoiceService;
import com.sfi.service.ProductService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.sfi.util.UIUtils;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.input.KeyCode;

public class VentasController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Button backButton;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private ComboBox<Client> clientCombo;
    @FXML private TableView<InvoiceItem> itemsTable;
    @FXML private TableColumn<InvoiceItem, String> colItemProduct;
    @FXML private TableColumn<InvoiceItem, Integer> colItemQty;
    @FXML private TableColumn<InvoiceItem, BigDecimal> colItemPrice;
    @FXML private TableColumn<InvoiceItem, BigDecimal> colItemSubtotal;
    @FXML private TableColumn<InvoiceItem, String> colItemAction;

    // Global discount
    @FXML private ComboBox<String> discountTypeCombo;
    @FXML private TextField discountValueField;

    // Per-item discount
    @FXML private ComboBox<String> itemDiscountTypeCombo;
    @FXML private TextField itemDiscountValueField;

    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label taxableLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private final ProductService productService = new ProductService();
    private final ClientService clientService = new ClientService();
    private final InvoiceService invoiceService = new InvoiceService();
    private final AuthService authService = App.getAuthService();

    private final ObservableList<InvoiceItem> itemsData = FXCollections.observableArrayList();
    private DescuentoStrategy currentDiscount = new SinDescuento();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Usuario: " + authService.getCurrentUser().getUsername());
        if (authService.isAdmin()) {
            backButton.setText("\u2190 Panel Admin");
        } else {
            backButton.setText("\u2190 Mis Facturas");
        }

        UIUtils.setMaxLength(quantityField, 9);
        UIUtils.setMaxLength(discountValueField, 10);
        UIUtils.setMaxLength(itemDiscountValueField, 10);

        // Enter en quantityField → agregar ítem (comportamiento esperado)
        quantityField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume(); // evita que se propague a otros botones
                handleAddItem();
            }
        });
        // Enter en campos de descuento → aplicar descuento, NO agregar ítem
        discountValueField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                handleApplyDiscount();
            }
        });
        itemDiscountValueField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                handleApplyItemDiscount();
            }
        });

        // Client combo
        clientCombo.setItems(FXCollections.observableArrayList(clientService.findAll()));
        clientCombo.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                setText(c == null ? "" : c.getName());
            }
        });
        clientCombo.setButtonCell((ListCell<Client>) clientCombo.getCellFactory().call(null));
        // Select "Consumidor Final" by default
        for (Client c : clientCombo.getItems()) {
            if ("Consumidor Final".equals(c.getName())) {
                clientCombo.getSelectionModel().select(c);
                break;
            }
        }
        if (clientCombo.getSelectionModel().isEmpty() && !clientCombo.getItems().isEmpty()) {
            clientCombo.getSelectionModel().selectFirst();
        }

        // Product combo
        productCombo.setItems(FXCollections.observableArrayList(productService.findAllActiveWithStock()));
        productCombo.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(p == null ? "" : p.getCode() + " - " + p.getName() + " ($" + p.getPrice() + ")");
            }
        });
        productCombo.setButtonCell((ListCell<Product>) productCombo.getCellFactory().call(null));

        // Items table
        colItemProduct.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProductName()));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colItemSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colItemAction.setCellValueFactory(cd -> new SimpleStringProperty("X"));

        colItemAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("X");
            {
                btn.getStyleClass().add("button-danger");
                btn.setStyle("-fx-padding: 2 8;");
                btn.setOnAction(e -> {
                    InvoiceItem item = getTableView().getItems().get(getIndex());
                    itemsData.remove(item);
                    recalcSummary();
                });
            }
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btn);
            }
        });

        itemsTable.setItems(itemsData);
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) syncItemDiscountControls(sel);
        });

        // Global discount
        discountTypeCombo.setItems(FXCollections.observableArrayList("Sin Descuento", "Porcentual (%)", "Nominal ($)"));
        discountTypeCombo.getSelectionModel().selectFirst();
        discountValueField.setDisable(true);
        discountTypeCombo.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasDiscount = newVal.intValue() > 0;
            discountValueField.setDisable(!hasDiscount);
            if (!hasDiscount) {
                discountValueField.clear();
                currentDiscount = new SinDescuento();
                recalcSummary();
            }
        });

        // Per-item discount
        itemDiscountTypeCombo.setItems(FXCollections.observableArrayList("Sin Descuento", "Porcentual (%)", "Nominal ($)"));
        itemDiscountTypeCombo.getSelectionModel().selectFirst();
        itemDiscountValueField.setDisable(true);
        itemDiscountTypeCombo.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasDiscount = newVal.intValue() > 0;
            itemDiscountValueField.setDisable(!hasDiscount);
            if (!hasDiscount) itemDiscountValueField.clear();
        });

        recalcSummary();
    }

    @FXML
    public void handleAddItem() {
        Product product = productCombo.getSelectionModel().getSelectedItem();
        if (product == null) { showAlert("Seleccione un producto"); return; }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Cantidad invalida");
            return;
        }

        long existingQty = itemsData.stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .mapToLong(InvoiceItem::getQuantity)
                .sum();
        if (existingQty + qty > product.getStock()) {
            showAlert("Stock insuficiente. Disponible: " + product.getStock() + " (Ya tienes " + existingQty + " en el carrito)");
            return;
        }

        InvoiceItem item = new InvoiceItem(product.getId(), product.getName(), qty, product.getPrice());

        // Apply per-item discount if set
        int discIdx = itemDiscountTypeCombo.getSelectionModel().getSelectedIndex();
        if (discIdx > 0) {
            String discText = itemDiscountValueField.getText().trim();
            if (discText.isEmpty()) {
                showAlert("Ingrese un valor de descuento");
                return;
            }
            try {
                BigDecimal val = new BigDecimal(discText);
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    showAlert("El descuento no puede ser negativo");
                    return;
                }
                if (discIdx == 1 && val.compareTo(new BigDecimal("100")) > 0) {
                    showAlert("El porcentaje de descuento no puede superar el 100%");
                    return;
                }

                item.setDiscountType(discIdx == 1 ? DiscountType.PERCENTAGE : DiscountType.NOMINAL);
                item.setDiscountValue(val);
            } catch (NumberFormatException e) {
                showAlert("Valor de descuento del ítem inválido");
                itemDiscountValueField.clear();
                itemDiscountTypeCombo.getSelectionModel().selectFirst();
                itemDiscountValueField.setDisable(true);
                return;
            }
        }

        itemsData.add(item);
        recalcSummary();
        quantityField.clear();
        itemDiscountTypeCombo.getSelectionModel().selectFirst();
        itemDiscountValueField.clear();
        itemDiscountValueField.setDisable(true);
    }

    @FXML
    public void handleApplyItemDiscount() {
        InvoiceItem sel = itemsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un producto en la tabla"); return; }

        int idx = itemDiscountTypeCombo.getSelectionModel().getSelectedIndex();
        String valText = itemDiscountValueField.getText().trim();

        if (idx == 0) {
            sel.setDiscountType(DiscountType.NONE);
            sel.setDiscountValue(BigDecimal.ZERO);
        } else {
            if (valText.isEmpty()) {
                showAlert("Ingrese un valor de descuento");
                return;
            }
            try {
                BigDecimal val = new BigDecimal(valText);
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    showAlert("El descuento no puede ser negativo");
                    return;
                }
                if (idx == 1 && val.compareTo(new BigDecimal("100")) > 0) {
                    showAlert("El porcentaje de descuento no puede superar el 100%");
                    return;
                }
                
                sel.setDiscountType(idx == 1 ? DiscountType.PERCENTAGE : DiscountType.NOMINAL);
                sel.setDiscountValue(val);
            } catch (NumberFormatException e) {
                showAlert("Valor de descuento inválido");
                itemDiscountValueField.clear();
                itemDiscountTypeCombo.getSelectionModel().selectFirst();
                itemDiscountValueField.setDisable(true);
                return;
            }
        }

        recalcSummary();
        itemsTable.refresh();
    }

    @FXML
    public void handleApplyDiscount() {
        int idx = discountTypeCombo.getSelectionModel().getSelectedIndex();
        String valText = discountValueField.getText().trim();

        if (idx == 0) {
            currentDiscount = new SinDescuento();
        } else {
            if (valText.isEmpty()) {
                showAlert("Ingrese un valor de descuento");
                return;
            }
            try {
                BigDecimal val = new BigDecimal(valText);
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    showAlert("El descuento no puede ser negativo");
                    return;
                }
                if (idx == 1 && val.compareTo(new BigDecimal("100")) > 0) {
                    showAlert("El porcentaje de descuento no puede superar el 100%");
                    return;
                }
                
                currentDiscount = switch (idx) {
                    case 1 -> new DescuentoPorcentual(val);
                    case 2 -> new DescuentoNominal(val);
                    default -> new SinDescuento();
                };
            } catch (NumberFormatException e) {
                showAlert("Valor de descuento inválido");
                discountValueField.clear();
                discountTypeCombo.getSelectionModel().selectFirst();
                currentDiscount = new SinDescuento();
                return;
            }
        }

        recalcSummary();
    }

    @FXML
    public void handleEmitirFactura() {
        if (itemsData.isEmpty()) {
            showAlert("Agregue al menos un producto");
            return;
        }

        Client client = clientCombo.getSelectionModel().getSelectedItem();
        Long clientId = client != null ? client.getId() : null;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Emitir factura por $" + totalLabel.getText().replace("$", "") + "?",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        try {
            Invoice invoice = invoiceService.emitirFactura(
                    authService.getCurrentUser().getId(),
                    clientId,
                    itemsData.stream().toList(),
                    currentDiscount
            );
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Factura Emitida");
            alert.setHeaderText(null);
            alert.setContentText("Factura #" + invoice.getInvoiceNumber() + "\nTotal: $" + invoice.getTotal());
            alert.showAndWait();

            itemsData.clear();
            currentDiscount = new SinDescuento();
            discountTypeCombo.getSelectionModel().selectFirst();
            discountValueField.clear();
            itemDiscountTypeCombo.getSelectionModel().selectFirst();
            itemDiscountValueField.clear();
            itemDiscountValueField.setDisable(true);
            recalcSummary();
            productCombo.setItems(FXCollections.observableArrayList(productService.findAllActiveWithStock()));
            productCombo.getSelectionModel().clearSelection();
            quantityField.clear();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleGoBack() {
        if (authService.isAdmin()) {
            App.loadAdminScene();
        } else {
            App.loadUserInvoicesScene();
        }
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        App.loadLoginScene();
    }

    private void syncItemDiscountControls(InvoiceItem item) {
        if (item.getDiscountType() == DiscountType.NONE) {
            itemDiscountTypeCombo.getSelectionModel().select(0);
            itemDiscountValueField.setDisable(true);
            itemDiscountValueField.clear();
        } else if (item.getDiscountType() == DiscountType.PERCENTAGE) {
            itemDiscountTypeCombo.getSelectionModel().select(1);
            itemDiscountValueField.setDisable(false);
            itemDiscountValueField.setText(item.getDiscountValue().toString());
        } else if (item.getDiscountType() == DiscountType.NOMINAL) {
            itemDiscountTypeCombo.getSelectionModel().select(2);
            itemDiscountValueField.setDisable(false);
            itemDiscountValueField.setText(item.getDiscountValue().toString());
        }
    }

    private void recalcSummary() {
        InvoiceCalculator.CalculationResult res = InvoiceCalculator.calculateInvoice(itemsData, currentDiscount, InvoiceCalculator.TAX_RATE);
        
        subtotalLabel.setText("$" + String.format("%.2f", res.rawSubtotal));
        discountLabel.setText("$" + String.format("%.2f", res.totalDiscount));
        taxableLabel.setText("$" + String.format("%.2f", res.taxableAmount));
        taxLabel.setText("$" + String.format("%.2f", res.taxAmount));
        totalLabel.setText("$" + String.format("%.2f", res.total));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacion");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
