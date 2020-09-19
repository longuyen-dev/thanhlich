package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.CommonUtil;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.object.Table;
import com.fpt.longnh42.thanhlich.rowAdapter.RowCategoryAdapter;
import com.fpt.longnh42.thanhlich.rowAdapter.RowItemAdapter;
import com.fpt.longnh42.thanhlich.rowAdapter.RowOrderAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Ref;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OrderActivity extends AppCompatActivity {
    private Button orderingButton, orderedButton;
    private Button saveButton, saveAndPrint, payButton, editButton;
    private TextView orderTableNameTextView, orderEmployeeNameTextView;
    private TextView quantumTextView, amountTextView, searchItemEditText;
    private ListView orderListView, categoryListView;

    private GridView listItemGridView;
    private LinearLayout buttonAreaLinearLayout;

    // Reference
    private DatabaseReference categoryRef;
    private DatabaseReference itemRef;
    private DatabaseReference orderRef;
    private DatabaseReference tableRef;

    // List
    private List<String> listCategory;
    private List<Item> listItem;
    private List<Item> listItemToListView;

    private List<Item> listOrdering;
    private List<Item> listOrdered;

    // Adapter
    private RowCategoryAdapter categoryAdapter;
    private RowItemAdapter itemAdapter;
    private RowOrderAdapter orderedAdapter;
    private RowOrderAdapter orderingAdapter;

    // Event
    private ValueEventListener getItemEvent;

    // Employee name
    private SharedPreferences checkLogin;
    private String employeeName;

    // Table name
    private int tableName = -1;
    private String status = "";

    // Library
    private CommonUtil commonUtil = new CommonUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        checkLogin = getSharedPreferences("checkLogin", MODE_PRIVATE);
        employeeName = checkLogin.getString("empName","lich");
        initView();

    }
    @SuppressLint("SetTextI18n")
    private void initView() {
        Intent i = getIntent();

        // Start set table information
        orderTableNameTextView = findViewById(R.id.orderTableNameTextView);
        tableName = i.getIntExtra("tableName",0);
        status = i.getStringExtra("status");
        orderTableNameTextView.setText(getString(R.string.table)+": "+tableName);

        orderEmployeeNameTextView = findViewById(R.id.orderEmployeeNameTextView);
        String employeeName = i.getStringExtra("employeeName");
        orderEmployeeNameTextView.setText(getString(R.string.employee)+": "+employeeName);

        quantumTextView = findViewById(R.id.quantumTextView);
        String quantum = i.getStringExtra("quantum");
        int quantumInt;
        try {
            quantumInt = Integer.parseInt(quantum);
        }catch (Exception e) {
            quantumInt = 0;
        }
        quantumTextView.setText(getString(R.string.quantum)+": "+quantumInt);

        amountTextView = findViewById(R.id.amountTextView);
        String amount = i.getStringExtra("amount");
        int amountInt;
        try {
            amountInt = Integer.parseInt(amount);
        }catch (Exception e) {
            amountInt = 0;
        }
        amountTextView.setText(getString(R.string.amount)+": "+String.format("%,d", amountInt));
        searchItemEditText = findViewById(R.id.searchItemEditText);


        // Start find Button
        orderingButton = findViewById(R.id.orderingButton);
        orderedButton = findViewById(R.id.orderedButton);
        saveButton = findViewById(R.id.saveButton);
        saveAndPrint = findViewById(R.id.saveAndPrint);
        payButton = findViewById(R.id.payButton);
        editButton = findViewById(R.id.editButton);
        orderingButton = findViewById(R.id.orderingButton);
        orderedButton = findViewById(R.id.orderedButton);


        // Find List View
        orderListView = findViewById(R.id.orderListView);
        categoryListView = findViewById(R.id.categoryListView);
        listItemGridView = findViewById(R.id.listItemGridView);

        // Find Layout
        buttonAreaLinearLayout = findViewById(R.id.buttonAreaLinearLayout);

        // Create List
        listCategory = new ArrayList<>();
        listItem = new ArrayList<>();
        listItemToListView = new ArrayList<>();
        listOrdered = new ArrayList<>();
        listOrdering = new ArrayList<>();

        // Create Adapter
        itemAdapter = new RowItemAdapter(listItemToListView);
        orderedAdapter = new RowOrderAdapter(this, R.layout.activity_row_order_adapter,
                listOrdered);
        orderedAdapter.setOrderActivity(this);
        orderingAdapter = new RowOrderAdapter(this, R.layout.activity_row_order_adapter,
                listOrdering);
        orderingAdapter.setOrderActivity(this);
        // Set Adapter
        orderListView.setAdapter(orderingAdapter);

        // Set Reference
        categoryRef = Reference.getCategoryRef();
        categoryRef.addListenerForSingleValueEvent(getCategoryEvent());
        tableRef = Reference.getTableRef();
        itemRef = Reference.getItemRef();
        getItemEvent = getItemEvent();
        orderRef = Reference.getOrderedRef();

        // Add Event
        itemRef.addListenerForSingleValueEvent(getItemEvent);
        categoryListView.setOnItemClickListener(changeCategoryEvent());
        searchItemEditText.addTextChangedListener(searchItemEvent());
        listItemGridView.setOnItemClickListener(selectItem());
        saveButton.setOnClickListener(saveOrder());
        orderedButton.setOnClickListener(changeOrderListViewEvent());
        orderingButton.setOnClickListener(changeOrderListViewEvent());
        orderRef.child(String.valueOf(tableName)).addListenerForSingleValueEvent(getOrderedEvent());
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoPayment = new Intent(OrderActivity.this, PaymentActivity.class);
                gotoPayment.putExtra("tableName", String.valueOf(tableName));
                startActivity(gotoPayment);
            }
        });
        editButton.setOnLongClickListener(editOrderedEvent());
        // Set up Button
        changeButtonView();
    }

    private ValueEventListener getCategoryEvent() {

        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                // Create List
                List<String> dataSnapshotValue = new ArrayList<>(map.values());
                listCategory = dataSnapshotValue;
                listCategory.add(0,getString(R.string.allTable));
                // Create Adapter
                categoryAdapter = new RowCategoryAdapter(OrderActivity.this,
                        R.layout.activity_row_category, listCategory);
                // Set Adapter
                categoryListView.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener getItemEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listItem.clear();
                listItemToListView.clear();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    Item item = userSnapshot.getValue(Item.class);
                    listItem.add(item);
                    listItemToListView.add(item);
                }


                // Set Adapter
                listItemGridView.setAdapter(itemAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private AdapterView.OnItemClickListener changeCategoryEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                listItemToListView.clear();
                if (position == 0) {
                    listItemToListView.addAll(listItem);
                } else {
                    for (Item item: listItem) {
                        if (item.getCategory() != null || "".equals(item.getCategory())) {
                            if (item.getCategory().equals(listCategory.get(position))) {
                                listItemToListView.add(item);
                            }
                        }
                    }
                }
                itemAdapter.notifyDataSetChanged();
//                itemRef.removeEventListener(getItemEvent);
//                if (position == 0) {
//                    itemRef.addListenerForSingleValueEvent(getItemEvent);
//                } else {
//                    itemRef.orderByChild("category").equalTo(listCategory.get(position))
//                            .addListenerForSingleValueEvent(getItemEvent);
//                }

            }
        };
    }

    private View.OnClickListener changeOrderListViewEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOrderListView(v);
            }
        };
    }

    private void changeOrderListView(View v) {
        if (v.getId() == orderedButton.getId()) {
            orderingButton.setBackground(getResources().getDrawable(R.drawable.corner_grid_main_color));
            orderingButton.setTextColor(getResources().getColor(R.color.white));
            orderListView.setAdapter(null);
            orderListView.setAdapter(orderedAdapter);
            orderedAdapter.notifyDataSetChanged();
        } else {
            orderedButton.setBackground(getResources().getDrawable(R.drawable.corner_grid_main_color));
            orderedButton.setTextColor(getResources().getColor(R.color.white));
            orderListView.setAdapter(null);
            orderListView.setAdapter(orderingAdapter);
            orderingAdapter.notifyDataSetChanged();
        }
        v.setBackground(getResources().getDrawable(R.drawable.corner_grid_second_color));
        Button clickedButton = (Button) v;
        clickedButton.setTextColor(getResources().getColor(R.color.applicationBackground));
        changeButtonView();
    }

    private void changeButtonView() {
        buttonAreaLinearLayout.removeAllViews();
        if (orderListView.getAdapter().equals(orderedAdapter)) {
            buttonAreaLinearLayout.addView(payButton);
            buttonAreaLinearLayout.addView(editButton);
        } else {
            buttonAreaLinearLayout.addView(saveButton);
            buttonAreaLinearLayout.addView(saveAndPrint);
        }
    }

    private TextWatcher searchItemEvent() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listItemToListView.clear();
                for (Item i: listItem) {
                    String name = commonUtil.removeAccent(i.getItemName());
                    name = name.toLowerCase();
                    if (name.contains(s)) {
                        listItemToListView.add(i);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private AdapterView.OnItemClickListener selectItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = listItemToListView.get(position);
                Item term = new Item();
                term.setPrice(selectedItem.getPrice());
                term.setItemName(selectedItem.getItemName());
                term.setQuantum("1");
                addToOrderList(listOrdering, term);
                orderingAdapter.notifyDataSetChanged();
            }
        };
    }

    private void addToOrderList (List<Item> list, Item selectedItem){
        if (list.size() == 0 || list == null) {
            list.add(selectedItem);
        } else {
            boolean addFlag = false;
            for (Item i: list) {
                String itemNameOfList = i.getItemName();
                String selectedName = selectedItem.getItemName();
                if (selectedName.equals(itemNameOfList)) {
                    addFlag = true;
                    int newPrice = Integer.valueOf(i.getPrice())
                            + Integer.valueOf(selectedItem.getPrice());
                    int newQuantum = Integer.valueOf(i.getQuantum()) + Integer.valueOf(selectedItem.getQuantum());

                    i.setQuantum(String.valueOf(newQuantum));
                    i.setPrice(String.valueOf(newPrice));
                }
            }

            if (!addFlag) list.add(selectedItem);
        }
    }
    public void updateOrderList(String function, String itemName) {
        if (orderListView.getAdapter() == orderedAdapter) {
            changeOrderListView(orderingButton);
            int rawPrice = 0;
            for (Item selectedItem: listItem) {
                if (selectedItem.getItemName().equals(itemName)) {
                    rawPrice = Integer.valueOf(selectedItem.getPrice()) * -1;
                }
            }
            if ("minus".equals(function)) {
                int exitsQuantum = 0;
                for (Item orderedItem: listOrdered) {
                    if (itemName.equals(orderedItem.getItemName())) {
                        exitsQuantum += Integer.valueOf(orderedItem.getQuantum());
                    }
                }


                int exitsQuantumOrdering = exitsQuantum;
                for (Item item: listOrdering) {
                    if (item.getItemName().equals(itemName)) {
                        exitsQuantumOrdering = Integer.valueOf(item.getQuantum());
                    }
                }
                if (Integer.valueOf(exitsQuantumOrdering) == exitsQuantum * -1) {
                    Toast.makeText(this, getString(R.string.cantMinus),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Item term = new Item();
                    term.setPrice(String.valueOf(rawPrice));
                    term.setItemName(itemName);
                    term.setQuantum("-1");
                    addToOrderList(listOrdering, term);
                    orderingAdapter.notifyDataSetChanged();
                }
            } else {
                Item term = new Item();
                term.setPrice(String.valueOf(rawPrice * -1));
                term.setItemName(itemName);
                term.setQuantum("1");
                addToOrderList(listOrdering, term);
                orderingAdapter.notifyDataSetChanged();
            }

        } else {
            if ("plus".equals(function)) {
                for (Item i : listOrdering) {
                    if (itemName.equals(i.getItemName())) {
                        int rawPrice = 0;
                        for (Item selectedItem: listItem) {
                            if (selectedItem.getItemName().equals(itemName)) {
                                rawPrice = Integer.valueOf(selectedItem.getPrice());
                            }
                        }
                        int newPrice = Integer.valueOf(i.getPrice())
                                + rawPrice;
                        int newQuantum = Integer.valueOf(i.getQuantum()) + 1;
                        i.setQuantum(String.valueOf(newQuantum));
                        i.setPrice(String.valueOf(newPrice));
                        orderingAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else {
                for (Item i : listOrdering) {
                    if (itemName.equals(i.getItemName())) {
                        int exitsQuantum = 0;
                        for (Item orderedItem: listOrdered) {
                            if (itemName.equals(orderedItem.getItemName())) {
                                exitsQuantum += Integer.valueOf(orderedItem.getQuantum());
                            }
                        }

                        if (Integer.valueOf(i.getQuantum()) == exitsQuantum * -1) {
//                            listOrdering.remove(i);
                            Toast.makeText(this, getString(R.string.cantMinus),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            int rawPrice = 0;
                            for (Item selectedItem: listItem) {
                                if (selectedItem.getItemName().equals(itemName)) {
                                    rawPrice = Integer.valueOf(selectedItem.getPrice());
                                }
                            }
                            int newPrice = Integer.valueOf(i.getPrice())
                                    - rawPrice;
                            int newQuantum = Integer.valueOf(i.getQuantum()) - 1;
                            i.setQuantum(String.valueOf(newQuantum));
                            i.setPrice(String.valueOf(newPrice));
                        }


                        orderingAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    private View.OnClickListener saveOrder() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference saveOrderRef = Reference.getOrderedRef();
                if (listOrdering.size() != 0 && listOrdering != null) {
                    int index = 1;
                    for (Item i: listOrdering) {
                        if (i.getQuantum().equals("0")) {
                            continue;
                        } else {
                            String invoiceId = commonUtil.getSystemDate2();
                            if (index < 10) {
                                invoiceId = invoiceId + "00" + index;
                            } else {
                                invoiceId = invoiceId + "0" + index;
                            }
                            Order order = new Order();
                            order.setAmount(i.getPrice());
                            order.setItemName(i.getItemName());
                            order.setOrderEmp(employeeName);
                            order.setOrderTime(commonUtil.getSystemDate());
                            order.setQuantum(i.getQuantum());
                            order.setTableName(String.valueOf(tableName));
                            order.setStatus("");
                            saveOrderRef.child(String.valueOf(tableName)).child(invoiceId).setValue(order);
                            index++;
                        }
                    }
                    /**
                     * Update table information
                     * @param: tableName, employeeName, status
                     * */
                    commonUtil.updateTableInfo(String.valueOf(tableName), employeeName, status);
                    onBackPressed();
                }
            }
        };
    }
    private ValueEventListener getOrderedEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    Order order = userSnapshot.getValue(Order.class);
                    Item item = new Item();
                    item.setItemName(order.getItemName());
                    item.setPrice(order.getAmount());
                    item.setQuantum(order.getQuantum());
                    /**
                     * Update table information
                     * @param: list to selectedItem add into, selectedItem
                     * */
                    addToOrderList(listOrdered, item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private View.OnLongClickListener editOrderedEvent() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DatabaseReference editOrderRef = Reference.getOrderedRef();
                editOrderRef.child(String.valueOf(tableName)).removeValue();
                Table table = new Table();
                table.setStatus("off");
                tableRef.child(String.valueOf(tableName)).setValue(table);
                orderRef.child(String.valueOf(tableName)).removeValue();
                onBackPressed();
                return true;
            }
        };
    }
}
