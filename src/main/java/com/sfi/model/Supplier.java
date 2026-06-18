package com.sfi.model;

public class Supplier {
    private Long id;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String cuit;

    public Supplier() {}

    public Supplier(Long id, String name, String contact, String phone, String email, String address, String cuit) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cuit = cuit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    @Override
    public String toString() {
        return name;
    }
}
