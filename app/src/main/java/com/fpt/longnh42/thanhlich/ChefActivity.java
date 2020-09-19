package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.object.Table;
import com.fpt.longnh42.thanhlich.rowAdapter.RowChefAdapter;
import com.fpt.longnh42.thanhlich.rowAdapter.RowTableOfChefAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChefActivity extends AppCompatActivity {
    private Map<String, Order> allOrderMap;
    private Map<String, Order> orderToGridViewMap;
    private DatabaseReference orderedRef;
    private DatabaseReference tableRef;
    private RowChefAdapter rowChefAdapter;

    private GridView tableGridView;
    private RowTableOfChefAdapter rowTableOfChefAdapter;

    private Map<Integer, String> mapTable;

    private TextView chefOptionTextView;
    private GridView chefGridView;

    private LinearLayout gridTableLayout;

    private String tableNameSelected = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef);
        initView();
    }

    private void initView() {
        // Find Element
        tableGridView = findViewById(R.id.tableGridView);
        chefOptionTextView = findViewById(R.id.chefOptionTextView);
        chefGridView = findViewById(R.id.chefGridView);
        gridTableLayout = findViewById(R.id.gridTableLayout);

        // Define Element
        mapTable = new HashMap<>();
        allOrderMap = new HashMap<>();
        orderToGridViewMap = new HashMap<>();
        orderedRef = Reference.getOrderedRef();
        tableRef = Reference.getTableRef();

        // Create value for list table
        setListTable();

        rowTableOfChefAdapter = new RowTableOfChefAdapter(mapTable);
        rowChefAdapter = new RowChefAdapter(orderToGridViewMap);

        // Set adapter
        chefGridView.setAdapter(rowChefAdapter);
        tableGridView.setAdapter(rowTableOfChefAdapter);

        // Set Listener
        tableRef.orderByChild("status").equalTo("on").addListenerForSingleValueEvent(getTableInfoEvent());
        orderedRef.addChildEventListener(getAllOrder());
        chefGridView.setOnItemClickListener(selectOrderEvent());
        tableGridView.setOnItemClickListener(changeTableOption());
    }

    private ChildEventListener getAllOrder() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    Order order = childData.getValue(Order.class);
                    allOrderMap.put(childData.getKey(), order);
                    if (tableNameSelected.equals(order.getTableName())
                            || "0".equals(tableNameSelected)) {
                        orderToGridViewMap.put(childData.getKey(), order);
                    }
                    if (!mapTable.containsValue(order.getTableName())) {
                        mapTable.put(Integer.parseInt(order.getTableName()),"on");
                        updateTableInfo();
                        rowTableOfChefAdapter.notifyDataSetChanged();
                    }
                    rowChefAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    Order order = childData.getValue(Order.class);
                    allOrderMap.put(childData.getKey(), order);
                    if (tableNameSelected.equals(order.getTableName())
                            || "0".equals(tableNameSelected)) {
                        orderToGridViewMap.put(childData.getKey(), order);
                    }
                    rowChefAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    allOrderMap.remove(childData.getKey());
                    Order order = childData.getValue(Order.class);
                    if (tableNameSelected.equals(order.getTableName())
                            || "0".equals(tableNameSelected)) {
                        orderToGridViewMap.remove(childData.getKey());
                    }
                    rowChefAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private AdapterView.OnItemClickListener selectOrderEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = (Order) orderToGridViewMap.values().toArray()[position];
                String orderID = (String) orderToGridViewMap.keySet().toArray()[position];

                if ("".equals(order.getStatus())) {
                    orderedRef.child(order.getTableName()).child(orderID).child("status").setValue("done");
                } else {
                    orderedRef.child(order.getTableName()).child(orderID).child("status").setValue("");
                }
            }
        };
    }

    private void setListTable() {
        mapTable.put(0, "on");
    }

    private ValueEventListener getTableInfoEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    System.out.println(childData);
                    Table table = childData.getValue(Table.class);
                    mapTable.put(Integer.valueOf(childData.getKey()), table.getStatus());
                    rowTableOfChefAdapter.notifyDataSetChanged();
                }
                updateTableInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void updateTableInfo() {
        int sumOfTable = mapTable.size();
        final float scale = getResources().getDisplayMetrics().density;
        int width = (int) ((65 * sumOfTable) * scale + 0.5f);

        gridTableLayout.getLayoutParams().width = width;
        gridTableLayout.requestLayout();

    }

    private AdapterView.OnItemClickListener changeTableOption() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                orderToGridViewMap.clear();
                Map<Integer, String> sortedMap = new TreeMap<>(mapTable);
                Integer tableName = (int) sortedMap.keySet().toArray()[position];
                tableNameSelected = String.valueOf(tableName);
                if ("0".equals(tableNameSelected)) {
                    chefOptionTextView.setText(R.string.allTable);
                    orderToGridViewMap.putAll(allOrderMap);
                } else {
                    chefOptionTextView.setText(getString(R.string.table) + tableNameSelected);
                    for (Map.Entry orderValue : allOrderMap.entrySet()) {
                        System.out.println("Key: " + orderValue.getKey() + " & Value: " + orderValue.getValue());
                        Order order = (Order) orderValue.getValue();
                        if (tableNameSelected.equals(order.getTableName())) {
                            orderToGridViewMap.put((String) orderValue.getKey(), order);
                        }
                    } // for loop
                }
                rowChefAdapter.notifyDataSetChanged();
            }
        };
    }
}
