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
            if (client.getPhone().length() > 50) {
                throw new IllegalArgumentException("El teléfono del cliente no puede superar los 50 caracteres");
            }
        }
    }

    public void delete(Long id) {
        if (clientDAO.hasInvoices(id)) {
            throw new IllegalArgumentException("No se puede eliminar el cliente porque tiene facturas asociadas.");
        }
        clientDAO.delete(id);
    }
}
