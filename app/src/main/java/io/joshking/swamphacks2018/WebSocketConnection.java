package io.joshking.swamphacks2018;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Created by Joshua King on 1/20/18.
 */

public class WebSocketConnection {

    private WebSocketClient client;
    private URI uri;
    private boolean isConnected;
    private Runnable connectedListener;
    private Consumer<String> responseCallback;

    public WebSocketConnection(String url, Runnable connectedListener) {
        this.connectedListener = connectedListener;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file, Consumer<String> responseCallback) {
        this.responseCallback = responseCallback;
        if (client == null || !isConnected) {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            client.send(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectWebSocket() {
        client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
//                client.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                isConnected = true;
                connectedListener.run();
            }

            @Override
            public void onMessage(String s) {
                System.out.println("||||||||: " + s);
                if (responseCallback != null) {
                    try {
                        responseCallback.accept(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
//                client = null;
                isConnected = false;
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        client.connect();
    }
}
