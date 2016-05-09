package com.example.nguyenthanhvinh_5120.myprojects;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    String localhost = "http://serverandroid.esy.es/androidapi/";//link server database
    static String string = "";

    //Hàm encryptMD5 giúp mã hóa MD5 password
    public static String encryptMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Gán các thuộc tính từ layout
        Button loginBtn = (Button)findViewById(R.id.loginBtn);
        Button registerBtn = (Button)findViewById(R.id.registerBtn);

        final EditText username =(EditText)findViewById(R.id.usernameEditText);
        final EditText password=(EditText)findViewById(R.id.passEditText);

        //Xử lý sự kiện click button Đăng nhập
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please retype username or password", Toast.LENGTH_SHORT).show();
                }
                else {

                    MyDownloadTask t = new MyDownloadTask();
                    t.execute();

                }
            }
        });

        //Xử lý sự kiện click button Đăng ký
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("USERNAME", username.getText().toString());
                intent.putExtra("PASSWORD", password.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    /*Class MyDownloadTask là AsyncTask tạo liên kết với Database
    lấy thông tin (name, password) kiểm tra để đăng nhập*/
    class MyDownloadTask extends AsyncTask<Void,Void,Void> {

        protected void onPreExecute() {
            //display progress dialog.

        }

        protected Void doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            //Tạo liên kết đến file result.php trên Database lấy name và password về
            try {
                url = new URL(localhost+"result.php");


                urlConnection = (HttpURLConnection) url
                        .openConnection();

                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                string = bufferedReader.readLine();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }
            return null;
        }
        protected void onPostExecute (Void result){
            String pass = "", email = "";
            EditText un = (EditText)findViewById(R.id.usernameEditText);
            EditText pw = (EditText)findViewById(R.id.passEditText);

            //Lấy name, password từ người dùng (người dùng nhập vào)
            final String uname = un.getText().toString();
            final String pword = encryptMD5(pw.getText().toString());

            //Lấy name, password từ server
            try {
                JSONArray mang = new JSONArray(string);

                for (int i=0; i< mang.length();i++) {
                    JSONObject obj = mang.getJSONObject(i);
                    if(uname.equals(obj.getString("name"))){
                        pass = obj.getString("password");
                        email = obj.getString("email");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //So sánh nếu trùng thì chuyển sang MainActivity còn không nhắc nhở
            if(pword.equals(pass)){
                Intent it = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", uname);
                bundle.putString("email", email);
                it.putExtra("account",bundle);
                startActivity(it);
                finish();
            }
            else Toast.makeText(LoginActivity.this, "Username or password don't match!", Toast.LENGTH_SHORT).show();
        }

    }
}
