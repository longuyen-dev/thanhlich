package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View navHeaderView;
    private TextView employeeNameTextView;

    private SharedPreferences checkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLogin = getSharedPreferences("checkLogin", MODE_PRIVATE);

        if (checkLogin.contains("empName")) {
            initView();
            if (savedInstanceState == null) {
                employeeNameTextView.setText(checkLogin.getString("empName",""));
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,
                        new TableFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_table);
            }
        } else {
            logout();
        }
        // TEST PRINT


    }

    public void initView() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header);
        employeeNameTextView = navHeaderView.findViewById(R.id.employeeNameTextView);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        displayView(menuItem.getItemId());
        return true;
    }
    public void displayView(int viewId) {

        Fragment fragment = null;

        switch (viewId) {
            case R.id.nav_table:
                fragment = new TableFragment();
                navigationView.setCheckedItem(R.id.nav_table);
                break;
//            case R.id.nav_chef:
//                fragment = new ChefFragment();
//                navigationView.setCheckedItem(R.id.nav_chef);
//                break;
            case R.id.nav_statistical:
                fragment = new StatisticalFragment();
                navigationView.setCheckedItem(R.id.nav_statistical);
                break;
            case R.id.nav_employee:
                if ("admin".equals(checkLogin.getString("rule",""))){
                    fragment = new EmployeeFragment();
                    navigationView.setCheckedItem(R.id.nav_employee);
                } else {
                    Toast.makeText(this, getString(R.string.notRule), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.nav_item:
                if ("admin".equals(checkLogin.getString("rule",""))){
                    fragment = new ItemFragment();
                    navigationView.setCheckedItem(R.id.nav_item);
                } else {
                    Toast.makeText(this, getString(R.string.notRule), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_layout, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }
    private void logout() {
        checkLogin.edit().clear().commit();
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(login);
    }

}
