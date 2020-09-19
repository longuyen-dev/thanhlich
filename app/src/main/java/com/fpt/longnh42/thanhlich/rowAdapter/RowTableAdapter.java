package com.fpt.longnh42.thanhlich.rowAdapter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Table;

import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Inflater;

public class RowTableAdapter extends BaseAdapter {

    private Map<Integer, Table> tableMap;
    private Map<Integer, Table> sortedMap;

    public RowTableAdapter(Map<Integer, Table> tableMap) {
        this.tableMap = tableMap;
    }

    @Override
    public int getCount() {
        if (tableMap.size() != 0) {
            sortedMap = new TreeMap<>(tableMap);
        }
        return tableMap.size();
    }

    @Override
    public Table getItem(int position) {
        return (Table) sortedMap.values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return (int) sortedMap.keySet().toArray()[position];
    }

    @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams", "DefaultLocale"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        v = inflater.inflate(R.layout.adapter_row_table,null);
        if (tableMap != null || tableMap.size() != 0) {
            Table table = getItem(position);
            TextView tableNameTextView = v.findViewById(R.id.tableNameTextView);
            tableNameTextView.setText(""+getItemId(position));

            TextView amountTextView = v.findViewById(R.id.amountTextView);
            String amount = "";
            try {
                amount = String.format("%,d", Integer.parseInt(table.getAmount()));
            } catch (Exception ignored){

            }

            amountTextView.setText(amount);

            TextView employeeNameTextView = v.findViewById(R.id.employeeNameTextView);
            employeeNameTextView.setText(table.getOpenTableEmp());

            TextView timeTextView = v.findViewById(R.id.timeTextView);
            String openDateTime = "";
            try {
                openDateTime += table.getOpenTime().substring(8, 10) + ":" + table.getOpenTime().substring(10, 12);
            } catch (Exception ignored) {

            }
            timeTextView.setText(openDateTime);

            final LinearLayout listTableLayout = v.findViewById(R.id.listTableLayout);

            String status = table.getStatus();
            if ("on".equals(status)) {
                listTableLayout.setBackground(context.getResources().getDrawable(R.drawable.corner_grid_second_color));
            } else {
                employeeNameTextView.setText(v.getResources().getString(R.string.onlyOffTable));
                listTableLayout.setBackground(context.getResources().getDrawable(R.drawable.corner_grid_main_color));
            }

            listTableLayout.setPadding(2,2,2,2);
        }


        return v;
    }
}
