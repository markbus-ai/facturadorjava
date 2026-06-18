package com.sfi.controller;

import com.sfi.App;
import com.sfi.model.Invoice;
import com.sfi.service.AuthService;
import com.sfi.service.InvoiceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class UserInvoicesController implements Initializable {

    @FXML private Label userLabel;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, String> colInvNumber;
    @FXML private TableColumn<Invoice, String> colInvClient;
    @FXML private TableColumn<Invoice, String> colInvDate;
    @FXML private TableColumn<Invoice, BigDecimal> colInvTotal;

    private final InvoiceService invoiceService = new InvoiceService();
    private final AuthService authService = App.getAuthService();
    private final ObservableList<Invoice> invoiceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Usuario: " + authService.getCurrentUser().getUsername());

        colInvNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colInvClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colInvDate.setCellValueFactory(cd -> {
            var d = cd.getValue().getIssuedAt();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.toLocalDate().toString() : "");
        });
        colInvTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        invoiceTable.setItems(invoiceData);
        loadInvoices();
    }

    private void loadInvoices() {
        invoiceData.setAll(invoiceService.myInvoices(authService.getCurrentUser().getId()));
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
}
