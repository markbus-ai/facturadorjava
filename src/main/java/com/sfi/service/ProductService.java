package com.sfi.service;

import com.sfi.dao.ProductDAO;
import com.sfi.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> findAll() {
        return productDAO.findAll();
    }

    public List<Product> findAllActive() {
        return productDAO.findAllActive();
    }

    public List<Product> findLowStock() {
        return productDAO.findLowStock();
    }

    public void save(Product product) {
        validateProduct(product);
        if (productDAO.findByCode(product.getCode()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto registrado con el código: " + product.getCode());
        }
        productDAO.save(product);
    }

    public void update(Product product) {
        validateProduct(product);
        productDAO.findByCode(product.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(product.getId())) {
                throw new IllegalArgumentException("Ya existe otro producto registrado con el código: " + product.getCode());
            }
        });
        productDAO.update(product);
    }

    private void validateProduct(Product product) {
        if (product.getCode() == null || product.getCode().isBlank()) {
            throw new IllegalArgumentException("El codigo del producto es obligatorio");
        }
        product.setCode(product.getCode().trim());
        if (product.getCode().length() > 20) {
            throw new IllegalArgumentException("El código del producto no puede superar los 20 caracteres");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        product.setName(product.getName().trim());
        if (product.getName().length() > 150) {
            throw new IllegalArgumentException("El nombre del producto no puede superar los 150 caracteres");
        }
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (product.getMinStock() < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }
    }

    public void delete(Long id) {
        if (productDAO.hasInvoiceItems(id)) {
            throw new IllegalArgumentException("No se puede eliminar el producto porque tiene facturas asociadas.");
        }
        productDAO.delete(id);
    }
}
