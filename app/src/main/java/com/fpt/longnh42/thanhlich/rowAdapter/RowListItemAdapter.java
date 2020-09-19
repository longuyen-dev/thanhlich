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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Item;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class RowListItemAdapter extends ArrayAdapter<Item> {
    public RowListItemAdapter(@NonNull Context context, int resource, @NonNull List<Item> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_row_list_item_adapter,null);
        }
        final Item item = getItem(position);
        if (item != null) {
            TextView itemNameTextView = v.findViewById(R.id.itemNameTextView);
            itemNameTextView.setText(item.getItemName());

            final EditText priceEditText = v.findViewById(R.id.priceEditText);
            priceEditText.setText(item.getPrice());

            Button saveButton = v.findViewById(R.id.saveButton);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newPrice = priceEditText.getText().toString();
                    DatabaseReference itemRef = Reference.getItemRef();
                    itemRef.child(item.getItemName()).child("price").setValue(newPrice);
                }
            });
        }
        return v;

    }
}
