package engineer.thesis.websocket.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import engineer.thesis.websocket.app.async.AuthorisationRequestTask;


public class LoginActivity extends Activity {

    private Button btnJoin;
    private EditText txtName, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        btnJoin = (Button) findViewById(R.id.btnJoin);
        txtName = (EditText) findViewById(R.id.name);
        txtPassword = (EditText) findViewById(R.id.password);

        btnJoin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (txtName.getText().toString().trim().length() > 0) {

                    String name = txtName.getText().toString().trim();
                    String password = txtPassword.getText().toString().trim();

                    try {
                        sendAuthorisationRequest(name, password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your name", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendAuthorisationRequest(String login, String password) throws JSONException, IOException {
        new AuthorisationRequestTask(this).execute(login, password);
    }
}
