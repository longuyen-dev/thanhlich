package com.fpt.longnh42.thanhlich.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.fpt.longnh42.thanhlich.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class PrintUtil extends MainActivity {
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public PrintUtil() {
        System.out.println("called roi");
    }

    public void StartPrint() {
        try{
            FindBluetoothDevice();
            openBluetoothPrinter();

        }catch (Exception ex){
            ex.printStackTrace();
        }
        try{
            printData();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        try{
            disconnectBT();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

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
                        break;
                    }
                }
                System.out.println(namePrt+ "name printer");
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    // Open Bluetooth Printer
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
    void printData() throws  IOException{
        try{
            String msg = "   THANH LICH";

            byte[] normal = new byte[]{0x1B,0x21,0x00};  // 32 digits
            byte[] bold = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
            byte[] boldMedium = new byte[]{0x1B,0x21,0x20}; // 16 digits
            byte[] boldLarge = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text

            outputStream.write(boldMedium);
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));

        }catch (Exception ex){
            ex.printStackTrace();
        }
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

