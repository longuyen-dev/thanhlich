package com.fpt.longnh42.thanhlich.rowAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.R;

import java.util.List;

public class RowCategoryAdapter extends ArrayAdapter<String> {

    List<String> listCategory;

    public RowCategoryAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        listCategory = objects;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_row_category,null);
        }
        TextView categoryName = v.findViewById(R.id.categoryNameTextView);
        categoryName.setText(listCategory.get(position));

        return v;
    }
}
