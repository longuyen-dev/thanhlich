package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Table;
import com.fpt.longnh42.thanhlich.rowAdapter.RowTableAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TableFragment extends Fragment {
    private View mRootView;
    private GridView tableGridView;
    private RowTableAdapter tableAdapter;
    private TextView tableOptionTextView;
    private Map<Integer, Table> tableMap;
    private DatabaseReference tableRef;
    private ChildEventListener getTableDataEventListener;
    private Button onlyOnTableButton, allTableButton, chefButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_table_fragment, container, false);
        initView();
        return mRootView;
    }
    @SuppressLint("UseSparseArrays")
    private void initView() {
        tableGridView = mRootView.findViewById(R.id.tableGridView);
        tableOptionTextView = mRootView.findViewById(R.id.tableOptionTextView);
        onlyOnTableButton = mRootView.findViewById(R.id.onlyOnTableButton);
        allTableButton = mRootView.findViewById(R.id.allTableButton);
        chefButton = mRootView.findViewById(R.id.chefButton);

        tableMap = new HashMap<>();
        tableAdapter = new RowTableAdapter(tableMap);
        tableRef = Reference.getTableRef();
        getTableDataEventListener = getTableData();
        tableRef.addChildEventListener(getTableDataEventListener);

        tableGridView.setAdapter(tableAdapter);

        // set listener for change option button
        changeOptionTable();
        tableGridView.setOnItemClickListener(gotoOrderEvent());
        chefButton.setOnClickListener(gotoChefActivity());
    }
    private ChildEventListener getTableData() {

        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Table table = dataSnapshot.getValue(Table.class);
                tableMap.put(Integer.valueOf(Objects.requireNonNull(dataSnapshot.getKey())), table);
                tableAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Table table = dataSnapshot.getValue(Table.class);
                tableMap.put(Integer.valueOf(Objects.requireNonNull(dataSnapshot.getKey())), table);
                tableAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void changeOptionTable() {
        View.OnClickListener changeOptionTable = new View.OnClickListener() {
            @SuppressLint("UseSparseArrays")
            @Override
            public void onClick(View v) {
                tableMap = new HashMap<>();
                tableAdapter = new RowTableAdapter(tableMap);
                tableGridView.setAdapter(tableAdapter);
                tableRef.removeEventListener(getTableDataEventListener);
                getTableDataEventListener = getTableData();
                if (v == allTableButton) {
                    onlyOnTableButton.setBackground(mRootView.getResources().getDrawable(R.drawable.corner_grid_main_color));
                    tableRef.addChildEventListener(getTableDataEventListener);
                } else {
                    allTableButton.setBackground(mRootView.getResources().getDrawable(R.drawable.corner_grid_main_color));
                    tableRef.orderByChild("status").equalTo("on").
                            addChildEventListener(getTableDataEventListener);
                }
                Button b = (Button) v;
                tableOptionTextView.setText(b.getText().toString());
                v.setBackground(mRootView.getResources().getDrawable(R.drawable.corner_grid_second_color));
            }
        };
        onlyOnTableButton.setOnClickListener(changeOptionTable);
        allTableButton.setOnClickListener(changeOptionTable);
    }

    private AdapterView.OnItemClickListener gotoOrderEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent gotoOrder = new Intent(mRootView.getContext(), OrderActivity.class);
                Map<Integer, Table> sortedMap = new TreeMap<>(tableMap);
                Table selectedTable = (Table) sortedMap.values().toArray()[position];
                int tableName = (Integer) sortedMap.keySet().toArray()[position];
                gotoOrder.putExtra("employeeName", selectedTable.getOpenTableEmp());
                gotoOrder.putExtra("tableName", tableName);
                gotoOrder.putExtra("amount", selectedTable.getAmount());
                gotoOrder.putExtra("quantum", selectedTable.getQuantum());
                gotoOrder.putExtra("status", selectedTable.getStatus());
                startActivity(gotoOrder);
            }
        };
    }

    private View.OnClickListener gotoChefActivity() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoChef = new Intent(mRootView.getContext(), ChefActivity.class);
                startActivity(gotoChef);
            }
        };
    }
}
