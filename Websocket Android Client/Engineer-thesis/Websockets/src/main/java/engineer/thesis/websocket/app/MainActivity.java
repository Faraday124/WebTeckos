package engineer.thesis.websocket.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import engineer.thesis.websocket.app.controllers.Controller;
import engineer.thesis.websocket.app.observers.SMSObserver;
import engineer.thesis.websocket.app.sql.SQLiteHandler;
import engineer.thesis.websocket.app.utils.Utils;
import engineer.thesis.websocket.app.config.WsConfig;


public class MainActivity extends Activity {


    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context = this;

    private static WebSocketClient client;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    private Utils utils;
    private String name = null;
    SMSObserver observer;
    private TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new Utils(getApplicationContext());

        registerSMSObserver();
        startDesktopTimer();
        createWakeLock();
        saveContactsToDatabase();

        Intent i = getIntent();
        name = i.getStringExtra("name");

        client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET + URLEncoder.encode(name)), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, String.format("Got string message! %s", message));
                // wakeLock.acquire();
                try {
                    parseMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onMessage(byte[] data) {
              //  wakeLock.acquire();
                Controller controller = new Controller(context);
                controller.readFile(data);

                // wakeLock.release();
            }

            @Override
            public void onDisconnect(int code, String reason) {
                String message = String.format(Locale.US,
                        "Disconnected! Code: %d Reason: %s", code, reason);
                showToast(message);
                utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error! : " + error);
                showToast("Error! : " + error);
            }
        }, null);
        client.connect();
    }


    public static void sendMessageToServer(String message) {
        if (client != null && client.isConnected()) {
            client.send(message);
        }
    }

    public static void sendFile(byte[] data) {
        if (client != null && client.isConnected()) {
            client.send(data);
        }
    }


    private void parseMessage(final String msg) throws IOException, JSONException {
        Controller controller = new Controller(context);
        controller.parse(msg);
        //       wakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null & client.isConnected()) {
            client.disconnect();
            context.getContentResolver().unregisterContentObserver(observer);
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveContactsToDatabase() {
        SQLiteHandler db = new SQLiteHandler(context);
        db.removeAllContacts();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            db.addContact(number, name);
        }
    }


    private void playBeep() {
        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerSMSObserver() {
        Uri smsUri = Uri.parse("content://sms/");
        observer = new SMSObserver(context);
        context.getContentResolver().registerContentObserver(smsUri, true, observer);
    }

    private void startDesktopTimer() {
        timer = (TextView) findViewById(R.id.timer);
        final SimpleDateFormat df = new SimpleDateFormat("k:mm:ss");
        final Handler handler = new Handler();
        Timer myTimer = new Timer();
        final Runnable myRunnable = new Runnable() {
            public void run() {
                Calendar c = Calendar.getInstance();
                String time = df.format(
                        c.getTime());
                timer.setText(time);
            }
        };
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(myRunnable);
            }
        }, 0, 1000);
    }

    private void createWakeLock() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");

    }
}
