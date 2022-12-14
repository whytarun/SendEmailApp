package net.simplifiedcoding.myemailsender;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.sql.DriverManager.println;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.security.ProviderInstaller;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTo;
    private EditText editTextFrom;
    private EditText editTextSubject;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTo = findViewById(R.id.editTextTo);
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextSubject = findViewById(R.id.editTextSubject);
        editTextMessage = findViewById(R.id.editTextMessage);
        checkTls();
        findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    private void sendEmail() {
        String to = editTextTo.getText().toString().trim();
        String from = editTextFrom.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(to).matches()) {
            editTextTo.setError("Valid Recipient required");
            editTextTo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(from).matches()) {
            editTextFrom.setError("Valid Sender required");
            editTextFrom.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            editTextSubject.setError("Subject required");
            editTextSubject.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            editTextMessage.setError("Message required");
            editTextMessage.requestFocus();
            return;
        }

        RetrofitClient.getInstance()
                .getApi()
                .sendEmail(from, to, subject, message)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == HTTP_OK) {
                            try {
                                JSONObject obj = new JSONObject(response.body().string());
                                Toast.makeText(MainActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        println("SSL ERROR=="+t.getMessage());
                    }
                });

    }

    protected void checkTls() {
        if (android.os.Build.VERSION.SDK_INT < 21) {
            try {
                ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
                    @Override
                    public void onProviderInstalled() {
                        SSLContext sslContext = null;
                        try {
                            sslContext = SSLContext.getInstance("TLSv1.2");
                            sslContext.init(null, null, null);
                            SSLEngine engine = sslContext.createSSLEngine();
                            engine.setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (KeyManagementException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onProviderInstallFailed(int i, Intent intent) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
