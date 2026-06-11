package com.sfi.controller;

import com.sfi.App;
import com.sfi.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = App.getAuthService();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Debe ingresar usuario y contrasena");
            return;
        }

        if (authService.login(username, password)) {
            errorLabel.setVisible(false);
            App.loadMainScene();
        } else {
            showError("Usuario o contrasena incorrectos");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
