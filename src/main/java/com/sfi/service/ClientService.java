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
        if (client.getName() == null || client.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        clientDAO.save(client);
    }

    public void update(Client client) {
        clientDAO.update(client);
    }

    public void delete(Long id) {
        clientDAO.delete(id);
    }
}
