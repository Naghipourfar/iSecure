package ir.naghi.isecure;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class LEDControllerActivity extends AppCompatActivity {
    private Button onButton;
    private Button offButton;
    private Button disconnectButton;
    private Button sendButton;
    private Button recieveButton;
    private TextView reciveTextView;
    private EditText messageText;
    private SeekBar brightness;
    private TextView progressValue;
    private String address = null;
    private ProgressDialog progress;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private boolean isBluetoothConnected = false;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//00001101-0000-1000-8000-00805F9B34FB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(DevicesListActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        setContentView(R.layout.activity_ledcontroller);

        onButton = (Button)findViewById(R.id.onButton);
        offButton = (Button)findViewById(R.id.offButton);
        disconnectButton = (Button)findViewById(R.id.disconnectButton);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        progressValue = (TextView)findViewById(R.id.lumn);
        sendButton = (Button) findViewById(R.id.sendButton);
        recieveButton = (Button) findViewById(R.id.recieveButton);
        messageText = (EditText) findViewById(R.id.messageText);
        reciveTextView = (TextView) findViewById(R.id.reciveTextView);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLED();
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLED();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromLED();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    progressValue.setText(String.valueOf(progress));
                    try {
                        bluetoothSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    } catch (IOException e) {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(messageText.getText().toString());
            }
        });

        recieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageText.getText().toString().isEmpty()){
                    return;
                }
                reciveTextView.append("\n" + reciveMessage());
            }
        });
    }

    private void disconnectFromLED() {
        if (bluetoothSocket !=null) //If the bluetoothSocket is busy
        {
            try
            {
                bluetoothSocket.close(); //close connection
            }
            catch (IOException e)
            { showMessage("Error");}
        }
        finish(); //return to the first layout

    }

    private void turnOffLED() {                                 // Arduino Code Must Be Set Up
        if (bluetoothSocket !=null)
        {
            try
            {
                bluetoothSocket.getOutputStream().write("Off".toString().getBytes());           // What's The String??
            }
            catch (IOException e)
            {
                showMessage("Error");
            }
        }
    }

    private void turnOnLED() {                              // Arduino Code Must Be Set Up
        if (bluetoothSocket !=null)
        {
            try
            {
                //turn the LED On
                bluetoothSocket.getOutputStream().write("On".toString().getBytes());            // What's The String??
            }
            catch (IOException e)
            {
                showMessage("Error in Turning On The LED");
            }
        }
    }

    private void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void sendMessage(String s) {
        if (bluetoothSocket !=null)
        {
            try
            {
                bluetoothSocket.getOutputStream().write(s.getBytes());
            }
            catch (IOException e)
            {
                showMessage("Error in Sending Message To The LED");
            }
        }
    }

    private String processReceiveSentence(String sentence) {
        String newSentence = "";
        for (int i = 0; i < sentence.length(); i++) {
            if (sentence.charAt(i) != '\n') {
                newSentence += sentence.charAt(i);
            }
        }

        return newSentence;
    }

    private String reciveMessage(){
        if (bluetoothSocket !=null)
        {
            try
            {
                int ch = 0;
                String sentence = "";
                int i = 0;
                while ((ch = bluetoothSocket.getInputStream().read()) != -1) {
                    if (i == 10) {
                        return processReceiveSentence(sentence);
                    }
                    i++;
                    sentence += (char) ch;
                }

            }
            catch (IOException e)
            {
                showMessage("Error in Turning On The LED");
            }
        }
        return "Bluetooth is Off";
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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

    private class ConnectBT extends AsyncTask<Void, Void, Void>{  // UI thread
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(LEDControllerActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (bluetoothSocket == null || !isBluetoothConnected)
                {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    bluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();//start connection

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                showMessage("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                showMessage("Connected.");
                isBluetoothConnected = true;
            }
            progress.dismiss();
        }
    }
}
