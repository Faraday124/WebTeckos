package engineer.thesis.websocket.app.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import engineer.thesis.websocket.app.MainActivity;
import engineer.thesis.websocket.app.config.WsConfig;


public class AuthorisationRequestTask extends AsyncTask<String, Void, String> {
    Context context;

    public AuthorisationRequestTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String login = strings[0];
        String password = strings[1];
        String result = null;
        String ip = WsConfig.getIpAddressFromFile();
        String serverAddress = "http://"+ip+":9000/database/index.php";
        HttpPost httpPost = new HttpPost(serverAddress);
        HttpClient httpclient = new DefaultHttpClient();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("tag", "login"));
        nameValuePairs.add(new BasicNameValuePair("login", login));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
     if (result == null){
         Toast.makeText(context,
                 "Error with server!", Toast.LENGTH_LONG).show();
         return;
     }

        JSONObject jsonObject = null;
        String name = null;
        boolean error = true;
        try {
            jsonObject = new JSONObject(result);
            error = jsonObject.getBoolean("error");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!error){
            JSONObject user = null;
            try {
                user = (JSONObject) jsonObject.get("user");
                name = user.getString("login");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            goToMainActivity(name);

        }else
            Toast.makeText(context,
                    "Invalid user or password", Toast.LENGTH_LONG).show();
    }

       private void goToMainActivity(String name) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("name", name);
        context.startActivity(intent);
        }


}
