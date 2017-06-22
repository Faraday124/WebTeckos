package engineer.thesis.websocket.app.config;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WsConfig {
    public static final String URL_WEBSOCKET = getIpAddress();//"ws://192.168.1.104:8080/WebsocketServer/webosocket?name=";

    public static String getIpAddressFromFile() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "address.txt");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (IOException e) {

        }

        return text.toString().trim();
    }

    public static String getIpAddress() {

        String result = getIpAddressFromFile();
        return "ws://" + result + ":8080/WebsocketServer/websocket?name=";
    }


}