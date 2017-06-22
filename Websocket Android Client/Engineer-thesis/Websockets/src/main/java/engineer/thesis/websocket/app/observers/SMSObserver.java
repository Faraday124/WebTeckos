package engineer.thesis.websocket.app.observers;

import android.content.Context;
import android.database.ContentObserver;

import org.json.JSONException;

import engineer.thesis.websocket.app.MainActivity;
import engineer.thesis.websocket.app.controllers.SMSController;
import engineer.thesis.websocket.app.utils.Utils;

public class SMSObserver extends ContentObserver {
    Context context;

    public SMSObserver(Context context) {
        super(null);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        SMSController smsController = new SMSController(context);
        Utils utils = new Utils(context);
        try {
            MainActivity.sendMessageToServer(utils.sendResponse(smsController.getFullSmsResponse().toString(), "sms-change"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}