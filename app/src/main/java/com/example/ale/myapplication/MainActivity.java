package com.example.ale.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonElement;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    URI uri = null;
    EditText edtMensagem;
    Button btnEnviar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupConection();

        edtMensagem = (EditText) findViewById(R.id.edtMensagem);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void setupConection(){

        try {
            uri = new URI("https://donutchat.herokuapp.com/cable");
            Log.i("::CHECK", uri.toString());
        }catch (Exception e){
        }

        Consumer.Options options = new Consumer.Options();

        Map<String, String> headers = new HashMap<>();
        headers.put("token", "tokenkey");
        options.headers = headers;

        Consumer consumer = ActionCable.createConsumer(uri, options);

        Channel chatChannel = new Channel("ChatRoomsChannel");

        Subscription subscription = consumer.getSubscriptions().create(chatChannel);

        consumer.connect();

        subscription
                .onConnected(new Subscription.ConnectedCallback() {
                    @Override
            public void call() {
                Log.i("::CHECK", "onConnected");
            }
        }).onRejected(new Subscription.RejectedCallback() {
            @Override
            public void call() {
                Log.i("::CHECK", "RejectedCallback");
                // Called when the subscription is rejected by the server
            }
        }).onReceived(new Subscription.ReceivedCallback() {
            @Override
            public void call(JsonElement data) {
                Log.i("::CHECK", "onReceived");

                try {
                    Log.i("::CHECK", data.toString());
                }catch (Exception e){
                }
                // Called when the subscription receives data from the server
            }
        }).onDisconnected(new Subscription.DisconnectedCallback() {
            @Override
            public void call() {
                Log.i("::CHECK", "onDisconnected");

                // Called when the subscription has been closed
            }
        }).onFailed(new Subscription.FailedCallback() {
            @Override
            public void call(ActionCableException e) {
                Log.i("::CHECK", "onFailed");
                // Called when the subscription encounters any error
            }
        });
    }
}
