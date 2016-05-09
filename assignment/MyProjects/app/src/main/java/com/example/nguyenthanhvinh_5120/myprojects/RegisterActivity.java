package com.example.nguyenthanhvinh_5120.myprojects;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.nguyenthanhvinh_5120.myprojects.LoginActivity.encryptMD5;

public class RegisterActivity extends AppCompatActivity {



    String localhost = "http://serverandroid.esy.es/androidapi/";//link server database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Gán các thuộc tính từ layout
        Button regBtn = (Button) findViewById(R.id.registerBtn);
        final EditText email = (EditText)findViewById(R.id.emailTF);
        final EditText retypepass = (EditText)findViewById(R.id.repassEditText);
        final EditText username = (EditText)findViewById(R.id.usernameTF);
        final EditText password = (EditText)findViewById(R.id.passEditText);

        //Receive data from incoming Intent
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            username.setText(extras.getString("USERNAME"));
            password.setText(extras.getString("PASSWORD"));
        }

        //Xử lý sự kiện click button Đăng ký thành viên
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty() || email.getText().toString().isEmpty() || retypepass.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please retype username or email or password or retype password", Toast.LENGTH_SHORT).show();
                }
                else {
                    final String pw = password.getText().toString();
                    final String rp = retypepass.getText().toString();
                    if (!pw.equals(rp)) {
                        Toast.makeText(getApplicationContext(), "Password don't match!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        MyUploadTask t = new MyUploadTask();
                        t.execute();
                        Toast.makeText(getApplicationContext(), "Register successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*Class MyUploadTask là AsyncTask tạo liên kết với Database
    đưa thông tin (name, mail, password) lên Database trên server*/
    class MyUploadTask extends AsyncTask<Void,Void,Void> {

        String un, em, pw;

        protected void onPreExecute() {
            //display progress dialog.

            //Khai báo biến và gán các thuộc tính từ layout
            EditText email = (EditText)findViewById(R.id.emailTF);
            EditText username = (EditText)findViewById(R.id.usernameTF);
            EditText password = (EditText)findViewById(R.id.passEditText);

            //Lấy name, mail, password từ người dùng (người dùng nhập vào)
            un = username.getText().toString();
            em = email.getText().toString();
            pw = encryptMD5(password.getText().toString());

        }
        protected Void doInBackground(Void... params) {
            URL url;

            //Đưa lên server thông qua file index.php trong thư mục test/ trên server
            try {
                JSONObject json = new JSONObject();
                json.put("name",un);
                json.put("email",em);
                json.put("encrypted_password",pw);
                String s = json.toString();
                url = new URL(localhost+"test/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(s.getBytes().length);
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                conn.connect();
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(s.getBytes());
                //clean up
                os.flush();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute (Void result){

        }
    }
}
