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
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contrasenas no coinciden");
        }

        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Role.USER;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole(role);
        user.setActive(true);
        userDAO.save(user);
        return true;
    }

    public boolean changeRole(Long userId, String newRoleStr) {
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
            long adminCount = countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No puedes eliminar al unico administrador del sistema");
            }
        }

        userDAO.delete(userId);
        return true;
    }
}
