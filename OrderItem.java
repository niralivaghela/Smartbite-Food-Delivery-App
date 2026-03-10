package com.smartbite.models;

import java.util.Objects;

public class OrderItem {

    private String menuItemId;
    private String name;
    private int    quantity;
    private double price;

    // ── Constructors ─────────────────────────────────────────────────────────

    public OrderItem() {}

    public OrderItem(String menuItemId, String name, int quantity, double price) {
        this.menuItemId = menuItemId;
        this.name       = name;
        this.quantity   = quantity;
        this.price      = price;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getMenuItemId() { return menuItemId; }
    public String getName()       { return name; }
    public int    getQuantity()   { return quantity; }
    public double getPrice()      { return price; }
    public double getSubtotal()   { return price * quantity; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
    public void setName(String name)             { this.name = name; }
    public void setQuantity(int quantity)        { this.quantity = quantity; }
    public void setPrice(double price)           { this.price = price; }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return quantity == that.quantity
                && Double.compare(that.price, price) == 0
                && Objects.equals(menuItemId, that.menuItemId)
                && Objects.equals(name,       that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItemId, name, quantity, price);
    }
}