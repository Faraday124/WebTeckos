package engineer.thesis.websocket.app.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import engineer.thesis.websocket.app.MainActivity;
import engineer.thesis.websocket.app.utils.Utils;

public class Controller {

    Context context;
    Utils utils;

    public Controller(Context context) {
        this.context = context;
        utils = new Utils(context);
    }

    public void parse(String msg) throws JSONException, IOException {
        JSONObject jObj = new JSONObject(msg);
        String flag = jObj.getString("flag");
        if (flag.equalsIgnoreCase("request")) {
            String subject = jObj.getString("subject");
            if (subject.equals("battery")) {
                handleBattery(subject);
            } else if (subject.contains("phonebook")) {
                handlePhonebook(jObj);
            } else if (subject.contains("sms")) {
                handleSms(jObj);
            } else if (subject.contains("files")) {
                handleFiles(jObj);
            }
        } else if (flag.equals("self")) {
            String sessionId = jObj.getString("sessionId");
            utils.storeSessionId(sessionId);
        }
    }

    private void handleBattery(String subject) {
        String response = "";
        response = Float.toString(getBatteryLevel());
        sendMessage(response, subject);
    }

    private void handlePhonebook(JSONObject jObj) throws JSONException, IOException {
        String response = "";
        String subject = jObj.getString("subject");
        PhonebookController phonebookController = new PhonebookController(context);
        if (subject.equals("phonebook")) {
            response = phonebookController.getPhonebook().toString();
            sendMessage(response, subject);
        } else if (subject.equals("phonebook-edit")) {
            String extras = jObj.getString("extras");
            JSONObject jsonExtr = new JSONObject(extras);
            String nameBefore = jsonExtr.getString("nameBefore");
            String nameAfter = jsonExtr.getString("nameAfter");
            String numberBefore = jsonExtr.getString("numberBefore");
            String numberAfter = jsonExtr.getString("numberAfter");
            phonebookController.editContact(nameBefore, nameAfter, numberBefore, numberAfter);
            response = phonebookController.getPhonebook().toString();
            sendMessage(response, "phonebook");
        } else if (jObj.get("subject").equals("phonebook-delete")) {
            String contact = jObj.getString("phone");
            JSONObject jsonExtr = new JSONObject(contact);
            String contactName = jsonExtr.getString("name");
            String contactNumber = jsonExtr.getString("number");
            phonebookController.deleteContact(contactName, contactNumber);
            response = phonebookController.getPhonebook().toString();
            sendMessage(response, "phonebook");
        } else if (subject.equals("phonebook-new")) {
            String contact = jObj.getString("phone");
            JSONObject jsonExtr = new JSONObject(contact);
            String contactName = jsonExtr.getString("name");
            String contactNumber = jsonExtr.getString("number");
            phonebookController.createNewContact(contactName, contactNumber);
        } else if (subject.equals("phonebook-backup")) {
            FilesController filesController = new FilesController(context);
            File data = phonebookController.doBackup();
            MainActivity.sendFile(filesController.prepareFileToSend(data));
        }
    }

    private void handleSms(JSONObject jObj) throws JSONException {
        String response = "";
        String subject = jObj.getString("subject");
        SMSController smsController = new SMSController(context);
        if (subject.equals("sms")) {
            response = smsController.getFullSmsResponse().toString();
            sendMessage(response, subject);
        } else if (subject.equals("sms-body")) {
            String phone = jObj.getString("phone");
            JSONObject data = new JSONObject();
            data.put("messages", smsController.getSmsMessages(phone));
            response = data.toString();
            sendMessage(response, subject);
        } else if (subject.equals("sms-send")) {
            String phone = jObj.getString("phone");
            String message = jObj.getString("message");
            smsController.sendSms(phone, message);
        } else if (subject.equals("sms-remove")) {
            String phone = jObj.getString("phone");
            smsController.removeMessagesFromPhoneNumber(phone);
        }
    }

    private void handleFiles(JSONObject jObj) throws JSONException, IOException {
        String response = "";
        String subject = jObj.getString("subject");
        FilesController filesController = new FilesController(context);
        if (subject.equals("files-directory")) {
            response = filesController.getFiles().toString();
            sendMessage(response, subject);
        } else if (subject.equals("files-download")) {
            String fileName = jObj.getString("extras");
            File file = filesController.getFileByName(fileName);
            if (file != null) {
                byte[] data = filesController.prepareFileToSend(file);
                MainActivity.sendFile(data);
            }
        } else if (subject.equals("files-delete")) {
            String fileName = jObj.getString("extras");
            File file = filesController.getFileByName(fileName);
            if (file != null) {
                file.delete();
                response = filesController.getFiles().toString();
                sendMessage(response, "files-directory");
            }
        }
    }

    public void readFile(byte[] data) {
        String countHeader = new String(new byte[]{data[0], data[1]});
        int count;
        try {
            count = Integer.parseInt(countHeader);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        byte[] subArray = Arrays.copyOfRange(data, 2, count + 2);
        byte[] rawData = Arrays.copyOfRange(data, count + 2, data.length);
        String header = new String(subArray);
        List details = Arrays.asList(header.split(","));
        String type = details.get(0).toString();
        String fileName = details.get(1).toString();
        String flag = details.get(2).toString();
        String tag = details.get(3).toString();
        if (flag.equals("request")) {
            FilesController filesController = new FilesController(context);
            filesController.saveFile(fileName, rawData);
            if (tag.equals("application")) {
                filesController.openFile(fileName, type);
            } else if (tag.equals("wallpaper")) {
                filesController.setPhoneWallpaper(fileName);
            } else if (tag.equals("ringtone")) {
                filesController.setPhoneRingtone(fileName);
            } else if (tag.equals("sendFile")) {
                try {
                    String response = filesController.getFiles().toString();
                    sendMessage(response, "files-directory");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private float getBatteryLevel() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }

    private void sendMessage(String response, String subject) {
        String message = utils.sendResponse(response, subject);
        MainActivity.sendMessageToServer(message);
    }
}