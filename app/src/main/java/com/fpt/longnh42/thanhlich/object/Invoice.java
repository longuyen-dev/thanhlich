package com.fpt.longnh42.thanhlich.object;

public class Invoice {
    String tableName, paymentEmp, amount, paymentDate;

    public Invoice() {
    }

    public Invoice(String tableName, String paymentEmp, String amount, String paymentDate) {
        this.tableName = tableName;
        this.paymentEmp = paymentEmp;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPaymentEmp() {
        return paymentEmp;
    }

    public void setPaymentEmp(String paymentEmp) {
        this.paymentEmp = paymentEmp;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
