package com.fpt.longnh42.thanhlich.rowAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Item;

import java.util.ArrayList;
import java.util.List;

public class RowItemAdapter extends BaseAdapter {

    private List<Item> listItem;

    public RowItemAdapter(List<Item> listItem) {
        this.listItem = listItem;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Item getItem(int position) {
        return listItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        v = inflater.inflate(R.layout.activity_row_item_adapter,null);
        Item item = getItem(position);
        TextView itemNameTextView = v.findViewById(R.id.itemNameTextView);
        itemNameTextView.setText(item.getItemName());

        TextView priceTextView = v.findViewById(R.id.priceTextView);
        int price;
        try {
            price = Integer.parseInt(item.getPrice());
        }catch (Exception e) {
            price = 0;
        }
        priceTextView.setText(String.format("%,d", price));

        return v;
    }
}
