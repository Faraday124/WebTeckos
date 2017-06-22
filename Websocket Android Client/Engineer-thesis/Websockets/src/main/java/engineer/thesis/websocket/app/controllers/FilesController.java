package engineer.thesis.websocket.app.controllers;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class FilesController extends Controller {

    public FilesController(Context context) {
        super(context);
    }

    public void openFile(String name, String type) {
        String root = Environment.getExternalStorageDirectory().toString() + "/websocketApp/" + name;
        File myFile = new File(root);
        String mime;
        if (type.equals("apk"))
            mime = "application/vnd.android.package-archive";
        else
            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("." + type);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myFile), mime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public JSONObject getFiles() throws JSONException {

        File sdCardRoot = Environment.getExternalStorageDirectory();
        Log.d("outputx", sdCardRoot.toString());
        ArrayList<String> images = getFilesFromParentDirectory(sdCardRoot, "DCIM");
        ArrayList<String> music = getFilesFromParentDirectory(sdCardRoot, "Music");
        ArrayList<String> appFiles = getFilesFromParentDirectory(sdCardRoot, "websocketApp");
        Log.d("outputx", images.toString());
        JSONArray jsonImages = convertToJson(images);
        JSONArray jsonMusic = convertToJson(music);
        JSONArray jsonApp = convertToJson(appFiles);

        JSONObject data = new JSONObject();
        data.put("images", jsonImages);
        data.put("music", jsonMusic);
        data.put("app", jsonApp);

        return data;
    }

    public ArrayList getFilesFromParentDirectory(File path, String fileName) {

        ArrayList<String> result = new ArrayList<String>();

        File yourDir = new File(path, fileName);
        if (!yourDir.exists())
            return result;
        for (File f : yourDir.listFiles()) {
            if (f.isFile()) {
                result.add(f.getName());
            } else if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        result.addAll(getFilesFromParentDirectory(f, files[i].getName()));
                    } else if (files[i].isFile()) {
                        result.add(files[i].getName());
                    }
                }
            }
        }
        return result;
    }

    public JSONArray convertToJson(ArrayList<String> array) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.size(); i++) {
            jsonArray.put(array.get(i));
        }
        return jsonArray;
    }

    public File getFileByName(String fileName) {
        String appFiles = Environment.getExternalStorageDirectory().toString() + "/websocketApp/" + fileName;
        String images = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/" + fileName;
        String music = Environment.getExternalStorageDirectory().toString() + "/Music/" + fileName;
        File file = new File(appFiles);
        if (!file.exists()) {
            file = new File(images);
            if (!file.exists()) {
                file = new File(music);
                if (!file.exists()) {
                    file = null;
                }
            }
        }
        return file;
    }

    public void saveFile(String name, byte[] data) {
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/websocketApp");
            myDir.mkdirs();
            File file = new File(myDir, name);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPhoneWallpaper(String name) {
        String root = Environment.getExternalStorageDirectory().toString() + "/websocketApp/" + name;
        Bitmap bitmap = BitmapFactory.decodeFile(root);
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(context);
        try {
            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPhoneRingtone(String name) {
        String root = Environment.getExternalStorageDirectory().toString() + "/websocketApp/" + name;
        File k = new File(root);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, "Title1");
        values.put(MediaStore.MediaColumns.SIZE, 241755);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, "Artist1");
        values.put(MediaStore.Audio.Media.DURATION, 230);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);


        Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
        String exists = context.getContentResolver().getType(uri);
        Uri newUri;
        if (exists != null) {
            context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + k.getAbsolutePath() + "\"", null);
        }
        newUri = context.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri);
    }

    public byte[] prepareFileToSend(File file) throws IOException {

        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        String name = file.getName() + ",";
        String[] extension = name.split("\\.");
        String type = extension[extension.length - 1];
        String flag = "response,";
        String tag = "files-download";
        int countHeader = type.length() + name.length() + flag.length() + tag.length();
        String count = Integer.toString(countHeader);
        String details = count + type + name + flag + tag;
        fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();
        byte[] header = details.getBytes();
        byte[] data = new byte[header.length + bFile.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(bFile, 0, data, header.length, bFile.length);
        return data;
    }
}
