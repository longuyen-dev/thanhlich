package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.CommonUtil;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Invoice;
import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.object.Table;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {
    private Button payButton;
    private TextView amountTextView, exchangeTextView, tableNameTextView;
    private EditText payEditText;
    private SharedPreferences checkLogin;
    private String amount, pay, exchange, tableName, empName;

    private DatabaseReference orderRef;
    private DatabaseReference invoiceRef;
    private DatabaseReference detailIvlRef;
    private DatabaseReference tableRef;

    private List<Order> orderedList;

    private boolean finded = false;

    // bluetooth
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initView();
    }
    private void initView() {
        Intent i = getIntent();

        // Find element
        payButton = findViewById(R.id.payButton);
        amountTextView = findViewById(R.id.amountTextView);
        exchangeTextView = findViewById(R.id.exchangeTextView);
        payEditText = findViewById(R.id.payEditText);
        tableNameTextView = findViewById(R.id.tableNameTextView);


        tableName = i.getStringExtra("tableName");
        checkLogin = getSharedPreferences("checkLogin", MODE_PRIVATE);
        empName = checkLogin.getString("empName","lich");
        tableNameTextView.setText(getString(R.string.table)+ " " + tableName);

        // Set List
        orderedList = new ArrayList<>();

        // Set Reference
        orderRef = Reference.getOrderedRef();
        invoiceRef = Reference.getInvoiceRef();
        detailIvlRef = Reference.getDetailInvoiceRef();
        tableRef = Reference.getTableRef();

        // Set Listener
        orderRef.child(tableName).addListenerForSingleValueEvent(getTableInfoEvent());
        payEditText.addTextChangedListener(changePay());
        payButton.setOnClickListener(paymentEvent());
    }

    private ValueEventListener getTableInfoEvent() {
        return new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int amountInt = 0;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    Order order = userSnapshot.getValue(Order.class);
                    boolean hasFlag = false;
                    for (Order ordered: orderedList) {

                        if (order.getItemName().equals(ordered.getItemName())) {
                            int newQuantum = Integer.valueOf(ordered.getQuantum())
                                    + Integer.valueOf(order.getQuantum());
                            int newAmount = Integer.valueOf(ordered.getAmount())
                                    + Integer.valueOf(order.getAmount());
                            ordered.setQuantum(String.valueOf(newQuantum));
                            ordered.setAmount(String.valueOf(newAmount));
                            hasFlag = true;
                        }

                    }
                    if (!hasFlag) {
                        orderedList.add(order);
                    }

                    amountInt += Integer.parseInt(order.getAmount());
                }
                for (Order ordered: orderedList) {
                    System.out.println(ordered.getItemName() + " : " + ordered.getQuantum() + " : " + ordered.getAmount());
                }

                amount = String.valueOf(amountInt);
                amountTextView.setText(String.format("%,d", amountInt));
                pay = String.valueOf(amountInt);
                payEditText.setText(pay);
                exchangeTextView.setText("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private TextWatcher changePay() {

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                if ("".equals(s) || s.length() == 0){
                    pay = "0";
                } else {
                    pay = payEditText.getText().toString();
                }

                try {
                    int exchangeInt = Integer.valueOf(pay) - Integer.valueOf(amount);
                    exchangeTextView.setText(String.format("%,d", exchangeInt));
                }catch (Exception e) {

                }
            }
        };
    }

    private View.OnClickListener paymentEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil commonUtil = new CommonUtil();
                String toDay = commonUtil.getSystemDate();
                String invoiceId = commonUtil.getSystemDate2();
                Invoice invoice = new Invoice();
                invoice.setAmount(amount);
                invoice.setTableName(tableName);
                invoice.setPaymentDate(toDay);
                invoice.setPaymentEmp(empName);
                // Find bluetooth printer
                try{
                    FindBluetoothDevice();
                    openBluetoothPrinter();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                // START PRINT
                try{
                    printData(invoice);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                if (finded == true) {
                    // Set new Invoice
                    invoiceRef.child(toDay.substring(0, 8)).child(invoiceId).setValue(invoice);
                    // Set detail for new Invoice
                    detailIvlRef.child(invoiceId).setValue(orderedList);
                    // Remove Ordered
                    orderRef.child(tableName).removeValue();
                    // Reset table
                    Table table = new Table();
                    table.setStatus("off");
                    tableRef.child(tableName).setValue(table);
                    // Back to home screen
                    Intent backToHome = new Intent(PaymentActivity.this, MainActivity.class);
                    startActivity(backToHome);
                }else {
                    Toast.makeText(PaymentActivity.this, getString(R.string.notPrinter), Toast.LENGTH_SHORT).show();
                    // Set new Invoice
                    invoiceRef.child(toDay.substring(0, 8)).child(invoiceId).setValue(invoice);
                    // Set detail for new Invoice
                    detailIvlRef.child(invoiceId).setValue(orderedList);
                    // Remove Ordered
                    orderRef.child(tableName).removeValue();
                    // Reset table
                    Table table = new Table();
                    table.setStatus("off");
                    tableRef.child(tableName).setValue(table);
                    // Back to home screen
                    Intent backToHome = new Intent(PaymentActivity.this, MainActivity.class);
                    startActivity(backToHome);
                }
            }
        };
    }
    // ===========================================
    // =============== PRINTER ===================
    // ===========================================
    void FindBluetoothDevice(){
        try{

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if(pairedDevice.size()>0){
                String namePrt = "";
                for(BluetoothDevice pairedDev:pairedDevice){
                    namePrt += pairedDev.getName();
                    // My Bluetoth printer name is BTP_F09F1A
                    if(pairedDev.getName().equals("Printer001")){
                        bluetoothDevice=pairedDev;
                        finded = true;
                        break;
                    }
                }
                Toast.makeText(this, namePrt, Toast.LENGTH_SHORT).show();
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }    // Open Bluetooth Printer
    void openBluetoothPrinter() throws IOException {
        try{

            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket=bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();

            beginListenData();

        }catch (Exception ex){

        }
    }
    // Listen data
    void beginListenData(){
        try{

            final Handler handler =new Handler();
            final byte delimiter=10;
            stopWorker =false;
            readBufferPosition=0;
            readBuffer = new byte[1024];

            thread=new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker){
                        try{
                            int byteAvailable = inputStream.available();
                            if(byteAvailable>0){
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for(int i=0; i<byteAvailable; i++){
                                    byte b = packetByte[i];
                                    if(b==delimiter){
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer,0,
                                                encodedByte,0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte, StandardCharsets.US_ASCII);
                                        readBufferPosition=0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        });
                                    }else{
                                        readBuffer[readBufferPosition++]=b;
                                    }
                                }
                            }
                        }catch(Exception ex){
                            stopWorker=true;
                        }
                    }

                }
            });

            thread.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    // Printing Text to Bluetooth Printer //
    void printData(Invoice invoice) throws  IOException{
        try{


            byte[] normal = new byte[]{0x1B,0x21,0x00};  // 32 digits
            byte[] bold = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
            byte[] boldMedium = new byte[]{0x1B,0x21,0x20}; // 16 digits
            byte[] boldLarge = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            String breakLine = "................................";
            String breakLine2 = "********************************";
            String breakLine3 = "________________________________";

            //
            String top = "\n\n\n";
            top += "        Oc Ngon \n";
            outputStream.write(boldMedium);
            outputStream.write(top.getBytes());

            String title = "";
            title += "\n   THANH LICH   \n";
//            title += breakLine3;
            outputStream.write(boldMedium);
            outputStream.write(title.getBytes());

            // SDT    : 0935016628
            // Dia chi: 01 Nguyen Khanh Toan
            String contact = "\n";
            contact += " Dia chi: 01 Nguyen Khanh Toan\n";
            contact += " SDT    : 0935016628\n";

            contact += breakLine3 + "\n";
            outputStream.write(normal);
            outputStream.write(contact.getBytes());

            // 20191128084013AM
            String y = invoice.getPaymentDate().substring(0, 4);
            String m = invoice.getPaymentDate().substring(4, 6);
            String d = invoice.getPaymentDate().substring(6, 8);
            String date = d + "/" + m + "/" + y;
            String time = invoice.getPaymentDate().substring(8, 10) +":"+invoice.getPaymentDate().substring(10, 12);

            // Ban           : 1
            // Nhan vien : abc
            // Ngay         : 1995/01/01-08:09
            // Print invoice info
            String invoiceInfoMsg = "\n";

            invoiceInfoMsg += "Ban       : " +invoice.getTableName() + "\n";
            invoiceInfoMsg += "Nhan vien : " +invoice.getPaymentEmp() + "\n";
            invoiceInfoMsg += "Ngay      : " +date+"-"+time + "\n";
            outputStream.write(normal);
            outputStream.write(invoiceInfoMsg.getBytes());

            // SL   Gia   TTien
            String title2 = "\n";
            title2 += " So luong    Gia    Thanh Tien\n";
            title2 += breakLine3 + "\n";
            outputStream.write(bold);
            outputStream.write(title2.getBytes());

            // Append orderedList
            String listOrderMsg = "";
            for (Order order: orderedList) {
                listOrderMsg += CommonUtil.removeAccent(order.getItemName()) + "\n";
                // 01    1.000.000    1.000.000
                int price = Integer.parseInt(order.getAmount()) / Integer.parseInt(order.getQuantum());
                String full = order.getQuantum() + String.format("%,d", price)
                        + String.format("%,d", Integer.parseInt(order.getAmount()));
                listOrderMsg += getMargin(full);
                listOrderMsg += order.getQuantum();
                listOrderMsg += getMargin(full);

                listOrderMsg += String.format("%,d", price);
                listOrderMsg += getMargin(full);
                listOrderMsg += String.format("%,d", Integer.parseInt(order.getAmount())) + "\n";
                listOrderMsg += breakLine + "\n";
            }
            outputStream.write(normal);
            outputStream.write(listOrderMsg.getBytes());

            // Print total amount
            String amountMsg = "";
            amountMsg = amountMsg + "\n********************************";
            amountMsg = amountMsg + "\nTong cong";
            amountMsg = amountMsg + "               ";
            amountMsg = amountMsg + String.format("%,d", Integer.valueOf(invoice.getAmount()));
            amountMsg = amountMsg + "\n\n";
            outputStream.write(normal);
            outputStream.write(amountMsg.getBytes());

            // Print Good bye
            String byebye = "        Cam on quy khach\n";
            byebye       += "     Copyright by loNguyen\n\n\n\n";
            outputStream.write(normal);
            outputStream.write(byebye.getBytes());
//            disconnectBT();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private String getMargin(String str) {
        String space = "";
        int margin = (32 - str.length()) / 4;
        for (int i=0; i<=margin; i++) space += " ";
        return space;
    }
    // Disconnect Printer //
    void disconnectBT() throws IOException{
        try {
            stopWorker=true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    //00000000000000000000000000000000
    //   01   1.000.000   1.000.000
    //        Cam on quy khach
    //     copyright by loNguyen
}
