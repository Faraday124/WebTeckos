package engineer.thesis.websocket.app.controllers;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import engineer.thesis.websocket.app.MainActivity;
import engineer.thesis.websocket.app.sql.SQLiteHandler;


public class PhonebookController extends Controller {

    public PhonebookController(Context context) {
        super(context);
    }

    public JSONObject getPhonebook() throws JSONException {
        JSONObject data = new JSONObject();
        JSONArray phonecontacts = new JSONArray();
        SQLiteHandler db = new SQLiteHandler(context);
        HashMap contactsList = db.getAllContacts();

        Iterator it = contactsList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            JSONObject phonecontact = new JSONObject();
            phonecontact.put("name", pair.getValue());
            phonecontact.put("number", pair.getKey());
            phonecontacts.put(phonecontact);
            it.remove();
        }
        data.put("phonecontacts", phonecontacts);
        return data;
    }

    public void editContact(String nameBefore, String nameAfter, String numberBefore, String numberAfter) {

        SQLiteHandler db = new SQLiteHandler(context);
        db.deleteContact(numberBefore);
        if (!numberBefore.equals(numberAfter))
            editNumber(nameBefore, numberAfter);

        Cursor phones = context.getContentResolver().query(Data.CONTENT_URI, null, null, null, null);

        if (!nameBefore.equals(nameAfter)) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(Data.DISPLAY_NAME));
                if (nameBefore.equals(name)) {
                    String id = phones.getString(phones.getColumnIndex(Data._ID));
                    editName(id, nameAfter);
                }
            }
        }
        db.addContact(numberAfter, nameAfter);
    }

    public void editName(String contact_id, String changedName) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                    .withSelection(Data._ID + "=? AND " +
                                    Data.MIMETYPE + "='" +
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
                            new String[]{contact_id})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, changedName)
                    .build());

            ContentProviderResult[] result = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.w("UpdateContact", e.getMessage() + "");
            for (StackTraceElement ste : e.getStackTrace()) {
                Log.w("UpdateContact", "\t" + ste.toString());
            }
        }
    }

    public void editNumber(String name, String numberAfter) {
        String where = Data.DISPLAY_NAME + " = ? AND " +
                Data.MIMETYPE + " = ? AND " +
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";

        String[] params = new String[]{name,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        try {
            ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                    .withSelection(where, params)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberAfter)
                    .build());
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.w("UpdateContact", e.getMessage() + "");
            for (StackTraceElement ste : e.getStackTrace()) {
                Log.w("UpdateContact", "\t" + ste.toString());
            }
        }
    }

    public void deleteContact(String name, String phone) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = context.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        context.getContentResolver().delete(uri, null, null);
                    }
                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        SQLiteHandler db = new SQLiteHandler(context);
        db.deleteContact(phone);
    }

    public void createNewContact(String name, String number) {
        SQLiteHandler db = new SQLiteHandler(context);
        String DisplayName = name;
        String MobileNumber = number;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        Log.d("rawContact", Integer.toString(rawContactInsertIndex));

        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        if (MobileNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.addContact(number, name);
        try {
            MainActivity.sendMessageToServer(utils.sendResponse(getPhonebook().toString(), "phonebook"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public File doBackup() {
        final String vfile = "Contacts.vcf";
        String path = null;
        Cursor phones = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        phones.moveToFirst();
        for (int i = 0; i < phones.getCount(); i++) {
            String lookupKey = phones.getString(phones
                    .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_VCARD_URI,
                    lookupKey);
            AssetFileDescriptor fd;
            try {
                fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                FileInputStream fis = fd.createInputStream();
                byte[] buf = new byte[(int) fd.getDeclaredLength()];
                fis.read(buf);
                String VCard = new String(buf);
                path = Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "websocketApp" + File.separator + vfile;
                FileOutputStream mFileOutputStream = new FileOutputStream(path,
                        true);
                mFileOutputStream.write(VCard.toString().getBytes());
                phones.moveToNext();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        File backup = new File(path);
        return backup;
    }
}
