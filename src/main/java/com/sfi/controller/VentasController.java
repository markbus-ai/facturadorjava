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

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class VentasController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Button adminButton;
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
        adminButton.setVisible(authService.isAdmin());

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
        productCombo.setItems(FXCollections.observableArrayList(productService.findAllActive()));
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

        // Per-item discount
        itemDiscountTypeCombo.setItems(FXCollections.observableArrayList("Sin Descuento", "Porcentual (%)", "Nominal ($)"));
        itemDiscountTypeCombo.getSelectionModel().selectFirst();
        itemDiscountValueField.setDisable(true);

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

        if (qty > product.getStock()) {
            showAlert("Stock insuficiente (disponible: " + product.getStock() + ")");
            return;
        }

        InvoiceItem item = new InvoiceItem(product.getId(), product.getName(), qty, product.getPrice());

        // Apply per-item discount if set
        int discIdx = itemDiscountTypeCombo.getSelectionModel().getSelectedIndex();
        if (discIdx > 0) {
            try {
                double val = Double.parseDouble(itemDiscountValueField.getText().trim());
                if (discIdx == 1) {
                    item.setDiscountType(DiscountType.PERCENTAGE);
                    item.setDiscountValue(BigDecimal.valueOf(val));
                } else if (discIdx == 2) {
                    item.setDiscountType(DiscountType.NOMINAL);
                    item.setDiscountValue(BigDecimal.valueOf(val));
                }
            } catch (NumberFormatException ignored) {}
        }

        itemsData.add(item);
        recalcSummary();
        quantityField.clear();
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
            try {
                double val = Double.parseDouble(valText);
                if (idx == 1) {
                    sel.setDiscountType(DiscountType.PERCENTAGE);
                    sel.setDiscountValue(BigDecimal.valueOf(val));
                } else if (idx == 2) {
                    sel.setDiscountType(DiscountType.NOMINAL);
                    sel.setDiscountValue(BigDecimal.valueOf(val));
                }
            } catch (NumberFormatException e) {
                showAlert("Valor de descuento invalido");
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

        try {
            currentDiscount = switch (idx) {
                case 1 -> new DescuentoPorcentual(Double.parseDouble(valText));
                case 2 -> new DescuentoNominal(Double.parseDouble(valText));
                default -> new SinDescuento();
            };
        } catch (Exception e) {
            showAlert("Valor de descuento invalido");
            currentDiscount = new SinDescuento();
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
            recalcSummary();
            productCombo.setItems(FXCollections.observableArrayList(productService.findAllActive()));
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleGoToAdmin() {
        App.loadAdminScene();
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
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalItemDiscount = BigDecimal.ZERO;

        for (InvoiceItem item : itemsData) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            BigDecimal lineDiscount = BigDecimal.ZERO;
            if (item.getDiscountType() == DiscountType.PERCENTAGE) {
                lineDiscount = lineTotal.multiply(item.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            } else if (item.getDiscountType() == DiscountType.NOMINAL) {
                lineDiscount = item.getDiscountValue().min(lineTotal);
            }

            BigDecimal finalLine = lineTotal.subtract(lineDiscount);
            item.setSubtotal(finalLine);
            totalItemDiscount = totalItemDiscount.add(lineDiscount);
        }

        BigDecimal afterItems = subtotal.subtract(totalItemDiscount);
        BigDecimal globalDiscAmt = currentDiscount.calculate(afterItems);
        BigDecimal taxable = afterItems.subtract(globalDiscAmt);
        BigDecimal taxAmt = taxable.multiply(new BigDecimal("0.21")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(taxAmt);
        BigDecimal totalDisc = totalItemDiscount.add(globalDiscAmt);

        subtotalLabel.setText("$" + String.format("%.2f", subtotal));
        discountLabel.setText("$" + String.format("%.2f", totalDisc));
        taxableLabel.setText("$" + String.format("%.2f", taxable));
        taxLabel.setText("$" + String.format("%.2f", taxAmt));
        totalLabel.setText("$" + String.format("%.2f", total));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacion");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
