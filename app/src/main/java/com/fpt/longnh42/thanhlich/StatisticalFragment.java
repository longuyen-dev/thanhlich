package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.fpt.longnh42.thanhlich.common.CommonUtil;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Invoice;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.rowAdapter.RowInvoiceAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class StatisticalFragment extends Fragment {

    private View mRootView;
    private EditText datePickerEditText;
    private Button todayButton, yesterdayButton;
    private GridView invoiceGridView;
    private DatePickerDialog datePickerDialog;
    private TextView amountTextView;


    private Map<String, Invoice> invoiceMap;

    private List<Order> listDetailIvl;

    private String selectedDate;

    private DatabaseReference invoiceRef;

    private RowInvoiceAdapter invoiceAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_statistical_fragment, container,
                false);

        initView();
        return mRootView;
    }

    private void initView() {
        // Find element
        datePickerEditText = mRootView.findViewById(R.id.datePickerEditText);
        todayButton = mRootView.findViewById(R.id.todayButton);
        yesterdayButton = mRootView.findViewById(R.id.yesterdayButton);
        invoiceGridView = mRootView.findViewById(R.id.invoiceGridView);
        amountTextView = mRootView.findViewById(R.id.amountTextView);

        // Set list
        invoiceMap = new HashMap<>();

        // Set Adapter
        invoiceAdapter = new RowInvoiceAdapter(invoiceMap);
        invoiceGridView.setAdapter(invoiceAdapter);

        // Set reference
        invoiceRef = Reference.getInvoiceRef();

        // Set Listener
        datePickerEditText.setOnClickListener(datePickerEvent());
        invoiceGridView.setOnItemClickListener(selectItemEvent());
        todayButton.setOnClickListener(todayEvent());
        yesterdayButton.setOnClickListener(yesterdayEvent());

        // Get today invoice
        getTodayInvoice();

    }

//    private void getDetailIvl(String deta){
//        DatabaseReference detailIvlRef = Reference.getDetailInvoiceRef();
//    }


    private void getTodayInvoice() {
        String today = new CommonUtil().getSystemDate();
        String y = today.substring(0, 4);
        String m = today.substring(4, 6);
        String d = today.substring(6, 8);
        selectedDate = y + "" + m + "" + d + "";
        datePickerEditText.setText(d + "/" + m + "/" + y);
        invoiceRef.child(selectedDate).addListenerForSingleValueEvent(getInvoiceEvent());
    }

    private View.OnClickListener datePickerEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String today = new CommonUtil().getSystemDate();
                int y = Integer.valueOf(today.substring(0, 4));
                int m = Integer.valueOf(today.substring(4, 6));
                int d = Integer.valueOf(today.substring(6, 8));
                datePickerDialog = new DatePickerDialog(mRootView.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String day = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) {
                            day = "0" + day;
                        }
                        String monthString = String.valueOf(month + 1);
                        if (month + 1 < 10) {
                            monthString = "0" + monthString;
                        }
                        selectedDate = year + "" + monthString + "" + day + "";
                        datePickerEditText.setText(day + "/" + monthString + "/" + year);
                        invoiceRef.child(selectedDate).addListenerForSingleValueEvent(getInvoiceEvent());
                    }
                }, y, m - 1, d);
                datePickerDialog.show();
            }
        };
    }

    private View.OnClickListener todayEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String today = new CommonUtil().getSystemDate();
                int y = Integer.valueOf(today.substring(0, 4));
                int m = Integer.valueOf(today.substring(4, 6));
                int d = Integer.valueOf(today.substring(6, 8));
                selectedDate = y + "" + m + "" + d + "";
                datePickerEditText.setText(d + "/" + m + "/" + y);
                invoiceRef.child(selectedDate).addListenerForSingleValueEvent(getInvoiceEvent());
            }
        };
    }

    private View.OnClickListener yesterdayEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDay = datePickerEditText.getText().toString();
                int y = Integer.valueOf(selectedDay.substring(6, 10));
                int m = Integer.valueOf(selectedDay.substring(3, 5));
                int d = Integer.valueOf(selectedDay.substring(0, 2)) - 1;

                if (d == 0) {
                    d = 31;
                    m = m - 1;
                }

                if (m == 0) {
                    m = 12;
                    y = y - 1;
                }
                String day = "";
                if (d < 10) {
                    day = "0" + d;
                } else {
                    day = d + "";
                }

                String month = "";
                if (m < 10) {
                    month = "0" + m;
                } else {
                    month = m + "";
                }

                selectedDate = y + "" + month + "" + day+ "";
                datePickerEditText.setText(day + "/" + month + "/" + y);
                invoiceRef.child(selectedDate).addListenerForSingleValueEvent(getInvoiceEvent());
            }
        };
    }

    private ValueEventListener getInvoiceEvent() {
        invoiceMap.clear();
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int amount = 0;
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Invoice invoice = child.getValue(Invoice.class);
                    invoiceMap.put(child.getKey(), invoice);
                    if (invoice.getAmount() == null) {
                        amount += 0;
                    } else {
                        amount += Integer.valueOf(invoice.getAmount());
                    }
                }
                amountTextView.setText(mRootView.getResources().getString(R.string.amount)+": "
                        +String.format("%,d", amount));
                invoiceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private AdapterView.OnItemClickListener selectItemEvent() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Invoice> sortedMap = new TreeMap<>(invoiceMap);
                Invoice selectedInvoice = (Invoice) sortedMap.values().toArray()[position];
                String invoiceID = (String) sortedMap.keySet().toArray()[position];
                Intent gotoDetail = new Intent(mRootView.getContext(), DetailInvoiceActivity.class);
                gotoDetail.putExtra("invoiceId", invoiceID);
                startActivity(gotoDetail);
//                System.out.println(selectedInvoice.getAmount() +" - "+selectedInvoice.getPaymentDate() + " - " +invoiceID);
            }
        };
    }

}
