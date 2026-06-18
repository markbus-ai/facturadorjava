package com.sfi.service;

import com.sfi.dao.SupplierDAO;
import com.sfi.model.Supplier;

import java.util.List;
import java.util.Optional;

public class SupplierService {
    private final SupplierDAO supplierDAO;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
    }

    public List<Supplier> findAll() {
        return supplierDAO.findAll();
    }

    public Optional<Supplier> findById(Long id) {
        return supplierDAO.findById(id);
    }

    public void save(Supplier supplier) {
        validateSupplier(supplier);
        supplierDAO.save(supplier);
    }

    public void update(Supplier supplier) {
        validateSupplier(supplier);
        supplierDAO.update(supplier);
    }

    public void delete(Long id) {
        if (supplierDAO.hasProducts(id)) {
            throw new IllegalArgumentException("No se puede eliminar el proveedor porque tiene productos asociados.");
        }
        supplierDAO.delete(id);
    }

    private void validateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }
        supplier.setName(supplier.getName().trim());
        if (supplier.getName().length() > 150) {
            throw new IllegalArgumentException("El nombre del proveedor no puede superar los 150 caracteres.");
        }
        if (supplier.getContact() != null) {
            supplier.setContact(supplier.getContact().trim());
            if (supplier.getContact().length() > 150) {
                throw new IllegalArgumentException("El contacto del proveedor no puede superar los 150 caracteres.");
            }
        }
        if (supplier.getPhone() != null) {
            supplier.setPhone(supplier.getPhone().trim());
            if (!supplier.getPhone().isEmpty()) {
                String cleanPhone = supplier.getPhone().replaceAll("\\D", "");
                if (cleanPhone.length() < 7) {
                    throw new IllegalArgumentException("El teléfono debe tener al menos 7 dígitos.");
                }
            }
        }
        if (supplier.getEmail() != null) {
            supplier.setEmail(supplier.getEmail().trim());
            if (!supplier.getEmail().isEmpty()) {
                if (!supplier.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    throw new IllegalArgumentException("El correo electrónico no es válido.");
                }
            }
        }
        if (supplier.getAddress() != null) {
            supplier.setAddress(supplier.getAddress().trim());
            if (supplier.getAddress().length() > 255) {
                throw new IllegalArgumentException("La dirección no puede superar los 255 caracteres.");
            }
        }
        if (supplier.getCuit() != null) {
            supplier.setCuit(supplier.getCuit().trim());
            if (!supplier.getCuit().isEmpty()) {
                String cleanCuit = supplier.getCuit().replaceAll("\\D", "");
                if (cleanCuit.length() != 11) {
                    throw new IllegalArgumentException("El CUIT debe tener exactamente 11 dígitos numéricos.");
                }
            }
        }
    }
}
