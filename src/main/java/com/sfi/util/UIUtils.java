package com.sfi.util;

import javafx.scene.control.TextField;

public class UIUtils {
    public static void setMaxLength(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > maxLength) {
                textField.setText(newValue.substring(0, maxLength));
            }
        });
    }

    /**
     * Restringe el campo a solo dígitos (para campos de cantidad, stock, etc.)
     */
    public static void setNumericOnly(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                textField.setText(oldVal != null ? oldVal : "");
            }
        });
    }

    /**
     * Restringe el campo a formato decimal (dígitos y un solo punto decimal,
     * para campos de precio, descuento, etc.)
     */
    public static void setDecimalOnly(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*\\.?\\d*")) {
                textField.setText(oldVal != null ? oldVal : "");
            }
        });
    }
}
