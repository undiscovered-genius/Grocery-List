package com.example.grocery;

class ProductsModel {
    private String name;
    private String quantity;

    private  ProductsModel(){}

    private  ProductsModel(String name, String quantity){
        this.name = name;
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
