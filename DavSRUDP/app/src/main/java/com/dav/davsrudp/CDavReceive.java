package com.dav.davsrudp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class CDavReceive extends AsyncTask<Void, Void, Void>{
    int actionType = 3;  // 0-stop,1-pause,2-continue,3-start/run
    boolean runing = true;
    private boolean Server_aktiv = true;
    private int portServer = 1600;
    private int portSender = 0;
    private InetAddress addressSender;
    private String strMSG;
    private String strLine;
    private DatagramSocket ds = null;
    private Handler myupdateUIHandler = null; //UpdateFrom Thread
    // Message type code.
    private final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;

    CDavReceive() {}// local port

    @Override
    protected void onPreExecute() {
        Log.d("CDavReceive:","onPreExecute");
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("CDavReceive:","doInBackground");
        byte[] lMsg = new byte[4096];
        DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
        String strMsg;
        SocketAddress iaddLocal;
        try
        {
            ds = new DatagramSocket(portServer);
            iaddLocal = ds.getLocalSocketAddress();
            while(Server_aktiv)
            {
                Log.d("CDavReceive:","doInBackground-receive");
                ds.receive(dp);
                strMSG =  new String(lMsg, 0, dp.getLength());
                int il = strMSG.length();
                strLine = "Len=" + Integer.toString(il) + ">" + strMSG;
                Log.d("Got:",strLine);
//                Intent i = new Intent();
//                i.setAction(Main.MESSAGE_RECEIVED);
//                i.putExtra(Main.MESSAGE_STRING, new String(lMsg, 0, dp.getLength()));
//                Main.MainContext.getApplicationContext().sendBroadcast(i);
                // -- Replay the message
                // Get sender IP
                addressSender = dp.getAddress ();
                // Get sender PORT
                portSender = dp.getPort();
                Send();
                String strMSGt = strMSG.replaceAll("\r\n","");
                UpdateUI(strMSGt,2);
                String stFromIP =  addressSender + ":" + portSender;
                stFromIP = stFromIP.replace("/","");
                UpdateUI(stFromIP,1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (ds != null)
            {
                ds.close();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.d("CDavReceive:","onPostExecute");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Log.d("CDavReceive:","onProgressUpdate");
    }
    //****
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
    void Send(){
        try
        {
            byte[] buf = new byte[256];
            String strBack ="Replay:" + strMSG;
            buf = strBack.getBytes ();
            DatagramPacket packet = new DatagramPacket (buf, buf.length, addressSender, 2600);
            ds.send (packet);
        }
        catch (IOException e)
        {
        }
    }
    void setHandle(Handler valHandler){
        myupdateUIHandler = valHandler;
    }
    void UpdateUI(String strMsg, int iwhat){
        // Build message object.
        Message message = myupdateUIHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("tv1", strMsg);
        message.what = iwhat;
        message.setData(bundle);

        myupdateUIHandler.sendMessage(message);

//        Message message = new Message();
        // Set message type.
//        message.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;
        // Send message to main thread Handler.
//        myupdateUIHandler.sendMessage(message);
    }

}
