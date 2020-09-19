package com.fpt.longnh42.thanhlich.common;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Reference {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference getEmployeeRef() {
        return database.getReference("Employee");
    }
    public static DatabaseReference getTableRef() {
        return database.getReference("Table");
    }
    public static DatabaseReference getInvoiceRef() {
        return database.getReference("Invoice");
    }
    public static DatabaseReference getDetailInvoiceRef() {
        return database.getReference("DetailInvoice");
    }
    public static DatabaseReference getItemRef() {
        return database.getReference("Item");
    }
    public static DatabaseReference getCategoryRef() {
        return database.getReference("Category");
    }
    public static DatabaseReference getOrderedRef() {
        return database.getReference("Ordered");
    }
}
