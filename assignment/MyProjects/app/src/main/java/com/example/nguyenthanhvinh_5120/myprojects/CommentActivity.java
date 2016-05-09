package com.example.nguyenthanhvinh_5120.myprojects;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {

    String localhost = "http://serverandroid.esy.es/androidapi/";//link server database
    ArrayList<CommentEntity> arrList = new ArrayList<CommentEntity>();
    CommentArrayAdapter adapter2 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //Khai báo biến và gán các thuộc tính từ layout
        final int id;
        final String name;
        TextView tvUserComm = (TextView)findViewById(R.id.tvUserComment);
        final EditText edtComm = (EditText)findViewById(R.id.edtComment);
        Button btnComm2 = (Button)findViewById(R.id.btnComment2);
        ListView lvComm = (ListView)findViewById(R.id.lvComment);

        //Lấy id, name từ MainActivity
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            id = extras.getInt("ID");
            name = extras.getString("NAME");
        }else{
            id = 0;
            name = null;
        }

        //Khởi tạo AsyncTask GetComment
        GetComment gc = new GetComment(id);

        //set text TextView tvUserComm
        tvUserComm.setText(name);

        //Khởi tạo adapter adapter2 đưa vào ListView
        adapter2 = new CommentArrayAdapter(this, R.layout.list_item_2, arrList){
            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        lvComm.setAdapter(adapter2);

        //Xử lý sự kiện click button Bình luận
        btnComm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lấy bình luận
                String com = edtComm.getText().toString();
                edtComm.setText("");
                //Lấy thời gian đăng bài
                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();
                String timeNow = today.format("%k:%M:%S") + " " + today.monthDay + "/" + today.month + "/" + today.year;
                PostComment pc = new PostComment(id, name, timeNow);
                if (!com.equals("")) pc.execute(com);
                CommentEntity cel = new CommentEntity(name, com, timeNow);
                arrList.add(cel);
                adapter2.notifyDataSetChanged();
            }
        });
        gc.execute();//Gọi AsyncTask GetComment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment, menu);
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

    /*Class CommentArrayAdapter là ArrayAdapter giúp Custom Layout cho ListView*/
    class CommentArrayAdapter extends ArrayAdapter<CommentEntity> {
        Activity context=null;
        ArrayList<CommentEntity> myArray=null;
        int layoutId;

        public CommentArrayAdapter(Activity context, int layoutId, ArrayList<CommentEntity> arr){
            super(context, layoutId, arr);
            this.context=context;
            this.layoutId=layoutId;
            this.myArray=arr;
        }

        class ViewHolder{
            TextView tvName;
            TextView tvComment;
            TextView tvTime;
            ViewHolder(View cv){
                //Gán các thuộc tính từ layout
                tvName = (TextView) cv.findViewById(R.id.tvName);
                tvComment = (TextView) cv.findViewById(R.id.tvComment);
                tvTime = (TextView) cv.findViewById(R.id.tvTimeComm);
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if(convertView==null)
            {
                LayoutInflater inflater = context.getLayoutInflater();
                convertView=inflater.inflate(layoutId, null);
                holder = new ViewHolder(convertView);

                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder) convertView.getTag();
            }
            if(myArray.size()>0 && position>=0)
            {
                //Khai báo biến và gán các thuộc tính từ layout
                final CommentEntity ce = myArray.get(position);

                //set text các thành phần layout của các item của ListView
                holder.tvComment.setText(ce.getComment()+"  ");
                holder.tvName.setText(ce.getName());
                holder.tvTime.setText(ce.getTime());
            }
            return convertView;
        }
    }

    /*Class PostComment là AsyncTask tạo liên kết với Database
    đưa bình luận (baidang_id, name, comment) lên Database trên server*/
    class PostComment extends AsyncTask<String,Void,Void> {

        //Khai báo biến
        int id;
        String name, time;

        //Lấy id, name
        PostComment(int id, String name, String time){
            this.id = id;
            this.name = name;
            this.time = time;
        }

        protected void onPreExecute() {
            //display progress dialog.

        }
        protected Void doInBackground(String... str) {
            URL url;

            //Đưa lên server thông qua file index.php trong thư mục postcomment/ trên server
            try {
                JSONObject json = new JSONObject();
                json.put("baidang_id",id);
                json.put("name",name);
                json.put("comment", str[0]);
                json.put("time",time);
                String s = json.toString();
                url = new URL(localhost + "postcomment/");
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

    /*Class GetComment là AsyncTask tạo liên kết với Database
    lấy bình luận (baidang_id, name, comment) về đưa vào ListView*/
    class GetComment extends AsyncTask<Void,Void,Void> {

        //Khai báo biến
        ProgressDialog pd;
        int id;

        //Lấy id
        GetComment(int id){
            this.id = id;
        }

        protected void onPreExecute() {
            //Hiển thị progress dialog.
            pd = ProgressDialog.show(CommentActivity.this, "Vui lòng đợi", "Đang tải...", true);

        }

        protected Void doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            String str;

            //tạo liên kết đến file getcomment.php trên Database lấy bình luận về
            try {
                url = new URL(localhost + "getcomment.php");


                urlConnection = (HttpURLConnection) url
                        .openConnection();

                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                str = bufferedReader.readLine();

            } catch (Exception e) {
                e.printStackTrace();
                str = null;
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }



            try {
                JSONArray mang = new JSONArray(str);

                //Lấy baidang_id, name, comment từ server đưa vào CommentEntity
                for (int i=0; i< mang.length();i++) {
                    JSONObject obj = mang.getJSONObject(i);
                    if(id == obj.getInt("baidang_id")) {
                        String name = obj.getString("name");
                        String com = obj.getString("comment");
                        String time = obj.getString("time");
                        CommentEntity ce = new CommentEntity(name, com, time);
                        arrList.add(ce);//Thêm vào arrList
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute (Void result){
            //Cập nhật lại ListView
            adapter2.notifyDataSetChanged();
            if(pd.isShowing()) pd.dismiss();//Dừng hiển thị progress dialog
        }
    }

}
