package com.example.ale.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;

import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    URI uri = null;
    ImageView statusDraw;
    EditText edtMensagem;
    TextView tvOutput;

    Button btnEnviar;
    Channel chatChannel;
    Subscription subscription;
    boolean isConnected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupConection();

        statusDraw = (ImageView) findViewById(R.id.statusdraw);
        edtMensagem = (EditText) findViewById(R.id.edtMensagem);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        tvOutput = findViewById(R.id.output);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendText = edtMensagem.getText().toString().trim();
                if (sendText.length() > 0 && isConnected){

                    JsonObject userValues = new JsonObject();
                    userValues.addProperty("message", sendText);
                    userValues.addProperty("user", "sue.soap");

                    subscription.perform("send_message", userValues);
                    edtMensagem.setText("");
                    altStatus(4);
                }
            }
        });

    }

    private void setupConection(){

        try {
            uri = new URI("ws://sockets.nxtstepdsgn.com/cable");
            Log.i("::CHECK", uri.toString());
        }catch (Exception ignored){
        }

        Consumer.Options options = new Consumer.Options();
        options.reconnection = true;

        Map<String, String> headers = new HashMap<>();
        headers.put("token", "tokenkey");
        options.headers = headers;

        Consumer consumer = ActionCable.createConsumer(uri, options);

        chatChannel = new Channel("MessagesChannel");
        chatChannel.addParam("room_id", "36");
        subscription = consumer.getSubscriptions().create(chatChannel);

        subscription
            .onConnected(new Subscription.ConnectedCallback() {
                @Override
                public void call() {
                    Log.i("::CHECK", "onConnected");
                    altStatus(0);
                }
            }).onRejected(new Subscription.RejectedCallback() {
                @Override
                public void call() {
                    Log.i("::CHECK", "RejectedCallback");
                    altStatus(3);
                }
            }).onReceived(new Subscription.ReceivedCallback() {
                @Override
                public void call(JsonElement data) {
                    Log.i("::CHECK", "onReceived");
                    pre(data.toString());
                    Log.i("::CHECK", data.toString());
                    altStatus(4);
                }
            }).onDisconnected(new Subscription.DisconnectedCallback() {
                @Override
                public void call() {
                    Log.i("::CHECK", "onDisconnected");
                    altStatus(1);
                }
            }).onFailed(new Subscription.FailedCallback() {
                @Override
                public void call(ActionCableException e) {
                    Log.i("::CHECK", "onFailed");
                    Log.i("::CHECK", e.getMessage());
                    altStatus(2);
                }
            });

        consumer.connect();
    }

    public void pre(String result){
        try {

            output("Tx: " + result);

            JSONObject jsonObj = new JSONObject(result);
            String texto = jsonObj.getString("message");
            String user = jsonObj.getString("user");

            JSONObject jsonObj2 = new JSONObject(texto);
            String mess = jsonObj2.getString("message");
            mostrarTexto(mess, user);
        }catch (Exception e){
        }
    }

    public void altStatus(final int i){
        isConnected = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (i) {
                    case 0:
                        statusDraw.setImageDrawable(getResources().getDrawable(R.drawable.status_on));
                        isConnected = true;
                        break;
                    case 1:
                        statusDraw.setImageDrawable(getResources().getDrawable(R.drawable.status_off));
                        break;
                    case 2:
                        statusDraw.setImageDrawable(getResources().getDrawable(R.drawable.failed_animation));
                        break;
                    case 3:
                        statusDraw.setImageDrawable(getResources().getDrawable(R.drawable.status_rejected));
                        break;
                    case 4:
                        statusDraw.setImageDrawable(getResources().getDrawable(R.drawable.received_animation));
                        isConnected = true;
                        break;
                }
            }
        });

    }

    public void mostrarTexto(final String mens, final String user){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.info);
                TextView insertText = new TextView(MainActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                insertText.setLayoutParams(params);
                insertText.setText(mens);
                insertText.setTextSize(18);
//                insertText.setId(user);
                insertText.setPadding(20, 20, 20, 20);

                linearLayout.addView(insertText);
            }
        });
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("websocketchat", txt);
                tvOutput.setText(tvOutput.getText().toString() + "\n\n" + txt);
            }
        });
    }
}
