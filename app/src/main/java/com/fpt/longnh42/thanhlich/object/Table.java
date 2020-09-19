package com.fpt.longnh42.thanhlich.object;

import java.io.Serializable;
import java.util.Map;

public class Table {
    String openTableEmp, amount, quantum, openTime, status;

    public Table() {
    }

    public Table(String openTableEmp, String amount, String quantum, String openTime, String status) {
        this.openTableEmp = openTableEmp;
        this.amount = amount;
        this.quantum = quantum;
        this.openTime = openTime;
        this.status = status;
    }

    public String getOpenTableEmp() {
        return openTableEmp;
    }

    public void setOpenTableEmp(String openTableEmp) {
        this.openTableEmp = openTableEmp;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getQuantum() {
        return quantum;
    }

    public void setQuantum(String quantum) {
        this.quantum = quantum;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
