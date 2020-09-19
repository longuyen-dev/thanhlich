package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.fpt.longnh42.thanhlich.common.CommonUtil;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.rowAdapter.RowListItemAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ItemFragment extends Fragment {

    private View mRootView;

    private EditText itemNameSearchEditText;
    private ListView itemListView;
    private Button addItemButton;

    private List<Item> itemList;
    private List<Item> itemListToListView;
    private RowListItemAdapter itemAdapter;

    private DatabaseReference itemRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_item_fragment, container, false);
        initView();
        return mRootView;
    }

    private void initView() {
        // Find Element
        itemNameSearchEditText = mRootView.findViewById(R.id.itemNameSearchEditText);
        itemListView = mRootView.findViewById(R.id.itemListView);
        addItemButton = mRootView.findViewById(R.id.addItemButton);

        // Define
        itemList = new ArrayList<>();
        itemListToListView = new ArrayList<>();
        itemAdapter = new RowListItemAdapter(mRootView.getContext(),
                R.layout.activity_row_list_item_adapter, itemListToListView);
        itemRef = Reference.getItemRef();

        // Set adapter
        itemListView.setAdapter(itemAdapter);

        // Get List
        itemRef.addChildEventListener(getItemListEvent());

        // Set listener
        itemNameSearchEditText.addTextChangedListener(searchItemEvent());
        itemListView.setOnItemClickListener(enableEditTextEvent());
        addItemButton.setOnClickListener(gotoAddItemEvent());
    }
    private ChildEventListener getItemListEvent() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Item item = dataSnapshot.getValue(Item.class);
                itemList.add(item);
                itemListToListView.add(item);
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Item item = dataSnapshot.getValue(Item.class);
                for (Item i: itemListToListView) {
                    if (i.getItemName().equals(item.getItemName())){
                        i.setPrice(item.getPrice());
                    }
                }
                for (Item i: itemList) {
                    if (i.getItemName().equals(item.getItemName())){
                        i.setPrice(item.getPrice());
                    }
                }

                itemAdapter.notifyDataSetChanged();
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
    private TextWatcher searchItemEvent() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                itemListToListView.clear();
                for (Item i: itemList) {
                    String key = CommonUtil.removeAccent(s.toString());
                    String itemName = CommonUtil.removeAccent(i.getItemName());
                    key = key.toLowerCase();
                    itemName = itemName.toLowerCase();
                    if (itemName.contains(key)) {
                        itemListToListView.add(i);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }
        };
    }

    private AdapterView.OnItemClickListener enableEditTextEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        };
    }

    private View.OnClickListener gotoAddItemEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddItem = new Intent(mRootView.getContext(), AddItemActivity.class);
                startActivity(gotoAddItem);
            }
        };
    }
}
