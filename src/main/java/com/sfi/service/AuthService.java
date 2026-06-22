package com.sfi.service;

import com.sfi.dao.UserDAO;
import com.sfi.model.Role;
import com.sfi.model.User;

import java.util.List;
import java.util.Optional;

public class AuthService {
  private final UserDAO userDAO;
  private User currentUser;

  public AuthService() {
    this.userDAO = new UserDAO();
  }

  public boolean login(String username, String password) {
    Optional<User> userOpt = userDAO.findByUsername(username);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (user.isActive() && password.equals(user.getPassword())) {
        this.currentUser = user;
        return true;
      }
    }
    return false;
  }

  public void logout() {
    this.currentUser = null;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public boolean isAuthenticated() {
    return currentUser != null;
  }

  public boolean isAdmin() {
    return isAuthenticated() && currentUser.isAdmin();
  }

  public boolean isRepositor() {
    return isAuthenticated() && currentUser.isRepositor();
  }

  public List<User> allUsers() {
    return userDAO.findAll();
  }

  public long countByRole(Role role) {
    return userDAO.countByRole(role);
  }

  public boolean registerUser(String username, String password, String confirmPassword, String roleStr) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("El usuario no puede estar vacio");
    }
    String trimmedUsername = username.trim();
    if (trimmedUsername.length() < 3) {
      throw new IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres");
    }
    if (trimmedUsername.length() > 50) {
      throw new IllegalArgumentException("El nombre de usuario no puede superar los 50 caracteres");
    }
    if (!trimmedUsername.matches("^[A-Za-z0-9_]+$")) {
      throw new IllegalArgumentException("El nombre de usuario solo puede contener letras, numeros y guion bajo");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("La contrasena no puede estar vacia");
    }
    if (password.length() < 4) {
      throw new IllegalArgumentException("La contrasena debe tener al menos 4 caracteres");
    }
    if (password.length() > 255) {
      throw new IllegalArgumentException("La contrasena no puede superar los 255 caracteres");
    }
    if (!password.equals(confirmPassword)) {
      throw new IllegalArgumentException("Las contrasenas no coinciden");
    }

    if (userDAO.findByUsername(trimmedUsername).isPresent()) {
      throw new IllegalArgumentException("El usuario ya existe");
    }

    if (roleStr == null || roleStr.isBlank()) {
      throw new IllegalArgumentException("El rol es obligatorio");
    }
    Role role;
    try {
      role = Role.valueOf(roleStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      role = Role.USER;
    }

    User user = new User();
    user.setUsername(trimmedUsername);
    user.setPassword(password);
    user.setRole(role);
    user.setActive(true);
    userDAO.save(user);
    return true;
  }

  public boolean updateUser(Long userId, String username, String password, String confirmPassword, String roleStr) {
    if (userId == null) {
      throw new IllegalArgumentException("El ID del usuario es obligatorio");
    }
    Optional<User> existing = userDAO.findById(userId);
    if (existing.isEmpty()) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }

    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("El usuario no puede estar vacio");
    }
    String trimmedUsername = username.trim();
    if (trimmedUsername.length() < 3) {
      throw new IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres");
    }
    if (trimmedUsername.length() > 50) {
      throw new IllegalArgumentException("El nombre de usuario no puede superar los 50 caracteres");
    }
    if (!trimmedUsername.matches("^[A-Za-z0-9_]+$")) {
      throw new IllegalArgumentException("El nombre de usuario solo puede contener letras, numeros y guion bajo");
    }

    userDAO.findByUsername(trimmedUsername).ifPresent(u -> {
      if (!u.getId().equals(userId)) {
        throw new IllegalArgumentException("El nombre de usuario ya esta en uso");
      }
    });

    if (roleStr == null || roleStr.isBlank()) {
      throw new IllegalArgumentException("El rol es obligatorio");
    }
    Role role;
    try {
      role = Role.valueOf(roleStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Rol invalido: " + roleStr);
    }

    User user = existing.get();
    user.setUsername(trimmedUsername);

    if (!password.isEmpty()) {
      if (password.length() < 4) {
        throw new IllegalArgumentException("La contrasena debe tener al menos 4 caracteres");
      }
      if (password.length() > 255) {
        throw new IllegalArgumentException("La contrasena no puede superar los 255 caracteres");
      }
      if (!password.equals(confirmPassword)) {
        throw new IllegalArgumentException("Las contrasenas no coinciden");
      }
      user.setPassword(password);
    }

    user.setRole(role);
    userDAO.update(user);
    return true;
  }

  public boolean changeRole(Long userId, String newRoleStr) {
    if (newRoleStr == null || newRoleStr.isBlank()) {
      throw new IllegalArgumentException("El rol es obligatorio");
    }
    Optional<User> userOpt = userDAO.findById(userId);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }

    Role newRole;
    try {
      newRole = Role.valueOf(newRoleStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Rol invalido: " + newRoleStr);
    }

    User user = userOpt.get();
    if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
      long adminCount = userDAO.countAllByRole(Role.ADMIN);
      if (adminCount <= 1) {
        throw new IllegalArgumentException("No puedes cambiar el rol del único administrador del sistema");
      }
    }

    user.setRole(newRole);
    userDAO.update(user);
    return true;
  }

  public boolean deleteUser(Long userId) {
    if (currentUser != null && currentUser.getId().equals(userId)) {
      throw new IllegalArgumentException("No puedes eliminarte a ti mismo");
    }

    Optional<User> userOpt = userDAO.findById(userId);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }

    if (userOpt.get().getRole() == Role.ADMIN) {
      long adminCount = userDAO.countAllByRole(Role.ADMIN);
      if (adminCount <= 1) {
        throw new IllegalArgumentException("No puedes eliminar al unico administrador del sistema");
      }
    }

    userDAO.delete(userId);
    return true;
  }
}
