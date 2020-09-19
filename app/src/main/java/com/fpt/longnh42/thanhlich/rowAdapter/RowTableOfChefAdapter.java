package com.fpt.longnh42.thanhlich.rowAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Table;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RowTableOfChefAdapter extends BaseAdapter {
    private Map<Integer, String> mapTable;
    private Map<Integer, String> sortedMap;

    public RowTableOfChefAdapter(Map<Integer, String> mapTable) {
        this.mapTable = mapTable;
    }

    @Override
    public int getCount() {
        if (mapTable.size() != 0) {
            sortedMap = new TreeMap<>(mapTable);
        }
        return mapTable.size();
    }

    @Override
    public String getItem(int position) {
        int tableName = (int) sortedMap.keySet().toArray()[position];
        return String.valueOf(tableName);
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
        v = inflater.inflate(R.layout.activity_row_table_chef_adapter,null);
        TextView tableNameTextView = v.findViewById(R.id.tableNameTextView);
        tableNameTextView.setText(getItem(position));
        String status = (String) sortedMap.values().toArray()[position];
        RelativeLayout tableNameRelativeLayout = v.findViewById(R.id.tableNameRelativeLayout);
        if ("on".equals(status)) {
            tableNameRelativeLayout.setBackground(context.getResources().getDrawable(R.drawable.corner_grid_second_color));
        } else {
            tableNameRelativeLayout.setBackground(context.getResources().getDrawable(R.drawable.corner_grid_main_color));
        }
        tableNameRelativeLayout.setPadding(10,5,10,5);
        return v;
    }
}
