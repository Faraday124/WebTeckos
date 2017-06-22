package engineer.thesis.websocket.app.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;


public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();


    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "websockets";


    private static final String TABLE_CONTACTS = "contacts";


    private static final String KEY_ID = "id";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_NAME = "name";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
      String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ADDRESS + " TEXT unique," + KEY_NAME + " TEXT)";
        db.execSQL(CREATE_CONTACTS_TABLE);


        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }
    public void removeAllContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_CONTACTS;
        db.execSQL(selectQuery);
        db.close();

        Log.d(TAG, "All contacts removed");
    }
    public void deleteContact(String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "DELETE FROM " + TABLE_CONTACTS +" WHERE "+KEY_ADDRESS +"="+ number;
        db.execSQL(selectQuery);
        db.close();

        Log.d(TAG, "Contact removed");
    }
    public void addContact(String address, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        address = address.replace("+48", "");
        address = address.replace("-", "");
        address = address.replaceAll("\\s+", "");
        ContentValues values = new ContentValues();
        values.put(KEY_ADDRESS, address);
        values.put(KEY_NAME, name);
        long id = -2;
        try {
            id = db.insert(TABLE_CONTACTS, null, values);
        } catch (SQLiteException ex) {
            Log.d("ERR", "This address already exists!");
        }

        db.close(); // Closing database connection

        Log.d(TAG, "New contact inserted into sqlite: " + name.toString() + address.toString());
    }

    public HashMap<String, String> getAllContacts() {
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS + " ORDER BY name ASC";
        HashMap contacts = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            do {
                contacts.put(cursor.getString(1), cursor.getString(2));
            } while (cursor.moveToNext());


        }
        cursor.close();
        //db.close();

        Log.d(TAG, "Fetching contacts from Sqlite: " + contacts.toString());

        return contacts;
    }
}