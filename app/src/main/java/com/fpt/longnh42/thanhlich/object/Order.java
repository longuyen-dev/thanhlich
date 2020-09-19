package com.fpt.longnh42.thanhlich.object;

public class Order {
    String itemName, quantum, amount, tableName, orderTime, orderEmp, status;

    public Order() {
    }

    public Order(String itemName, String quantum, String amount, String orderTime, String orderEmp, String tableName) {
        this.itemName = itemName;
        this.quantum = quantum;
        this.amount = amount;
        this.tableName = tableName;
        this.orderTime = orderTime;
        this.orderEmp = orderEmp;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantum() {
        return quantum;
    }

    public void setQuantum(String quantum) {
        this.quantum = quantum;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderEmp() {
        return orderEmp;
    }

    public void setOrderEmp(String orderEmp) {
        this.orderEmp = orderEmp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
