package com.fpt.longnh42.thanhlich.rowAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.OrderActivity;
import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.object.Item;

import java.util.List;

public class RowOrderAdapter extends ArrayAdapter<Item> {

    private OrderActivity orderActivity;
    private Button itemPlusButton;
    private Button itemMinusButton;
    public RowOrderAdapter(@NonNull Context context, int resource, @NonNull List<Item> objects) {
        super(context, resource, objects);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_row_order_adapter,null);

        }
        Item item = getItem(position);
        if (item != null) {
            TextView drinkName = v.findViewById(R.id.itemNameTextView);
            drinkName.setText(item.getItemName());

            TextView drinkPrice = v.findViewById(R.id.itemPriceTextView);
            int price;
            try {
                price = Integer.parseInt(item.getPrice());
            }catch (Exception e) {
                price = 0;
            }
            drinkPrice.setText(String.format("%,d", price));

            TextView drinkQuantity = v.findViewById(R.id.itemQuantityTextView);
            drinkQuantity.setText(item.getQuantum());

            itemPlusButton = v.findViewById(R.id.itemPlusButton);
            itemPlusButton.setOnClickListener(updateListOrder(item.getItemName()));

            itemMinusButton = v.findViewById(R.id.itemMinusButton);
            itemMinusButton.setOnClickListener(updateListOrder(item.getItemName()));

        }
        return v;
    }
    private View.OnClickListener updateListOrder(final String nameItem) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(v.getId());
                if (v.getId() == itemPlusButton.getId()) {
                    orderActivity.updateOrderList("plus", nameItem);
                } else {
                    orderActivity.updateOrderList("minus", nameItem);
                }
            }
        };
    }

    public void setOrderActivity(OrderActivity orderActivity) {
        this.orderActivity = orderActivity;
    }
}
