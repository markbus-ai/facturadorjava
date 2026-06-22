package com.sfi.service;

import com.sfi.dao.ClientDAO;
import com.sfi.model.Client;

import java.util.List;

public class ClientService {
    private final ClientDAO clientDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
    }

    public List<Client> findAll() {
        return clientDAO.findAll();
    }

    public void save(Client client) {
        validateClient(client);
        clientDAO.save(client);
    }

    public void update(Client client) {
        validateClient(client);
        clientDAO.update(client);
    }

    private void validateClient(Client client) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        client.setName(client.getName().trim());
        if (client.getName().length() < 2) {
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        }
        if (client.getName().length() > 150) {
            throw new IllegalArgumentException("El nombre del cliente no puede superar los 150 caracteres");
        }
        if (client.getAddress() != null) {
            client.setAddress(client.getAddress().trim());
            if (client.getAddress().length() > 255) {
                throw new IllegalArgumentException("La dirección del cliente no puede superar los 255 caracteres");
            }
        }
        if (client.getPhone() != null) {
            client.setPhone(client.getPhone().trim());
            if (!client.getPhone().isEmpty()) {
                if (!client.getPhone().matches("[0-9\\+\\-\\s]+")) {
                    throw new IllegalArgumentException("El teléfono contiene caracteres inválidos");
                }
                if (client.getPhone().length() > 50) {
                    throw new IllegalArgumentException("El teléfono del cliente no puede superar los 50 caracteres");
                }
            }
        }
        if (client.getId() != null && client.getId() <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido");
        }
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        if (!clientDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("El cliente no existe");
        }
        if (clientDAO.hasInvoices(id)) {
            throw new IllegalArgumentException("No se puede eliminar el cliente porque tiene facturas asociadas.");
        }
        clientDAO.delete(id);
    }
}
