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
}
