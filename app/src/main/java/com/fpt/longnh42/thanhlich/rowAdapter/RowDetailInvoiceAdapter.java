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
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.object.Order;

import java.util.List;

public class RowDetailInvoiceAdapter extends ArrayAdapter<Order> {

    public RowDetailInvoiceAdapter(@NonNull Context context, int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_row_detail_invoice_adapter,null);

        }
        Order order = getItem(position);
        if (order != null) {
            TextView drinkName = v.findViewById(R.id.itemNameTextView);
            drinkName.setText(order.getItemName());

            TextView drinkPrice = v.findViewById(R.id.itemPriceTextView);
            int price;
            try {
                price = Integer.parseInt(order.getAmount());
            }catch (Exception e) {
                price = 0;
            }
            drinkPrice.setText(String.format("%,d", price));

            TextView drinkQuantity = v.findViewById(R.id.itemQuantityTextView);
            drinkQuantity.setText(order.getQuantum());

        }
        return v;
    }
}
