package com.premonition;

import java.util.Collection;

public class ShoppingCart {
    private String id;

    private Collection<LineItem> lineItems;

    public String getId() {
        return id;
    }

    public Collection<LineItem> getLineItems() {
        return lineItems;
    }
}
