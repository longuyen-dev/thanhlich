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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.EmployeeFragment;
import com.fpt.longnh42.thanhlich.R;

import java.util.List;

public class RowEmpAdapter extends ArrayAdapter<String> {

    List<String> listEmployee;


    public RowEmpAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        listEmployee = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_row_emp_adapter,null);
        }
        TextView employeeNameTextView = v.findViewById(R.id.employeeNameTextView);
        employeeNameTextView.setText(listEmployee.get(position));
        return v;
    }
}
