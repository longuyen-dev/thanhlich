package com.fpt.longnh42.thanhlich.object;

public class Employee {
    private String password, rule;

    public Employee() {
    }

    public Employee(String password, String rule) {
        this.password = password;
        this.rule = rule;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
