package com.dav.davsrudp;

import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CDavSendThread extends Thread {
    Object syncToken;
    //public class CDavSend extends AsyncTask<String, Void, String>{
    int actionType = 1;  // 0-stop,1-pause,2-continue,3-start/run
    boolean runing = true;
    private DatagramSocket socket = null;
    private InetAddress address = null;
    public static com.dav.davsrudp.ChatSender mySender;


//    private DatagramPacket packet = null;

    CDavSendThread (Object syncToken) {
        this.syncToken = syncToken;
    }
    private static Object monitor = new Object();

    @Override
    public void run() {
        //   protected String doInBackground(String... params) {
        int iCount = 0;
        String strLine;

        while (runing) {
            synchronized(syncToken) {
                try {
                    syncToken.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            switch (actionType) {
                case 0: //Stop
                    runing = false;
                    break;
                case 1: // Pause
                    break;
                case 2: // Cont
                    actionType = 4;
                    break;
                case 3: // Start (from begining)
                    runing = true;
                    iCount = 0;
                    actionType = 4;
                    break;
                case 4: // running
                    strLine = Integer.toString(iCount);
                    Log.d("Send:",strLine );
                    mySender = new ChatSender();
                    mySender.run("192.168.17.149","Android Started",2600);

                    break;
                case 5: // Send
                    Send("Test");
                    break;
            }
            iCount++;
            try {
                //set time in mili
                Thread.sleep(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Boolean isRuning() {
        return  runing;
    }
    void setStart(){
        actionType = 3;
    }
    void setPause(){
        actionType = 1;
    }
    void setCont(){
        actionType = 2;
    }
    void setStop(){
        actionType = 0;
    }
    void setSend(){
        actionType = 5;
 //       synchronized(monitor) {
 //           monitor.notifyAll();
 //      }
    }
    void Send(String strMsg) {
        try {
            socket = new DatagramSocket(2600);
        } catch (SocketException e) {
            Log.e("Udp:", "DatagramSocket Error:", e);
            return;
        }
        try
        {
            try {
                address = InetAddress.getByName("192.168.17.149");
            } catch (UnknownHostException e1) {
                Log.e("Udp:", "getByName Error:", e1);
            }
            byte[] buf = new byte[256];
            String strBack ="Replay:" + strMsg;
            buf = strBack.getBytes ();
            DatagramPacket packet = new DatagramPacket (buf, buf.length, address, 2600);
            socket.setBroadcast(true);
            socket.send(packet);
        }
        catch (IOException e)
        {
            Log.e("Udp:", "Send Error:", e);
        }
    }

}
