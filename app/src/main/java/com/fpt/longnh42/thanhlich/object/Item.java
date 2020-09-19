package com.fpt.longnh42.thanhlich.object;

public class Item {
    String itemName, price, category, quantum;

    public Item() {
    }

    public Item(String itemName, String price, String category) {
        this.itemName = itemName;
        this.price = price;
        this.category = category;
    }

    public Item(String itemName, String price, String category, String quantum) {
        this.itemName = itemName;
        this.price = price;
        this.category = category;
        this.quantum = quantum;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuantum() {
        return quantum;
    }

    public void setQuantum(String quantum) {
        this.quantum = quantum;
    }
}
