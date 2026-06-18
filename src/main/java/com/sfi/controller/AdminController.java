package com.sfi.controller;

import com.sfi.App;
import com.sfi.model.*;
import com.sfi.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.sfi.util.UIUtils;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Label userLabel;

    // Facturas
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, String> colInvNumber;
    @FXML private TableColumn<Invoice, String> colInvClient;
    @FXML private TableColumn<Invoice, String> colInvUser;
    @FXML private TableColumn<Invoice, String> colInvDate;
    @FXML private TableColumn<Invoice, BigDecimal> colInvTotal;

    // Productos
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colProdCode;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, BigDecimal> colProdPrice;
    @FXML private TableColumn<Product, Integer> colProdStock;
    @FXML private TableColumn<Product, Integer> colProdMinStock;
    @FXML private TableColumn<Product, String> colProdSupplier;

    // Clientes
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, String> colCliName;
    @FXML private TableColumn<Client, String> colCliAddress;
    @FXML private TableColumn<Client, String> colCliPhone;

    // Proveedores
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, String> colSupName;
    @FXML private TableColumn<Supplier, String> colSupContact;
    @FXML private TableColumn<Supplier, String> colSupPhone;
    @FXML private TableColumn<Supplier, String> colSupEmail;
    @FXML private TableColumn<Supplier, String> colSupAddress;
    @FXML private TableColumn<Supplier, String> colSupCuit;

    // Usuarios
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsrUsername;
    @FXML private TableColumn<User, String> colUsrRole;
    @FXML private ComboBox<String> roleCombo;

    // Stock bajo
    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> colLowName;
    @FXML private TableColumn<Product, Integer> colLowStock;
    @FXML private TableColumn<Product, Integer> colLowMinStock;
    @FXML private TableColumn<Product, Integer> colLowMissing;

    // Reportes
    @FXML private Label totalFacturadoLabel;
    @FXML private Label cantidadFacturasLabel;

    private final ProductService productService = new ProductService();
    private final ClientService clientService = new ClientService();
    private final SupplierService supplierService = new SupplierService();
    private final InvoiceService invoiceService = new InvoiceService();
    private final AuthService authService = App.getAuthService();

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierData = FXCollections.observableArrayList();
    private final ObservableList<User> userData = FXCollections.observableArrayList();
    private final ObservableList<Invoice> invoiceData = FXCollections.observableArrayList();
    private final ObservableList<Product> lowStockData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Admin: " + authService.getCurrentUser().getUsername());

        // Facturas
        colInvNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colInvClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colInvUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colInvDate.setCellValueFactory(cd -> {
            var d = cd.getValue().getIssuedAt();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.toLocalDate().toString() : "");
        });
        colInvTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        invoiceTable.setItems(invoiceData);

        // Productos
        colProdCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProdMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        colProdSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        productTable.setItems(productData);

        // Clientes
        colCliName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCliAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCliPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientTable.setItems(clientData);

        // Proveedores
        colSupName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSupContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colSupPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colSupEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSupAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colSupCuit.setCellValueFactory(new PropertyValueFactory<>("cuit"));
        supplierTable.setItems(supplierData);

        // Usuarios
        colUsrUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsrRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(userData);
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "USER", "REPOSITOR"));

        // Stock bajo
        colLowName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLowStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colLowMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        colLowMissing.setCellValueFactory(cd -> {
            int m = cd.getValue().getMinStock() - cd.getValue().getStock();
            return new javafx.beans.property.SimpleIntegerProperty(Math.max(0, m)).asObject();
        });
        lowStockTable.setItems(lowStockData);

        refreshAll();
    }

    private void refreshAll() {
        loadInvoices();
        loadProducts();
        loadClients();
        loadSuppliers();
        loadUsers();
        loadLowStock();
        loadReports();
    }

    private void loadInvoices() {
        invoiceData.setAll(invoiceService.findAll());
    }

    private void loadProducts() {
        productData.setAll(productService.findAll());
    }

    private void loadClients() {
        clientData.setAll(clientService.findAll());
    }

    private void loadSuppliers() {
        supplierData.setAll(supplierService.findAll());
    }

    private void loadUsers() {
        userData.setAll(authService.allUsers());
    }

    private void loadLowStock() {
        lowStockData.setAll(productService.findLowStock());
    }

    private void loadReports() {
        totalFacturadoLabel.setText("$" + String.format("%.2f", invoiceService.getTotalFacturado()));
        cantidadFacturasLabel.setText(String.valueOf(invoiceService.getCantidadFacturas()));
    }

    // ─── Facturas ───

    @FXML
    public void handleDeleteInvoice() {
        Invoice sel = invoiceTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione una factura"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar factura #" + sel.getInvoiceNumber() + "?",
            ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                invoiceService.delete(sel.getId());
                refreshAll();
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
            }
        }
    }

    // ─── Productos ───

    @FXML
    public void handleAddProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Producto");
        dialog.setHeaderText("Agregar producto");

        Product p = new Product();
        p.setActive(true);

        dialog.setContentText("Codigo:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        p.setCode(result.get().trim());

        dialog.getEditor().clear();
        dialog.setContentText("Nombre:");
        result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        p.setName(result.get().trim());

        dialog.getEditor().clear();
        dialog.setContentText("Descripcion:");
        result = dialog.showAndWait();
        if (result.isPresent()) p.setDescription(result.get().trim());

        dialog.getEditor().clear();
        dialog.setContentText("Precio:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            BigDecimal price = new BigDecimal(result.get().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("El precio debe ser mayor a cero");
            p.setPrice(price); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("precio") ? e.getMessage() : "Precio invalido"); return; }

        dialog.getEditor().clear();
        dialog.setContentText("Stock:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            int stock = Integer.parseInt(result.get().trim());
            if (stock < 0) throw new Exception("El stock no puede ser negativo");
            p.setStock(stock); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("stock") ? e.getMessage() : "Stock invalido"); return; }

        dialog.getEditor().clear();
        dialog.setContentText("Stock minimo:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            int minStock = Integer.parseInt(result.get().trim());
            if (minStock < 0) throw new Exception("El stock minimo no puede ser negativo");
            p.setMinStock(minStock); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("minimo") ? e.getMessage() : "Stock minimo invalido"); return; }

        try { productService.save(p); loadProducts(); }
        catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleEditProduct() {
        Product sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un producto"); return; }

        TextInputDialog dialog = new TextInputDialog(sel.getCode());
        dialog.setTitle("Editar Producto");
        dialog.setHeaderText("Editando: " + sel.getName());

        dialog.setContentText("Codigo:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        sel.setCode(result.get().trim());

        dialog.getEditor().setText(sel.getName());
        dialog.setContentText("Nombre:");
        result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        sel.setName(result.get().trim());

        dialog.getEditor().setText(sel.getDescription());
        dialog.setContentText("Descripcion:");
        result = dialog.showAndWait();
        if (result.isPresent()) sel.setDescription(result.get().trim());

        dialog.getEditor().setText(sel.getPrice().toString());
        dialog.setContentText("Precio:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            BigDecimal price = new BigDecimal(result.get().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("El precio debe ser mayor a cero");
            sel.setPrice(price); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("precio") ? e.getMessage() : "Precio invalido"); return; }

        dialog.getEditor().setText(String.valueOf(sel.getStock()));
        dialog.setContentText("Stock:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            int stock = Integer.parseInt(result.get().trim());
            if (stock < 0) throw new Exception("El stock no puede ser negativo");
            sel.setStock(stock); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("stock") ? e.getMessage() : "Stock invalido"); return; }

        dialog.getEditor().setText(String.valueOf(sel.getMinStock()));
        dialog.setContentText("Stock minimo:");
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try { 
            int minStock = Integer.parseInt(result.get().trim());
            if (minStock < 0) throw new Exception("El stock minimo no puede ser negativo");
            sel.setMinStock(minStock); 
        }
        catch (Exception e) { showAlert(e.getMessage().contains("minimo") ? e.getMessage() : "Stock minimo invalido"); return; }

        try { productService.update(sel); loadProducts(); }
        catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleDeleteProduct() {
        Product sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un producto"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar producto '" + sel.getName() + "'?",
            ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                productService.delete(sel.getId());
                loadProducts();
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
            }
        }
    }

    // ─── Clientes ───

    @FXML
    public void handleAddClient() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Cliente");
        dialog.setHeaderText("Agregar cliente");

        dialog.setContentText("Nombre:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        String name = result.get().trim();

        dialog.getEditor().clear();
        dialog.setContentText("Direccion:");
        result = dialog.showAndWait();
        String address = result.orElse("").trim();

        dialog.getEditor().clear();
        dialog.setContentText("Telefono:");
        result = dialog.showAndWait();
        String phone = result.orElse("").trim();
        if (!phone.isEmpty() && !phone.matches("[0-9\\+\\-\\s]+")) {
            showAlert("El telefono contiene caracteres invalidos");
            return;
        }

        try {
            Client c = new Client();
            c.setName(name);
            c.setAddress(address);
            c.setPhone(phone);
            clientService.save(c);
            loadClients();
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleEditClient() {
        Client sel = clientTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un cliente"); return; }

        TextInputDialog dialog = new TextInputDialog(sel.getName());
        dialog.setTitle("Editar Cliente");
        dialog.setHeaderText("Editando: " + sel.getName());

        dialog.setContentText("Nombre:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        sel.setName(result.get().trim());

        dialog.getEditor().setText(sel.getAddress());
        dialog.setContentText("Direccion:");
        result = dialog.showAndWait();
        sel.setAddress(result.orElse("").trim());

        dialog.getEditor().setText(sel.getPhone());
        dialog.setContentText("Telefono:");
        result = dialog.showAndWait();
        String phoneText = result.orElse("").trim();
        if (!phoneText.isEmpty() && !phoneText.matches("[0-9\\+\\-\\s]+")) {
            showAlert("El telefono contiene caracteres invalidos");
            return;
        }
        sel.setPhone(phoneText);

        try { clientService.update(sel); loadClients(); }
        catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleDeleteClient() {
        Client sel = clientTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un cliente"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar cliente '" + sel.getName() + "'?",
            ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { clientService.delete(sel.getId()); loadClients(); }
            catch (Exception e) { showAlert("Error: " + e.getMessage()); }
        }
    }

    // ─── Usuarios ───

    @FXML
    public void handleChangeRole() {
        User sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un usuario"); return; }

        Long currentUserId = authService.getCurrentUser().getId();
        if (sel.getId().equals(currentUserId)) {
            showAlert("No puedes cambiarte el rol a ti mismo");
            return;
        }

        String newRole = roleCombo.getValue();
        if (newRole == null) { showAlert("Seleccione un rol"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Cambiar rol de '" + sel.getUsername() + "' a " + newRole + "?",
            ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                authService.changeRole(sel.getId(), newRole);
                loadUsers();
            } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
        }
    }

    @FXML
    public void handleCreateUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Usuario");
        dialog.setHeaderText("Nuevo usuario");

        dialog.setContentText("Nombre de usuario:");
        UIUtils.setMaxLength(dialog.getEditor(), 50);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        String username = result.get().trim();
        if (username.length() < 3) {
            showAlert("El nombre de usuario debe tener al menos 3 caracteres");
            return;
        }

        dialog.getEditor().clear();
        dialog.setContentText("Contrasena:");
        UIUtils.setMaxLength(dialog.getEditor(), 255);
        result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isEmpty()) return;
        String password = result.get();
        if (password.length() < 4) {
            showAlert("La contrasena debe tener al menos 4 caracteres");
            return;
        }

        dialog.getEditor().clear();
        dialog.setContentText("Confirmar contrasena:");
        UIUtils.setMaxLength(dialog.getEditor(), 255);
        result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String confirm = result.get();

        ChoiceDialog<String> roleDialog = new ChoiceDialog<>("USER", "USER", "ADMIN", "REPOSITOR");
        roleDialog.setTitle("Rol");
        roleDialog.setHeaderText("Seleccione rol");
        roleDialog.setContentText("Rol:");
        Optional<String> roleResult = roleDialog.showAndWait();
        if (roleResult.isEmpty()) return;

        try {
            authService.registerUser(username, password, confirm, roleResult.get());
            showAlert("Usuario creado correctamente");
            loadUsers();
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleDeleteUser() {
        User sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un usuario"); return; }

        try {
            authService.deleteUser(sel.getId());
            loadUsers();
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    // ─── Stock bajo ───

    @FXML
    public void handleRefreshLowStock() {
        loadLowStock();
    }

    // ─── Generales ───

    @FXML
    public void handleRefresh() {
        refreshAll();
    }

    @FXML
    public void handleNuevaFactura() {
        App.loadVentasScene();
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        App.loadLoginScene();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacion");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ─── Proveedores ───

    @FXML
    public void handleAddSupplier() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Proveedor");
        dialog.setHeaderText("Agregar proveedor");

        dialog.setContentText("Nombre:");
        UIUtils.setMaxLength(dialog.getEditor(), 150);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        String name = result.get().trim();

        dialog.getEditor().clear();
        dialog.setContentText("Contacto:");
        UIUtils.setMaxLength(dialog.getEditor(), 150);
        result = dialog.showAndWait();
        String contact = result.orElse("").trim();

        dialog.getEditor().clear();
        dialog.setContentText("Telefono:");
        UIUtils.setMaxLength(dialog.getEditor(), 50);
        result = dialog.showAndWait();
        String phone = result.orElse("").trim();

        dialog.getEditor().clear();
        dialog.setContentText("Email:");
        UIUtils.setMaxLength(dialog.getEditor(), 100);
        result = dialog.showAndWait();
        String email = result.orElse("").trim();

        dialog.getEditor().clear();
        dialog.setContentText("Direccion:");
        UIUtils.setMaxLength(dialog.getEditor(), 255);
        result = dialog.showAndWait();
        String address = result.orElse("").trim();

        dialog.getEditor().clear();
        dialog.setContentText("CUIT:");
        UIUtils.setMaxLength(dialog.getEditor(), 11);
        result = dialog.showAndWait();
        String cuit = result.orElse("").trim();

        try {
            Supplier s = new Supplier();
            s.setName(name);
            s.setContact(contact);
            s.setPhone(phone);
            s.setEmail(email);
            s.setAddress(address);
            s.setCuit(cuit);
            supplierService.save(s);
            loadSuppliers();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditSupplier() {
        Supplier sel = supplierTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un proveedor"); return; }

        TextInputDialog dialog = new TextInputDialog(sel.getName());
        dialog.setTitle("Editar Proveedor");
        dialog.setHeaderText("Editando: " + sel.getName());

        dialog.setContentText("Nombre:");
        UIUtils.setMaxLength(dialog.getEditor(), 150);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        sel.setName(result.get().trim());

        dialog.getEditor().setText(sel.getContact());
        dialog.setContentText("Contacto:");
        UIUtils.setMaxLength(dialog.getEditor(), 150);
        result = dialog.showAndWait();
        sel.setContact(result.orElse("").trim());

        dialog.getEditor().setText(sel.getPhone());
        dialog.setContentText("Telefono:");
        UIUtils.setMaxLength(dialog.getEditor(), 50);
        result = dialog.showAndWait();
        sel.setPhone(result.orElse("").trim());

        dialog.getEditor().setText(sel.getEmail());
        dialog.setContentText("Email:");
        UIUtils.setMaxLength(dialog.getEditor(), 100);
        result = dialog.showAndWait();
        sel.setEmail(result.orElse("").trim());

        dialog.getEditor().setText(sel.getAddress());
        dialog.setContentText("Direccion:");
        UIUtils.setMaxLength(dialog.getEditor(), 255);
        result = dialog.showAndWait();
        sel.setAddress(result.orElse("").trim());

        dialog.getEditor().setText(sel.getCuit());
        dialog.setContentText("CUIT:");
        UIUtils.setMaxLength(dialog.getEditor(), 11);
        result = dialog.showAndWait();
        sel.setCuit(result.orElse("").trim());

        try {
            supplierService.update(sel);
            loadSuppliers();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteSupplier() {
        Supplier sel = supplierTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un proveedor"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar proveedor '" + sel.getName() + "'?",
            ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                supplierService.delete(sel.getId());
                loadSuppliers();
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
            }
        }
    }
}
