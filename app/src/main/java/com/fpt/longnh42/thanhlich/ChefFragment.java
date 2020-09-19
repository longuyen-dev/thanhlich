package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.rowAdapter.RowChefAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChefFragment extends Fragment  {
    private View mRootView;
    private Map<String, Order> allOrderMap;
    private Map<String, Order> orderToGridViewMap;
    private Map<String, Order> treemap;
    private DatabaseReference orderedRef;
    private RowChefAdapter rowChefAdapter;

    private TextView chefOptionTextView;
    private GridView chefGridView;
    private Button allChefButton, onlyNewsButton, onlyDoneButton;

    // show list ordered option
    // 0: all ordered
    // 1: only news ordered
    // 2: only done ordered
    private int orderedOption = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_chef_fragment, container,
                false);

        initView();
        return mRootView;
    }

    private void initView() {
        // Find element
        chefOptionTextView = mRootView.findViewById(R.id.chefOptionTextView);
        chefGridView = mRootView.findViewById(R.id.chefGridView);
        allChefButton = mRootView.findViewById(R.id.allChefButton);
        onlyNewsButton = mRootView.findViewById(R.id.onlyNewsButton);
        onlyDoneButton = mRootView.findViewById(R.id.onlyDoneButton);

        // Define element
        allOrderMap = new HashMap<>();
        orderToGridViewMap = new HashMap<>();
        treemap = new TreeMap<>(Collections.reverseOrder());
        orderedRef = Reference.getOrderedRef();
        rowChefAdapter = new RowChefAdapter(treemap);

        // Set adapter
        chefGridView.setAdapter(rowChefAdapter);

        // Set listener
        orderedRef.addChildEventListener(getAllOrder());
        chefGridView.setOnItemClickListener(selectOrderEvent());
    }

    private ChildEventListener getAllOrder() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    Order order = childData.getValue(Order.class);
                    allOrderMap.put(childData.getKey(), order);
                    treemap.putAll(allOrderMap);
                    rowChefAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    Order order = childData.getValue(Order.class);
                    allOrderMap.put(childData.getKey(), order);
                    treemap.putAll(allOrderMap);
                    rowChefAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childData: dataSnapshot.getChildren()) {
                    Order order = childData.getValue(Order.class);
                    allOrderMap.remove(childData.getKey());
                    treemap.remove(childData.getKey());
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
                Order order = (Order) treemap.values().toArray()[position];
                String orderID = (String) treemap.keySet().toArray()[position];

                if ("".equals(order.getStatus())) {
                    orderedRef.child(order.getTableName()).child(orderID).child("status").setValue("done");
                } else {
                    orderedRef.child(order.getTableName()).child(orderID).child("status").setValue("");
                }
            }
        };
    }

    private View.OnClickListener changeChefOption() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == allChefButton.getId()) {
                    if (orderedOption != 0) {
                        orderedOption = 0;
                    }
                }
            }
        };
    }

}
