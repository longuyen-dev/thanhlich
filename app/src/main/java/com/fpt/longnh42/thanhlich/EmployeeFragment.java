package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Employee;
import com.fpt.longnh42.thanhlich.rowAdapter.RowEmpAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployeeFragment extends Fragment {

    private View mRootView;
    private ListView employeeListView;
    private List<String> empList;
    private List<Employee> empInfoList;
    private RowEmpAdapter empAdapter;
    private EditText employeeNameEditText, passwordEditText;
    private Button changeButton;

    private DatabaseReference empRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_employee_fragment, container, false);
        initView();
        return mRootView;
    }
    private void initView() {
        // Find element
        employeeListView = mRootView.findViewById(R.id.employeeListView);
        employeeNameEditText = mRootView.findViewById(R.id.employeeNameEditText);
        passwordEditText = mRootView.findViewById(R.id.passwordEditText);
        changeButton = mRootView.findViewById(R.id.changeButton);

        empInfoList = new ArrayList<>();
        empList = new ArrayList<>();
        empAdapter = new RowEmpAdapter(mRootView.getContext(),R.layout.activity_row_emp_adapter,
                empList);
        employeeListView.setAdapter(empAdapter);

        empRef = Reference.getEmployeeRef();
        // Add listener

        refreshEmpData();
        employeeListView.setOnItemClickListener(selectEmpEvent());
        employeeListView.setOnItemLongClickListener(deleteEmployeeEvent());
        changeButton.setOnClickListener(changeEmpInfoEvent());

    }

    private void refreshEmpData() {
        empRef.addListenerForSingleValueEvent(getEmpEvent());
    }

    private ValueEventListener getEmpEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                empInfoList.clear();
                empList.clear();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    empList.add(child.getKey());
                    Employee employee = child.getValue(Employee.class);
                    empInfoList.add(employee);
                }
                empAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private AdapterView.OnItemClickListener selectEmpEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                employeeNameEditText.setText(empList.get(position));
                passwordEditText.setText(empInfoList.get(position).getPassword());
            }
        };
    }

    public AdapterView.OnItemLongClickListener deleteEmployeeEvent() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String emp = empList.get(position);
                if (!mRootView.getResources().getString(R.string.admin).equals(emp)) {
                    empRef.child(emp).removeValue();
                    refreshEmpData();
                }
                return true;
            }
        };
    }

    private View.OnClickListener changeEmpInfoEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = passwordEditText.getText().toString();
                String emp = employeeNameEditText.getText().toString();
                if ("".equals(pwd) || "".equals(emp)) {
                    Toast.makeText(mRootView.getContext(),
                            mRootView.getResources().getString(R.string.notEmpty), Toast.LENGTH_SHORT).show();
                } else {
                    if (mRootView.getResources().getString(R.string.admin).equals(emp)) {
                        empRef.child(emp).child("password").setValue(pwd);
                        empRef.child(emp).child("rule").setValue("admin");
                    } else {
                        Employee employee = new Employee();
                        employee.setPassword(pwd);
                        employee.setRule("nv");
                        empRef.child(emp).setValue(employee);
                    }
                    refreshEmpData();
                }
            }
        };
    }
}
