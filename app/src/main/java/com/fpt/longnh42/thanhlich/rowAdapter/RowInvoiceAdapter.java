package com.fpt.longnh42.thanhlich.rowAdapter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Invoice;
import com.fpt.longnh42.thanhlich.object.Table;

import java.util.Map;
import java.util.TreeMap;

public class RowInvoiceAdapter extends BaseAdapter {

    private Map<String, Invoice> invoiceMap;
    private Map<String, Invoice> sortedMap;

    public RowInvoiceAdapter(Map<String, Invoice> invoiceMap) {
        this.invoiceMap = invoiceMap;
    }

    @Override
    public int getCount() {

        if (invoiceMap.size() != 0 || invoiceMap != null) {
            sortedMap = new TreeMap<>(invoiceMap);
        }
        return invoiceMap.size();
    }

    @Override
    public Invoice getItem(int position) {
        return (Invoice) sortedMap.values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        v = inflater.inflate(R.layout.activity_row_invoice_adapter,null);
        if (invoiceMap.size() != 0 || invoiceMap != null) {
            Invoice invoice = getItem(position);
            TextView tableNameTextView = v.findViewById(R.id.tableNameTextView);
            tableNameTextView.setText(v.getResources().getString(R.string.table)+" "
                    + invoice.getTableName());

            TextView amountTextView = v.findViewById(R.id.amountTextView);
            if (invoice.getAmount() == null) {
                amountTextView.setText("0");
            } else {
                amountTextView.setText(String.format("%,d", Integer.valueOf(invoice.getAmount())));
            }

            TextView employeeNameTextView = v.findViewById(R.id.employeeNameTextView);
            employeeNameTextView.setText(invoice.getPaymentEmp());

            TextView timeTextView = v.findViewById(R.id.timeTextView);
            String time = invoice.getPaymentDate().substring(8, 10) + ":"
                    + invoice.getPaymentDate().substring(10, 12);
            timeTextView.setText(time);

        }

        return v;
    }
}
