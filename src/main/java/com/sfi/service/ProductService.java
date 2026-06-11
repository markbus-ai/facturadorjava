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
        if (product.getCode() == null || product.getCode().isBlank()) {
            throw new IllegalArgumentException("El codigo del producto es obligatorio");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (product.getMinStock() < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }
        productDAO.save(product);
    }

    public void update(Product product) {
        productDAO.update(product);
    }

    public void delete(Long id) {
        productDAO.delete(id);
    }
}
