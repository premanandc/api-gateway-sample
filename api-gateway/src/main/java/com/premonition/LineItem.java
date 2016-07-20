package com.premonition;

public class LineItem {
    private String name;
    private int price;

    @SuppressWarnings("unused")
    private LineItem() {
    }

    public LineItem(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
