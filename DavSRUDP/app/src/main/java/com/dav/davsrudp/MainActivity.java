package com.dav.davsrudp;

import android.annotation.SuppressLint;
import android.database.DataSetObserver;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.Arrays;

/*
2020-02-15  After lost changes from 2020-02-14
            Fix update screen items
 */

public class MainActivity extends AppCompatActivity {

    public static com.dav.davsrudp.CDavSend mySendAsyncTask;
    public static com.dav.davsrudp.CDavReceive myReceiveThread;
    public static com.dav.davsrudp.CDavSendThread mySendThread;
    public static com.dav.davsrudp.UDP_Client udp_Client;
    public static int ServerPort = 1600;
    public static int ClientPort = 1600;
    public static String ESPAddress = "192.168.17.149";
    public static String IPAddress, mySocket;
    public static String strToSend;
    public Object syncToken = new Object();
    private static int iCMsg = 0;

    Button butStart;
    Button butPause;
    Button butCont;
    Button butStop;
    Button butSend;
    EditText etMyIP;
    EditText etClientIP;
    TextView tvRecieved;
    TextView tvInfo;
    TextView etMsgToSend;
    TextView tvMsgFromIP;
    CheckBox ledUNO;
    CheckBox ledESP;
    CheckBox ledAll;
    ListView lvMsgList;
    private ArrayAdapter<String> listAdapter ;

    static private Handler updateUIHandler = null; //UpdateFrom Thread
    // Message type code.
    private final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// Initialize Handler.
        createUpdateUiHandler();

        butStart = findViewById(R.id.butStart);
        butPause = findViewById(R.id.butPause);
        butCont = findViewById(R.id.butCont);
        butStop = findViewById(R.id.butStop);
        butSend = findViewById(R.id.butSend);
        etMyIP = findViewById(R.id.etMyIP);
        etClientIP = findViewById(R.id.etClientIP);
        tvMsgFromIP = findViewById(R.id.tvMsgFromIP);
        tvRecieved = findViewById(R.id.tvRecieved);
        tvInfo = findViewById(R.id.tvInfo);
        etMsgToSend = findViewById(R.id.etMsgToSend);
        ledUNO = findViewById(R.id.leduno);
        ledESP = findViewById(R.id.ledesp);
        ledAll = findViewById(R.id.ledAll);
        lvMsgList = findViewById(R.id.lvMsgList);

        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IPAddress = GetAddress(etClientIP);
                int iPort = GetPort(etClientIP);
                strToSend = etMsgToSend.getText().toString();
                iCMsg++;
                udp_Client.bMsg = true;
            }
        });

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        etMyIP.setText(ip+":"+ClientPort);

        workerThread.start(); // Update from thread

        strToSend = "From Android"; // get socket data
        etClientIP.setText(ESPAddress+":"+ServerPort);
        // send socket data
        udp_Client = new com.dav.davsrudp.UDP_Client();
        udp_Client.execute();


        myReceiveThread = new com.dav.davsrudp.CDavReceive();
        myReceiveThread.setHandle(updateUIHandler);
  //      myReceiveThread.execute();
          myReceiveThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  //x      mySendAsyncTask = new com.dav.davsrudp.CDavSend();
  //x      mySendAsyncTask.execute();

  //x      mySendThread = new com.dav.davsrudp.CDavSendThread(syncToken);
  //x      mySendThread.setPriority(10);
  //      mySendThread.run();
        String[] planets = new String[] { "Messages"};
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll( Arrays.asList(planets) );

        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);

        // Add more planets. If you passed a String[] instead of a List<String>
        // into the ArrayAdapter constructor, you must not add more items.
        // Otherwise an exception will occur.
        // Set the ArrayAdapter as the ListView's adapter.
        lvMsgList.setAdapter( listAdapter );
        // Default setting on startup
        butStart.setEnabled(false);
        butPause.setEnabled(false);
        butCont.setEnabled(false);
        butStop.setEnabled(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    public String GetAddress(EditText etVal)
    {
        String IPAddress;
        String strIPandPort = etClientIP.getText().toString();
        IPAddress = strIPandPort.substring(0, strIPandPort.indexOf(":"));
        return IPAddress;
    }
    int GetPort(EditText etVal)
    {
        String strIPandPort = etClientIP.getText().toString();
        int iPort = Integer.parseInt(strIPandPort.substring(strIPandPort.indexOf(":")+1));
        return iPort;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        boolean bSend = true;
        String strCmd ="";
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.leduno:
                if (!checked) {
                    strCmd = "U:001:"; // get socket data
                }else
                {
                    strCmd = "U:002:"; // get socket data
                }
                break;
            case R.id.ledesp:
                if (!checked) {
                    strCmd = "E:001:"; // get socket data
                }else
                {
                    strCmd = "E:002:"; // get socket data
                }
                break;
            case R.id.ledAll:
                if (!checked) {
                    strCmd = "A:001:"; // get socket data
                    ledESP.setChecked(false);
                    ledUNO.setChecked(false);
                }else
                {
                    strCmd = "A:002:"; // get socket data
                    ledESP.setChecked(true);
                    ledUNO.setChecked(true);
                }
                break;
            default:
                bSend = false;
                break;
        }
        if (bSend) {
 //           IPAddress = ESPAddress; // get ip address
            IPAddress = GetAddress(etClientIP);
  //          int iPort = GetPort(etClientIP);
            strToSend = strCmd;

            iCMsg++;
            strToSend = strToSend + iCMsg;
            udp_Client.bMsg = true;
        }
    }
    // Update from THread
    Thread workerThread = new Thread()
    {
        @Override
        public void run() {
            // Can not update ui component directly when child thread run.
            // updateText();

            // Build message object.
            Message message = new Message();
            // Set message type.
            message.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;
            // Send message to main thread Handler.
            updateUIHandler.sendMessage(message);
        }
    };
    // Update ui text.
    private void updateText(TextView tvVal)
    {
        String userInputText = tvVal.getText().toString();
        tvVal.setText(userInputText);
    }
    /* Create Handler object in main thread. */
    @SuppressLint("HandlerLeak")
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    String text = bundle.getString("tv1");
                    switch(msg.what) {
                        case 1:
                            tvMsgFromIP.setText(text);
                            break;
                        case 2:
//                            tvRecieved.setText(text);
//                            updateText(lvMsgList);
//                            String userInputText = tvRecieved.getText().toString();
                            listAdapter.insert(text,0);
                            break;
                        case 3:
                            etMsgToSend.setText("Send:");
       //                     updateText(etMsgToSend);
                            break;
                    }
                    // text will contain the string "your string message"
                }
 //               @Override
                public void handleMessagex(Message msg) {
                    // Means the message is sent from child thread.
                    if(msg.what == MESSAGE_UPDATE_TEXT_CHILD_THREAD)
                    {
                        // Update ui in main thread.
                        updateText(tvRecieved);
                        String userInputText = tvRecieved.getText().toString();
                        listAdapter.add(userInputText);
                    }
                }
            };
        }
    }
}
