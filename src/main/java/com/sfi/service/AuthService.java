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

    public static String hashPassword(String password) {
        try {
            String salted = password + "sfi_salt_2026";
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salted.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    public boolean login(String username, String password) {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isActive() && hashPassword(password).equals(user.getPassword())) {
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
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 4 caracteres");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contrasenas no coinciden");
        }

        if (userDAO.findByUsername(trimmedUsername).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Role.USER;
        }

        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(hashPassword(password));
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
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long adminCount = countByRole(Role.ADMIN);
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
            long adminCount = countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No puedes eliminar al unico administrador del sistema");
            }
        }

        userDAO.delete(userId);
        return true;
    }
}
