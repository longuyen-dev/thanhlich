package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText itemPriceEditText;
    private Spinner itemCategorySpinner;
    private Button addItemButton;

    private List<String> listCategory;
    ArrayAdapter<String> listCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initView();
    }

    private void initView() {
        // define element
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemPriceEditText = findViewById(R.id.itemPriceEditText);
        itemCategorySpinner = findViewById(R.id.itemCategorySpinner);
        addItemButton = findViewById(R.id.addItemButton);
        listCategory = new ArrayList<>();

        // Create Adapter
        listCategoryAdapter = new ArrayAdapter<>
                (this, R.layout.spinner_item,
                        listCategory);
        listCategoryAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        itemCategorySpinner.setAdapter(listCategoryAdapter);


        // Get list category
        DatabaseReference categoryRef = Reference.getCategoryRef();
        categoryRef.addListenerForSingleValueEvent(getListCategoryEvent());

        // Set Listener
        addItemButton.setOnClickListener(addItemEvent());
    }

    private ValueEventListener getListCategoryEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    listCategory.add((String) childData.getValue());
                }
                listCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private View.OnClickListener addItemEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemCategory = itemCategorySpinner.getSelectedItem().toString();
                String itemName = itemNameEditText.getText().toString();
                String itemPrice = itemPriceEditText.getText().toString();

                if ("".equals(itemCategory) || "".equals(itemName) || "".equals(itemPrice)) {
                    Toast.makeText(AddItemActivity.this,
                            getResources().getString(R.string.notEmpty), Toast.LENGTH_SHORT).show();
                } else {
                    int itemPriceInteger = 0;
                    try {
                        itemPriceInteger = Integer.parseInt(itemPrice);
                    } catch (Exception e){}

                    Item newItem = new Item();
                    newItem.setPrice(String.valueOf(itemPriceInteger));
                    newItem.setItemName(itemName);
                    newItem.setCategory(itemCategory);

                    DatabaseReference itemRef = Reference.getItemRef();
                    itemRef.child(itemName).child("itemName").setValue(itemName);
                    itemRef.child(itemName).child("category").setValue(itemCategory);
                    itemRef.child(itemName).child("price").setValue(String.valueOf(itemPriceInteger));
                    Toast.makeText(AddItemActivity.this,
                            getResources().getString(R.string.addSuccess), Toast.LENGTH_SHORT).show();

                    itemNameEditText.setText("");
                    itemPriceEditText.setText("");
                }
            }
        };
    }
}
