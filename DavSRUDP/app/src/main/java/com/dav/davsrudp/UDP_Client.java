package com.dav.davsrudp;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDP_Client extends AsyncTask<Void, Void, Void>{
    public boolean bMsg = true;
    @Override
    protected void onPreExecute() {
        Log.d("UDP_Client:","onPreExecute");
    }
    @Override
    protected Void doInBackground(Void... params)
    {
        Log.d("UDP_Client:","doInBackground");
        String str = MainActivity.strToSend;
        DatagramSocket ds = null;
        while(true) {
            if (bMsg) {
                str = MainActivity.strToSend;
                try {
                    ds = new DatagramSocket();
                    InetAddress sendAddress = InetAddress.getByName(MainActivity.IPAddress);
                    DatagramPacket dp;
                    dp = new DatagramPacket(str.getBytes(), str.length(), sendAddress, MainActivity.ServerPort);
                    ds.send(dp);
                    bMsg = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onPostExecute(Void result)
    {
        Log.d("UDP_Client:","onPostExecute");
    }
    @Override
    protected void onProgressUpdate(Void... values) {
        Log.d("UDP_Client:","onProgressUpdate");
    }

}