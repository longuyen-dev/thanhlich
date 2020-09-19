package com.fpt.longnh42.thanhlich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.common.CommonUtil;
import com.fpt.longnh42.thanhlich.common.Reference;
import com.fpt.longnh42.thanhlich.object.Invoice;
import com.fpt.longnh42.thanhlich.object.Item;
import com.fpt.longnh42.thanhlich.object.Order;
import com.fpt.longnh42.thanhlich.rowAdapter.RowDetailInvoiceAdapter;
import com.fpt.longnh42.thanhlich.rowAdapter.RowItemAdapter;
import com.fpt.longnh42.thanhlich.rowAdapter.RowOrderAdapter;
import com.google.firebase.database.ChildEventListener;
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

public class DetailInvoiceActivity extends AppCompatActivity {
    TextView employeeNameEditText, dateEditText,
            amountTextView, tableNameTextView;
    Button printButton;
    ListView detailInvoiceListView;

    List<Order> detailInvoiceList = new ArrayList<>();
    RowDetailInvoiceAdapter rowDetailInvoiceAdapter;

    private boolean finded = false;

    // bluetooth
    private String amount, pay, exchange, tableName, empName;
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
        setContentView(R.layout.activity_detail_invoice);

        initView();
    }

    private void initView() {
        // find element
        employeeNameEditText = findViewById(R.id.employeeNameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        amountTextView = findViewById(R.id.amountTextView);
        tableNameTextView = findViewById(R.id.tableNameTextView);
        detailInvoiceListView = findViewById(R.id.detailInvoiceListView);
        printButton = findViewById(R.id.printButton);

        // define
        rowDetailInvoiceAdapter = new RowDetailInvoiceAdapter(this, R.layout.activity_row_detail_invoice_adapter, detailInvoiceList);
        detailInvoiceListView.setAdapter(rowDetailInvoiceAdapter);

        // add listener
        printButton.setOnClickListener(printEvent());

        Intent i = getIntent();
        String invoiceId = i.getStringExtra("invoiceId");
        getInvoiceInformation(invoiceId);

    }

    private void getInvoiceInformation(String invoiceId) {
        DatabaseReference invoiceRef = Reference.getInvoiceRef();
        String dateOfInvoice = invoiceId.substring(0, 8);
        invoiceRef.child(dateOfInvoice).child(invoiceId).addListenerForSingleValueEvent(getInvoiceEvent());

        DatabaseReference detailInvoiceRef = Reference.getDetailInvoiceRef();
        detailInvoiceRef.child(invoiceId).addListenerForSingleValueEvent(getDetailInvoiceEvent());
    }

    private ValueEventListener getInvoiceEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Invoice invoice = dataSnapshot.getValue(Invoice.class);
                amount = invoice.getAmount();
                tableName = invoice.getTableName();
                empName = invoice.getPaymentEmp();

                employeeNameEditText.setText(getString(R.string.employee) + ": "
                        + invoice.getPaymentEmp());
                String time = invoice.getPaymentDate().substring(8, 10) + ":"
                        +invoice.getPaymentDate().substring(10, 12);
                dateEditText.setText(time);
                amountTextView.setText(getString(R.string.allTable) + ": "
                        +invoice.getAmount());
                tableNameTextView.setText(getString(R.string.table) + ": "
                        +invoice.getTableName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener getDetailInvoiceEvent() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Order order = child.getValue(Order.class);
                    detailInvoiceList.add(order);
                }
                rowDetailInvoiceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private View.OnClickListener printEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil commonUtil = new CommonUtil();
                String toDay = commonUtil.getSystemDate();
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
            for (Order order: detailInvoiceList) {
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
}
