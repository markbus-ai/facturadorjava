package com.sfi.controller;

import com.sfi.App;
import com.sfi.model.*;
import com.sfi.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

  @FXML
  private Label userLabel;

  // Facturas
  @FXML
  private TableView<Invoice> invoiceTable;
  @FXML
  private TableColumn<Invoice, String> colInvNumber;
  @FXML
  private TableColumn<Invoice, String> colInvClient;
  @FXML
  private TableColumn<Invoice, String> colInvUser;
  @FXML
  private TableColumn<Invoice, String> colInvDate;
  @FXML
  private TableColumn<Invoice, BigDecimal> colInvTotal;

  // Productos
  @FXML
  private TableView<Product> productTable;
  @FXML
  private TableColumn<Product, String> colProdCode;
  @FXML
  private TableColumn<Product, String> colProdName;
  @FXML
  private TableColumn<Product, BigDecimal> colProdPrice;
  @FXML
  private TableColumn<Product, Integer> colProdStock;
  @FXML
  private TableColumn<Product, Integer> colProdMinStock;
  @FXML
  private TableColumn<Product, String> colProdSupplier;

  // Clientes
  @FXML
  private TableView<Client> clientTable;
  @FXML
  private TableColumn<Client, String> colCliName;
  @FXML
  private TableColumn<Client, String> colCliAddress;
  @FXML
  private TableColumn<Client, String> colCliPhone;

  // Proveedores
  @FXML
  private TableView<Supplier> supplierTable;
  @FXML
  private TableColumn<Supplier, String> colSupName;
  @FXML
  private TableColumn<Supplier, String> colSupContact;
  @FXML
  private TableColumn<Supplier, String> colSupPhone;
  @FXML
  private TableColumn<Supplier, String> colSupEmail;
  @FXML
  private TableColumn<Supplier, String> colSupAddress;
  @FXML
  private TableColumn<Supplier, String> colSupCuit;

  // Usuarios
  @FXML
  private TableView<User> userTable;
  @FXML
  private TableColumn<User, String> colUsrUsername;
  @FXML
  private TableColumn<User, String> colUsrRole;

  // Stock bajo
  @FXML
  private TableView<Product> lowStockTable;
  @FXML
  private TableColumn<Product, String> colLowName;
  @FXML
  private TableColumn<Product, Integer> colLowStock;
  @FXML
  private TableColumn<Product, Integer> colLowMinStock;
  @FXML
  private TableColumn<Product, Integer> colLowMissing;

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

  // ─── Facturas ───

  @FXML
  public void handleDeleteInvoice() {
    Invoice sel = invoiceTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showAlert("Seleccione una factura");
      return;
    }

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
    Map<String, String> defaults = new LinkedHashMap<>();
    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Nuevo Producto", "Agregar producto",
          defaults, "Codigo", "Nombre", "Descripcion", "Precio", "Stock", "Stock minimo");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String code = v.get("Codigo").trim();
      if (code.isBlank()) errors.add("El codigo es obligatorio");
      else if (code.length() > 20) errors.add("El codigo no puede superar 20 caracteres");

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String desc = v.get("Descripcion").trim();

      BigDecimal price = null;
      try {
        price = new BigDecimal(v.get("Precio").trim());
        if (price.compareTo(BigDecimal.ZERO) <= 0) errors.add("El precio debe ser mayor a cero");
        else if (price.scale() > 2) errors.add("El precio no puede tener mas de 2 decimales");
        else if (price.compareTo(new BigDecimal("99999999999.99")) > 0)
          errors.add("El precio no puede superar 99.999.999.999,99");
      } catch (NumberFormatException e) {
        errors.add("Precio invalido");
      }

      int stock = 0;
      try {
        stock = Integer.parseInt(v.get("Stock").trim());
        if (stock < 0) errors.add("El stock no puede ser negativo");
        else if (stock > 999_999_999) errors.add("El stock no puede superar 999.999.999");
      } catch (NumberFormatException e) {
        errors.add("Stock invalido");
      }

      int minStock = 0;
      try {
        minStock = Integer.parseInt(v.get("Stock minimo").trim());
        if (minStock < 0) errors.add("El stock minimo no puede ser negativo");
        else if (minStock > 999_999_999) errors.add("El stock minimo no puede superar 999.999.999");
      } catch (NumberFormatException e) {
        errors.add("Stock minimo invalido");
      }

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      Product p = new Product();
      p.setActive(true);
      p.setCode(code);
      p.setName(name);
      if (!desc.isBlank()) p.setDescription(desc);
      p.setPrice(price);
      p.setStock(stock);
      p.setMinStock(minStock);

      try {
        productService.save(p);
        loadProducts();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleEditProduct() {
    Product sel = productTable.getSelectionModel().getSelectedItem();
    if (sel == null) { showAlert("Seleccione un producto"); return; }

    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("Codigo", sel.getCode());
    defaults.put("Nombre", sel.getName());
    defaults.put("Descripcion", sel.getDescription() != null ? sel.getDescription() : "");
    defaults.put("Precio", sel.getPrice().toString());
    defaults.put("Stock", String.valueOf(sel.getStock()));
    defaults.put("Stock minimo", String.valueOf(sel.getMinStock()));

    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Editar Producto",
          "Editando: " + sel.getName(), defaults,
          "Codigo", "Nombre", "Descripcion", "Precio", "Stock", "Stock minimo");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String code = v.get("Codigo").trim();
      if (code.isBlank()) errors.add("El codigo es obligatorio");
      else if (code.length() > 20) errors.add("El codigo no puede superar 20 caracteres");

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String desc = v.get("Descripcion").trim();

      BigDecimal price = null;
      try {
        price = new BigDecimal(v.get("Precio").trim());
        if (price.compareTo(BigDecimal.ZERO) <= 0) errors.add("El precio debe ser mayor a cero");
        else if (price.scale() > 2) errors.add("El precio no puede tener mas de 2 decimales");
        else if (price.compareTo(new BigDecimal("99999999999.99")) > 0)
          errors.add("El precio no puede superar 99.999.999.999,99");
      } catch (NumberFormatException e) {
        errors.add("Precio invalido");
      }

      int stock = 0;
      try {
        stock = Integer.parseInt(v.get("Stock").trim());
        if (stock < 0) errors.add("El stock no puede ser negativo");
        else if (stock > 999_999_999) errors.add("El stock no puede superar 999.999.999");
      } catch (NumberFormatException e) {
        errors.add("Stock invalido");
      }

      int minStock = 0;
      try {
        minStock = Integer.parseInt(v.get("Stock minimo").trim());
        if (minStock < 0) errors.add("El stock minimo no puede ser negativo");
        else if (minStock > 999_999_999) errors.add("El stock minimo no puede superar 999.999.999");
      } catch (NumberFormatException e) {
        errors.add("Stock minimo invalido");
      }

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      sel.setCode(code);
      sel.setName(name);
      sel.setDescription(desc.isBlank() ? null : desc);
      sel.setPrice(price);
      sel.setStock(stock);
      sel.setMinStock(minStock);

      try {
        productService.update(sel);
        loadProducts();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleDeleteProduct() {
    Product sel = productTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showAlert("Seleccione un producto");
      return;
    }

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
    Map<String, String> defaults = new LinkedHashMap<>();
    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Nuevo Cliente", "Agregar cliente",
          defaults, "Nombre", "Direccion", "Telefono");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() < 2) errors.add("El nombre debe tener al menos 2 caracteres");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String address = v.get("Direccion").trim();
      String phone = v.get("Telefono").trim();

      if (!phone.isEmpty() && !phone.matches("[0-9\\+\\-\\s]+"))
        errors.add("El telefono contiene caracteres invalidos");

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      Client c = new Client();
      c.setName(name);
      c.setAddress(address);
      c.setPhone(phone);

      try {
        clientService.save(c);
        loadClients();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleEditClient() {
    Client sel = clientTable.getSelectionModel().getSelectedItem();
    if (sel == null) { showAlert("Seleccione un cliente"); return; }

    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("Nombre", sel.getName());
    defaults.put("Direccion", sel.getAddress() != null ? sel.getAddress() : "");
    defaults.put("Telefono", sel.getPhone() != null ? sel.getPhone() : "");

    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Editar Cliente",
          "Editando: " + sel.getName(), defaults,
          "Nombre", "Direccion", "Telefono");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() < 2) errors.add("El nombre debe tener al menos 2 caracteres");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String address = v.get("Direccion").trim();
      String phone = v.get("Telefono").trim();

      if (!phone.isEmpty() && !phone.matches("[0-9\\+\\-\\s]+"))
        errors.add("El telefono contiene caracteres invalidos");

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      sel.setName(name);
      sel.setAddress(address);
      sel.setPhone(phone);

      try {
        clientService.update(sel);
        loadClients();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleDeleteClient() {
    Client sel = clientTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showAlert("Seleccione un cliente");
      return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Eliminar cliente '" + sel.getName() + "'?",
        ButtonType.YES, ButtonType.NO);
    if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
      try {
        clientService.delete(sel.getId());
        loadClients();
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
      }
    }
  }

  // ─── Usuarios ───

  @FXML
  public void handleEditUser() {
    User sel = userTable.getSelectionModel().getSelectedItem();
    if (sel == null) { showAlert("Seleccione un usuario"); return; }

    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("Nombre de usuario", sel.getUsername());
    defaults.put("Rol", sel.getRole().name());

    Map<String, String[]> choices = new HashMap<>();
    choices.put("Rol", new String[]{"USER", "ADMIN", "REPOSITOR"});

    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Editar Usuario",
          "Editando: " + sel.getUsername(), defaults, choices,
          "Nombre de usuario", "Contrasena", "Confirmar contrasena", "Rol");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String username = v.get("Nombre de usuario").trim();
      if (username.isBlank()) errors.add("El nombre de usuario es obligatorio");
      else if (username.length() < 3) errors.add("El nombre de usuario debe tener al menos 3 caracteres");
      else if (username.length() > 50) errors.add("El nombre de usuario no puede superar los 50 caracteres");

      String password = v.get("Contrasena");
      String confirm = v.get("Confirmar contrasena");
      if (!password.isEmpty()) {
        if (password.length() < 4) errors.add("La contrasena debe tener al menos 4 caracteres");
        else if (password.length() > 255) errors.add("La contrasena no puede superar los 255 caracteres");
        else if (confirm.isEmpty()) errors.add("Debe confirmar la contrasena");
        else if (!password.equals(confirm)) errors.add("Las contrasenas no coinciden");
      }

      String roleStr = v.get("Rol");
      if (roleStr == null || roleStr.isBlank()) errors.add("El rol es obligatorio");

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      try {
        authService.updateUser(sel.getId(), username, password, confirm, roleStr);
        loadUsers();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleCreateUser() {
    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("Rol", "USER");

    Map<String, String[]> choices = new HashMap<>();
    choices.put("Rol", new String[]{"USER", "ADMIN", "REPOSITOR"});

    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Crear Usuario", "Nuevo usuario",
          defaults, choices,
          "Nombre de usuario", "Contrasena", "Confirmar contrasena", "Rol");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String username = v.get("Nombre de usuario").trim();
      if (username.isBlank()) errors.add("El nombre de usuario es obligatorio");
      else if (username.length() < 3) errors.add("El nombre de usuario debe tener al menos 3 caracteres");
      else if (username.length() > 50) errors.add("El nombre de usuario no puede superar los 50 caracteres");

      String password = v.get("Contrasena");
      if (password.isEmpty()) errors.add("La contrasena es obligatoria");
      else if (password.length() < 4) errors.add("La contrasena debe tener al menos 4 caracteres");
      else if (password.length() > 255) errors.add("La contrasena no puede superar los 255 caracteres");

      String confirm = v.get("Confirmar contrasena");
      if (confirm.isEmpty()) errors.add("Debe confirmar la contrasena");
      else if (!password.equals(confirm)) errors.add("Las contrasenas no coinciden");

      String roleStr = v.get("Rol");
      if (roleStr == null || roleStr.isBlank()) errors.add("El rol es obligatorio");

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      try {
        authService.registerUser(username, password, confirm, roleStr);
        showAlert("Usuario creado correctamente");
        loadUsers();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleDeleteUser() {
    User sel = userTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showAlert("Seleccione un usuario");
      return;
    }

    try {
      authService.deleteUser(sel.getId());
      loadUsers();
    } catch (Exception e) {
      showAlert("Error: " + e.getMessage());
    }
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

  private Optional<Map<String, String>> showFormDialog(String title, String header,
          Map<String, String> defaults, String... labels) {
    return showFormDialog(title, header, defaults, new HashMap<>(), labels);
  }

  private Optional<Map<String, String>> showFormDialog(String title, String header,
          Map<String, String> defaults, Map<String, String[]> choices, String... labels) {
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);

    List<TextField> fields = new ArrayList<>();
    Map<Integer, ComboBox<String>> combos = new HashMap<>();
    for (int i = 0; i < labels.length; i++) {
      grid.add(new Label(labels[i] + ":"), 0, i);
      String[] opts = choices.get(labels[i]);
      if (opts != null && opts.length > 0) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(opts);
        cb.setEditable(false);
        String val = defaults.getOrDefault(labels[i], opts[0]);
        cb.setValue(val);
        cb.setMaxWidth(300);
        grid.add(cb, 1, i);
        combos.put(i, cb);
      } else {
        TextField tf = new TextField(defaults.getOrDefault(labels[i], ""));
        tf.setMaxWidth(300);
        grid.add(tf, 1, i);
        fields.add(tf);
      }
    }

    dialog.getDialogPane().setContent(grid);
    Platform.runLater(() -> {
      if (!fields.isEmpty()) fields.get(0).requestFocus();
    });

    dialog.setResultConverter(btn -> {
      if (btn == ButtonType.OK) {
        Map<String, String> result = new LinkedHashMap<>();
        int tfIdx = 0;
        for (int i = 0; i < labels.length; i++) {
          ComboBox<String> cb = combos.get(i);
          if (cb != null) {
            result.put(labels[i], cb.getValue());
          } else {
            result.put(labels[i], fields.get(tfIdx).getText());
            tfIdx++;
          }
        }
        return result;
      }
      return null;
    });

    return dialog.showAndWait();
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
    Map<String, String> defaults = new LinkedHashMap<>();
    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Nuevo Proveedor", "Agregar proveedor",
          defaults, "Nombre", "Contacto", "Telefono", "Email", "Direccion", "CUIT");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() < 2) errors.add("El nombre debe tener al menos 2 caracteres");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String contact = v.get("Contacto").trim();
      String phone = v.get("Telefono").trim();
      String email = v.get("Email").trim();
      String address = v.get("Direccion").trim();
      String cuit = v.get("CUIT").trim();

      if (!phone.isEmpty()) {
        if (!phone.matches("[0-9\\+\\-\\s]+"))
          errors.add("El telefono contiene caracteres invalidos");
        else if (phone.length() > 50)
          errors.add("El telefono no puede superar 50 caracteres");
        else {
          String cleanPhone = phone.replaceAll("\\D", "");
          if (cleanPhone.length() < 7)
            errors.add("El telefono debe tener al menos 7 digitos");
        }
      }

      if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$"))
        errors.add("El correo electronico no es valido");

      if (!cuit.isEmpty()) {
        String cleanCuit = cuit.replaceAll("\\D", "");
        if (cleanCuit.length() != 11)
          errors.add("El CUIT debe tener exactamente 11 digitos numericos");
      }

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      Supplier s = new Supplier();
      s.setName(name);
      s.setContact(contact);
      s.setPhone(phone);
      s.setEmail(email);
      s.setAddress(address);
      s.setCuit(cuit);

      try {
        supplierService.save(s);
        loadSuppliers();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleEditSupplier() {
    Supplier sel = supplierTable.getSelectionModel().getSelectedItem();
    if (sel == null) { showAlert("Seleccione un proveedor"); return; }

    Map<String, String> defaults = new LinkedHashMap<>();
    defaults.put("Nombre", sel.getName());
    defaults.put("Contacto", sel.getContact() != null ? sel.getContact() : "");
    defaults.put("Telefono", sel.getPhone() != null ? sel.getPhone() : "");
    defaults.put("Email", sel.getEmail() != null ? sel.getEmail() : "");
    defaults.put("Direccion", sel.getAddress() != null ? sel.getAddress() : "");
    defaults.put("CUIT", sel.getCuit() != null ? sel.getCuit() : "");

    while (true) {
      Optional<Map<String, String>> result = showFormDialog("Editar Proveedor",
          "Editando: " + sel.getName(), defaults,
          "Nombre", "Contacto", "Telefono", "Email", "Direccion", "CUIT");
      if (result.isEmpty()) return;

      Map<String, String> v = result.get();
      List<String> errors = new ArrayList<>();

      String name = v.get("Nombre").trim();
      if (name.isBlank()) errors.add("El nombre es obligatorio");
      else if (name.length() < 2) errors.add("El nombre debe tener al menos 2 caracteres");
      else if (name.length() > 150) errors.add("El nombre no puede superar 150 caracteres");

      String contact = v.get("Contacto").trim();
      String phone = v.get("Telefono").trim();
      String email = v.get("Email").trim();
      String address = v.get("Direccion").trim();
      String cuit = v.get("CUIT").trim();

      if (!phone.isEmpty()) {
        if (!phone.matches("[0-9\\+\\-\\s]+"))
          errors.add("El telefono contiene caracteres invalidos");
        else if (phone.length() > 50)
          errors.add("El telefono no puede superar 50 caracteres");
        else {
          String cleanPhone = phone.replaceAll("\\D", "");
          if (cleanPhone.length() < 7)
            errors.add("El telefono debe tener al menos 7 digitos");
        }
      }

      if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$"))
        errors.add("El correo electronico no es valido");

      if (!cuit.isEmpty()) {
        String cleanCuit = cuit.replaceAll("\\D", "");
        if (cleanCuit.length() != 11)
          errors.add("El CUIT debe tener exactamente 11 digitos numericos");
      }

      if (!errors.isEmpty()) {
        showAlert("Corrija los siguientes errores:\n\n- " + String.join("\n- ", errors));
        defaults.putAll(v);
        continue;
      }

      sel.setName(name);
      sel.setContact(contact);
      sel.setPhone(phone);
      sel.setEmail(email);
      sel.setAddress(address);
      sel.setCuit(cuit);

      try {
        supplierService.update(sel);
        loadSuppliers();
        return;
      } catch (Exception e) {
        showAlert("Error: " + e.getMessage());
        defaults.putAll(v);
      }
    }
  }

  @FXML
  public void handleDeleteSupplier() {
    Supplier sel = supplierTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showAlert("Seleccione un proveedor");
      return;
    }

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
