package com.example.oniria;

public class Transaction {
    private long id;
    private String type; // "Ingreso" o "Gasto"
    private String description;
    private double amount;
    private long dateMillis;
    private String category; // Puede ser null, especialmente para ingresos

    public Transaction(long id, String type, String description, double amount, long dateMillis, String category) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.dateMillis = dateMillis;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public String getCategory() {
        return category;
    }

    // Opcional: setters si necesitas modificar objetos Transaction después de crearlos
}