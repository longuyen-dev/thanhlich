package com.fpt.longnh42.thanhlich.common;

import android.annotation.SuppressLint;

import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.object.Table;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class CommonUtil {
    public CommonUtil() {
    }
    public void updateTableInfo(final String tableName, final String employeeName, final String status) {
        final String[] tableOldInfo = {"",""};
        DatabaseReference orderRef = Reference.getOrderedRef();
        orderRef.child(tableName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int quantum = 0;
                int amount = 0;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    Order order = userSnapshot.getValue(Order.class);
                    quantum += Integer.valueOf(order.getQuantum());
                    amount += Integer.valueOf(order.getAmount());
                }
                tableOldInfo[0] = String.valueOf(quantum);
                tableOldInfo[1] = String.valueOf(amount);
                DatabaseReference tableRef = Reference.getTableRef();
                if ("off".equals(status)) {
                    CommonUtil commonUtil = new CommonUtil();
                    Table table = new Table();
                    table.setAmount(tableOldInfo[1]);
                    table.setOpenTableEmp(employeeName);
                    table.setOpenTime(commonUtil.getSystemDate());
                    table.setStatus("on");
                    table.setQuantum(tableOldInfo[0]);
                    tableRef.child(tableName).setValue(table);
                } else {
                    if ("0".equals(tableOldInfo[1])) {
                        Table table = new Table();
                        table.setStatus("off");
                        tableRef.child(tableName).setValue(table);
                    } else {
                        tableRef.child(tableName).child("quantum").setValue(tableOldInfo[0]);
                        tableRef.child(tableName).child("amount").setValue(tableOldInfo[1]);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // get old info
    }

    public String getSystemDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssaa");
        String currentDateTime = df.format(c);
        return currentDateTime;
    }
    public String getSystemDate2() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String currentDateTime = df.format(c);
        return currentDateTime;
    }
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d");
    }
}
