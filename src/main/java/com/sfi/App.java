package com.sfi;

import com.sfi.service.AuthService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class App extends Application {

  private static Stage primaryStage;
  private static final AuthService authService = new AuthService();
  private static final String STYLESHEET = App.class.getResource("/css/style.css").toExternalForm();
  private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
  private static String currentSceneKey = "login";

  @Override
  public void start(Stage stage) {
    primaryStage = stage;
    stage.setTitle("SFI");
    stage.setResizable(true);
    stage.setOnCloseRequest(e -> {
      if (!currentSceneKey.equals("login")) {
        saveWindowState(currentSceneKey);
      }
    });
    loadLoginScene();
    stage.show();
  }

  // guardo el estado de la ventana
  private static void saveWindowState(String key) {
    prefs.putInt(key + ".x", (int) primaryStage.getX());
    prefs.putInt(key + ".y", (int) primaryStage.getY());
    prefs.putInt(key + ".w", (int) primaryStage.getWidth());
    prefs.putInt(key + ".h", (int) primaryStage.getHeight());
    prefs.putBoolean(key + ".maximized", primaryStage.isMaximized());
  }

  // restaura el estado de la ventana
  private static void restoreOrSetWindow(String key, int defW, int defH, int minW, int minH) {
    int w = Math.max(prefs.getInt(key + ".w", defW), minW);
    int h = Math.max(prefs.getInt(key + ".h", defH), minH);
    int x = prefs.getInt(key + ".x", -1);
    int y = prefs.getInt(key + ".y", -1);
    boolean maximized = prefs.getBoolean(key + ".maximized", true);

    primaryStage.setMinWidth(minW);
    primaryStage.setMinHeight(minH);

    if (maximized) {
      primaryStage.setMaximized(true);
      return;
    }

    if (x >= 0 && y >= 0) {
      primaryStage.setX(x);
      primaryStage.setY(y);
    }
    primaryStage.setWidth(w);
    primaryStage.setHeight(h);
  }

  public static void loadLoginScene() {
    if (!currentSceneKey.equals("login")) {
      saveWindowState(currentSceneKey);
    }
    try {
      Scene scene = new Scene(
          FXMLLoader.load(App.class.getResource("/view/LoginView.fxml")));
      scene.getStylesheets().add(STYLESHEET);
      currentSceneKey = "login";
      primaryStage.setResizable(true);
      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);
    } catch (IOException e) {
      throw new RuntimeException("Error al cargar LoginView", e);
    }
  }

  public static void loadMainScene() {
    if (currentSceneKey.equals("admin"))
      saveWindowState("admin");
    else if (currentSceneKey.equals("repositor"))
      saveWindowState("repositor");
    else if (currentSceneKey.equals("ventas"))
      saveWindowState("ventas");
    else if (currentSceneKey.equals("user_invoices"))
      saveWindowState("user_invoices");

    primaryStage.setResizable(true);
    if (authService.isAdmin()) {
      loadAdminScene();
    } else if (authService.isRepositor()) {
      loadRepositorScene();
    } else {
      loadUserInvoicesScene();
    }
  }

  public static void loadAdminScene() {
    try {
      Scene scene = new Scene(
          FXMLLoader.load(App.class.getResource("/view/AdminView.fxml")),
          1000, 640);
      scene.getStylesheets().add(STYLESHEET);
      currentSceneKey = "admin";
      primaryStage.setScene(scene);
      restoreOrSetWindow("admin", 1000, 640, 700, 450);
    } catch (IOException e) {
      throw new RuntimeException("Error al cargar AdminView", e);
    }
  }

  public static void loadRepositorScene() {
    try {
      Scene scene = new Scene(
          FXMLLoader.load(App.class.getResource("/view/RepositorView.fxml")),
          900, 600);
      scene.getStylesheets().add(STYLESHEET);
      currentSceneKey = "repositor";
      primaryStage.setScene(scene);
      restoreOrSetWindow("repositor", 900, 600, 600, 400);
    } catch (IOException e) {
      throw new RuntimeException("Error al cargar RepositorView", e);
    }
  }

  public static void loadVentasScene() {
    try {
      Scene scene = new Scene(
          FXMLLoader.load(App.class.getResource("/view/VentasView.fxml")),
          900, 600);
      scene.getStylesheets().add(STYLESHEET);
      currentSceneKey = "ventas";
      primaryStage.setScene(scene);
      restoreOrSetWindow("ventas", 900, 600, 650, 500);
    } catch (IOException e) {
      throw new RuntimeException("Error al cargar VentasView", e);
    }
  }

  public static void loadUserInvoicesScene() {
    try {
      Scene scene = new Scene(
          FXMLLoader.load(App.class.getResource("/view/UserInvoicesView.fxml")),
          900, 600);
      scene.getStylesheets().add(STYLESHEET);
      currentSceneKey = "user_invoices";
      primaryStage.setScene(scene);
      restoreOrSetWindow("user_invoices", 900, 600, 650, 500);
    } catch (IOException e) {
      throw new RuntimeException("Error al cargar UserInvoicesView", e);
    }
  }

  public static void quit() {
    saveWindowState(currentSceneKey);
    Platform.exit();
  }

  public static AuthService getAuthService() {
    return authService;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
