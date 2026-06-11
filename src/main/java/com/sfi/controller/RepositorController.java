package com.sfi.controller;

import com.sfi.App;
import com.sfi.model.Product;
import com.sfi.service.AuthService;
import com.sfi.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class RepositorController implements Initializable {

    @FXML private Label userLabel;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, BigDecimal> colProdPrice;
    @FXML private TableColumn<Product, Integer> colProdStock;
    @FXML private TableColumn<Product, Integer> colProdMinStock;

    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> colLowName;
    @FXML private TableColumn<Product, Integer> colLowStock;
    @FXML private TableColumn<Product, Integer> colLowMinStock;
    @FXML private TableColumn<Product, Integer> colLowMissing;

    private final ProductService productService = new ProductService();
    private final AuthService authService = App.getAuthService();

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<Product> lowStockData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Repositor: " + authService.getCurrentUser().getUsername());

        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProdMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        productTable.setItems(productData);

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
        loadProducts();
        loadLowStock();
    }

    private void loadProducts() {
        productData.setAll(productService.findAll());
    }

    private void loadLowStock() {
        lowStockData.setAll(productService.findLowStock());
    }

    @FXML
    public void handleUpdateStock() {
        Product sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleccione un producto"); return; }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(sel.getStock()));
        dialog.setTitle("Actualizar Stock");
        dialog.setHeaderText("Stock de: " + sel.getName());
        dialog.setContentText("Nuevo stock:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            int newStock = Integer.parseInt(result.get().trim());
            if (newStock < 0) { showAlert("El stock no puede ser negativo"); return; }
            sel.setStock(newStock);
            productService.update(sel);
            refreshAll();
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshProducts() {
        loadProducts();
    }

    @FXML
    public void handleRefreshLowStock() {
        loadLowStock();
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
}
