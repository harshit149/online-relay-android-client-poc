package com.example.onlinerelayclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInput;
    private TextView textViewOutput;
    private Button buttonSend;
    private boolean connection = false;
    //Initializing the socket variable
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.0.105:5000");
        } catch (URISyntaxException e) {
            Log.d("Socket","some error occurred in initializing socket.io");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        mSocket.on(EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Socket","Socket connection made");
                connection = true;
            }
        });

        mSocket.on(EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Socket","Socket connection disconnected");
                connection = false;
            }
        });

        mSocket.on("share-text", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.d("Socket","Share text event received from server with data "+args[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String outputText = textViewOutput.getText().toString() + "\n" + args[0].toString();
                        textViewOutput.setText(outputText);
                    }
                });
            }
        });
        //connect to the socket
        mSocket.connect();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextInput.getText().length() > 0 && connection)
                    mSocket.emit("share-text",editTextInput.getText().toString());
            }
        });
    }

    private void initializeViews() {
        editTextInput = findViewById(R.id.editTextInput);
        textViewOutput = findViewById(R.id.textViewOutput);
        buttonSend = findViewById(R.id.buttonSend);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("share-text");
    }
}