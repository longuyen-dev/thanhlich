package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Employee;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText employeeNameEditText, passwordEditText;
    Button loginButton;
    DatabaseReference empRef;
    private SharedPreferences checkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkLogin = getSharedPreferences("checkLogin", MODE_PRIVATE);
        initView();
    }
    private void initView() {
        // Find Element
        employeeNameEditText = findViewById(R.id.employeeNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Set reference
        empRef = Reference.getEmployeeRef();

        // Set listener
        loginButton.setOnClickListener(loginHandle());
    }

    private View.OnClickListener loginHandle() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String empName = employeeNameEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                if ("".equals(empName) || "".equals(password)){

                } else {
                    empRef.child(empName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() <= 0) {
                                Toast.makeText(LoginActivity.this,
                                        getString(R.string.invalidEmp), Toast.LENGTH_SHORT).show();
                            } else {
                                Employee employee = dataSnapshot.getValue(Employee.class);
                                if (password.equals(employee.getPassword())) {
                                    loginSuccessHandle(empName, employee.getRule());
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            getString(R.string.invalidPass), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        };
    }
    private void loginSuccessHandle(String empName, String rule) {
        Intent gotoMain = new Intent(LoginActivity.this, MainActivity.class);
        SharedPreferences.Editor ed = checkLogin.edit();
        ed.putString("empName",empName).apply();
        ed.putString("rule",rule).apply();
        startActivity(gotoMain);
    }
    @Override
    public void onBackPressed() { }
}
