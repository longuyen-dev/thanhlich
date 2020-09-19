package com.fpt.longnh42.thanhlich.rowAdapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Order;

import java.util.HashMap;
import java.util.Map;

public class RowChefAdapter extends BaseAdapter {

    private Map<String, Order> orderMap;


    public RowChefAdapter(Map<String, Order> orderMap) {
        this.orderMap = orderMap;
    }

    @Override
    public int getCount() {
        return orderMap.size();
    }

    @Override
    public Order getItem(int position) {
        return (Order) orderMap.values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        v = inflater.inflate(R.layout.activity_row_chef_adapter,null);
        if (orderMap != null || orderMap.size() != 0) {
            Order order = getItem(position);
            TextView itemNameTextView = v.findViewById(R.id.itemNameTextView);
            itemNameTextView.setText(order.getItemName());

            TextView employeeNameTextView = v.findViewById(R.id.employeeNameTextView);
            employeeNameTextView.setText("NV: "+order.getOrderEmp());

            String orderTime = "";
            try {
                orderTime += order.getOrderTime().substring(8, 10) + ":"
                        + order.getOrderTime().substring(10, 12);
            } catch (Exception ignored) {

            }

            TextView timeTextView = v.findViewById(R.id.timeTextView);
            timeTextView.setText(orderTime);

            TextView quantumTextView = v.findViewById(R.id.quantumTextView);
            quantumTextView.setText("SL: "+order.getQuantum());

            TextView tableNameTextView = v.findViewById(R.id.tableNameTextView);
            tableNameTextView.setText(v.getResources().getString(R.string.table)
                    +": "+order.getTableName());

            LinearLayout listOrderLayout = v.findViewById(R.id.listOrderLayout);
            String status = order.getStatus();
            if ("done".equals(status)) {
                listOrderLayout.setBackground(context.getResources()
                        .getDrawable(R.drawable.corner_grid_second_color));
            }
            if (0 > Integer.valueOf(order.getQuantum())) {
                listOrderLayout.setBackground(context.getResources()
                        .getDrawable(R.drawable.corner_grid_delete_color));
            }
            listOrderLayout.setPadding(2,2,2,2);
        }
        return v;
    }
}
