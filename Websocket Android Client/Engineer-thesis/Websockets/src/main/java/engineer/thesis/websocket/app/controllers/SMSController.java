package engineer.thesis.websocket.app.controllers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import engineer.thesis.websocket.app.models.Correspondent;
import engineer.thesis.websocket.app.models.Message;
import engineer.thesis.websocket.app.sql.SQLiteHandler;

public class SMSController extends Controller {

    public SMSController(Context context) {
       super(context);
    }
    public void removeMessagesFromPhoneNumber(String phone){
        String[] reqCols = new String[]{"_id","address"};
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/"), reqCols, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String address = cursor.getString(1);
                address = address.replace("+48", "");
                address = address.replaceAll("\\s+", "");
              if (phone.equals(address)) {
                  context.getContentResolver().delete(
                          Uri.parse("content://sms/" + id), null, null);
                }
            }

            while (cursor.moveToNext());
            cursor.close();
        }
    }
    public void sendSms(String phone, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = smsManager.divideMessage(message);
        smsManager.sendMultipartTextMessage(phone, null, messages, null, null);
    }

    public JSONObject getFullSmsResponse() throws JSONException {
        JSONObject data = new JSONObject();
        JSONArray contacts = getSmsCorrespondents();
        JSONObject first = (JSONObject) contacts.get(0);
        String firstNumber = first.getString("address");
        JSONArray messages = getSmsMessages(firstNumber);
        data.put("contacts", contacts);
        data.put("messages", messages);
        return data;
    }

    public JSONArray getSmsCorrespondents() throws JSONException {

        String[] reqCols = new String[]{"date", "address", "body"};
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/"), reqCols, null, null, null);
        ArrayList<Correspondent> correspondents = getContactsFromMessages(cursor);
        JSONArray result = convertToJsonCorrespondents(correspondents);

        return result;
    }

    public JSONArray getSmsMessages(String number) throws JSONException {
        String[] reqCols = new String[]{"date", "address", "body"};
        Cursor cursorSent = context.getContentResolver().query(Uri.parse("content://sms/sent"), reqCols, null, null, null);
        Cursor cursorInbox = context.getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);

        ArrayList<Message> messagesSent = getMessagesFromPhoneNumber(cursorSent, number, "outgoing");
        ArrayList<Message> messagesInbox = getMessagesFromPhoneNumber(cursorInbox, number, "incoming");
        messagesSent.addAll(messagesInbox);
        Collections.sort(messagesSent, new Message.MessageComparator());
        JSONArray messages = convertToJsonMessages(messagesSent);

        return messages;
    }
    public JSONArray convertToJsonCorrespondents(ArrayList<Correspondent> array) throws JSONException {

        JSONArray result = new JSONArray();
        for (int i = 0; i < array.size(); i++) {
            JSONObject correspondent = new JSONObject();
            correspondent.put("name", array.get(i).getName());
            correspondent.put("address", array.get(i).getAddress());
            correspondent.put("date", array.get(i).getDate());
            result.put(correspondent);
        }
        return result;
    }
    public ArrayList getContactsFromMessages(Cursor cursor) throws JSONException {

        SQLiteHandler db = new SQLiteHandler(context);
        HashMap contactsList = db.getAllContacts();
        ArrayList correspondents = new ArrayList();
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String address = cursor.getString(1);
                address = address.replace("+48", "");
                address = address.replaceAll("\\s+", "");
                String name = contactsList.get(address) != null ? contactsList.get(address).toString() : address;
                JSONObject contactName = new JSONObject();
                if (!checkIfNameExists(correspondents, name)) {

                    Correspondent correspondent = new Correspondent(name, address, date);
                    correspondents.add(correspondent);
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return correspondents;
    }
    public boolean checkIfNameExists(ArrayList array, String name) {

        for (int j = 0; j < array.size(); j++) {
            Correspondent single = (Correspondent) array.get(j);
            if (single.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public ArrayList getMessagesFromPhoneNumber(Cursor cursor, String number, String direction) {
        ArrayList messages = new ArrayList();
        if (cursor.moveToFirst()) {
            do {
                String address = cursor.getString(1);
                address = address.replace("+48", "");
                address = address.replaceAll("\\s+", "");
                if (address.equals(number)) {
                    String date = cursor.getString(0);
                    String body = cursor.getString(2);
                    Message message = new Message(address, body, date, direction);
                    messages.add(message);
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return messages;
    }
    public JSONArray convertToJsonMessages(ArrayList array) throws JSONException {
        JSONArray result = new JSONArray();
        ArrayList<Message> myArray = array;
        for (int i = 0; i < myArray.size(); i++) {
            Message message = myArray.get(i);
            JSONObject json = new JSONObject();
            json.put("body", message.getBody());
            json.put("direction", message.getDirection());
            result.put(json);
        }
        return result;
    }

}
