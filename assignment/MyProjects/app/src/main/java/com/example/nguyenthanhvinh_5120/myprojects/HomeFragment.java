package com.example.nguyenthanhvinh_5120.myprojects;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    String localhost = "http://serverandroid.esy.es/androidapi/";//link server database
    String uname = "Username";
    ListView lvNew, lvTop;
    ArrayList<PostEntity> arrNew = new ArrayList<PostEntity>();
    ArrayList<PostEntity> arrTop = new ArrayList<PostEntity>();
    MyArrayAdapter adapterNew = null, adapterTop = null;
    TabHost host;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle bundle = getArguments();
        uname = bundle.getString("username");

        GetTask gt = new GetTask();//Khởi tạo AsyncTask GetTask

        host=(TabHost) v.findViewById(R.id.tabHost);
        host.setup();
        loadTabs(host);

        lvNew = (ListView) v.findViewById(R.id.lvNew);
        lvTop = (ListView) v.findViewById(R.id.lvTop);

        adapterNew = new MyArrayAdapter(getActivity(), R.layout.list_item, arrNew, uname);
        lvNew.setAdapter(adapterNew);

        adapterTop = new MyArrayAdapter(getActivity(), R.layout.list_item, arrTop, uname);
        lvTop.setAdapter(adapterTop);

        gt.execute();//Gọi AsyncTask GetTask

        return v;
    }

    //Cấu hình tab
    public void loadTabs(TabHost tab)
    {
        TabHost.TabSpec spec;
        spec=tab.newTabSpec("t1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Mới");
        tab.addTab(spec);

        spec=tab.newTabSpec("t2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Yêu Thích");
        tab.addTab(spec);


        tab.setCurrentTab(0);
    }

    /*Class MyArrayAdapter là ArrayAdapter giúp Custom Layout cho ListView*/
    class MyArrayAdapter extends ArrayAdapter<PostEntity> {
        Activity context=null;
        ArrayList<PostEntity> myArray=null;
        int layoutId;
        String uname;

        public MyArrayAdapter(Activity context, int layoutId, ArrayList<PostEntity> arr, String uname){
            super(context, layoutId, arr);
            this.context=context;
            this.layoutId=layoutId;
            this.myArray=arr;
            this.uname = uname;
        }

        class ViewHolder{
            TextView tvUserPost;
            TextView tvTime;
            TextView tvText;
            Button btnLike;
            TextView tvLike;
            Button btnComm;
            ViewHolder(View cv){
                //Gán các thuộc tính từ layout
                tvUserPost = (TextView) cv.findViewById(R.id.tvUserPost);
                tvTime = (TextView) cv.findViewById(R.id.tvTime);
                tvText = (TextView) cv.findViewById(R.id.tvText);
                btnLike = (Button) cv.findViewById(R.id.btnLike);
                tvLike = (TextView) cv.findViewById(R.id.tvLiked);
                btnComm = (Button) cv.findViewById(R.id.btnComment);
            }
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
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
                final PostEntity pe = myArray.get(position);

                //set text các layout của mỗi item của ListView
                holder.tvUserPost.setText(pe.getName());
                holder.tvTime.setText(pe.getTime());
                holder.tvText.setText(pe.getText());
                holder.tvLike.setText(pe.getLiked() + " người thích");

                //Kiểm tra xem người dùng đã thích chưa
                if (pe.getChk()){
                    holder.btnLike.setEnabled(false);
                    holder.btnLike.setText("Đã thích");
                }
                else{
                    holder.btnLike.setText("Thích");
                    holder.btnLike.setEnabled(true);
                }

                //Xử lý sự kiện click button Đăng ký thành viên
                holder.btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Gọi AsyncTask PostLike
                        PostLike pl = new PostLike(pe.getId(), uname);
                        pl.execute();
                        holder.btnLike.setEnabled(false);//Disable button
                        holder.btnLike.setText("Đã thích");//Chuyển text thành đã thích
                        holder.tvLike.setText(pe.getLiked() + 1 + " người thích");//Tăng lượt thích lên 1
                    }
                });

                //Hiển thị ảnh lên ImageView
                ImageView ivShow = (ImageView) convertView.findViewById(R.id.ivShow);
                Picasso.with(getActivity()).load(localhost + "images/" + pe.getLink()).into(ivShow);

                //Xử lý sự kiện click button Đăng ký thành viên
                //Chuyển sang CommentActivity đồng thời gửi bài đăng id và tên người dùng
                holder.btnComm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        intent.putExtra("ID", pe.getId());
                        intent.putExtra("NAME", uname);
                        startActivity(intent);
                    }
                });
            }
            return convertView;
        }

        /*Class PostLike là AsyncTask tạo liên kết với Database
        đưa thông tin like lên Database trên server*/
        class PostLike extends AsyncTask<Void,Void,Void> {

            //Khai báo biến
            int id;
            String name;

            //Lấy id, name
            PostLike(int id, String name){
                this.id = id;
                this.name = name;
            }

            protected void onPreExecute() {
                //display progress dialog.

                name = uname;

            }
            protected Void doInBackground(Void... v) {
                URL url;

                //Đưa lên server thông qua file index.php trong thư mục postlike/ trên server
                try {
                    JSONObject json = new JSONObject();
                    json.put("baidang_id",id);
                    json.put("name",name);
                    String s = json.toString();
                    url = new URL(localhost + "postlike/");
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


    /*Class GetTask là AsyncTask tạo liên kết với Database
    lấy bài đăng từ Database trên server hiển thị lên ListView*/
    class GetTask extends AsyncTask<Void,PostEntity,Void> {
        //Khai báo
        ProgressDialog pd;
        int index = 0;

        protected void onPreExecute() {
            //hiển thị progress dialog.
            pd = ProgressDialog.show(getActivity(), "Vui lòng đợi", "Đang tải...", true);

        }

        protected Void doInBackground(Void... params) {
            URL url1, url2, url3;
            HttpURLConnection urlConnection1 = null, urlConnection2 = null, urlConnection3 = null;
            String str1, str2, str3;

            //Tạo liên kết đến file getnew.php và gettop.php trên Database lấy bài đăng về
            //getnew.php sắp xếp theo thời gian
            //gettop.php sắp xếp theo lượt thích
            try {
                url1 = new URL(localhost + "getnew.php");
                url2 = new URL(localhost + "gettop.php");
                url3 = new URL(localhost + "getlike.php");

                urlConnection1 = (HttpURLConnection) url1
                        .openConnection();
                urlConnection2 = (HttpURLConnection) url2
                        .openConnection();
                urlConnection3 = (HttpURLConnection) url3
                        .openConnection();

                BufferedReader bufferedReader1=new BufferedReader(new InputStreamReader(urlConnection1.getInputStream()));
                BufferedReader bufferedReader2=new BufferedReader(new InputStreamReader(urlConnection2.getInputStream()));
                BufferedReader bufferedReader3=new BufferedReader(new InputStreamReader(urlConnection3.getInputStream()));

                str1 = bufferedReader1.readLine();
                str2 = bufferedReader2.readLine();
                str3 = bufferedReader3.readLine();

            } catch (Exception e) {
                e.printStackTrace();
                str1 = null;
                str2 = null;
                str3 = null;
            } finally {
                try {
                    urlConnection1.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                JSONArray mang1 = new JSONArray(str1);
                JSONArray mang2 = new JSONArray(str2);

                //Lấy thông tin bài đăng từ server đưa vào PostEntity
                for (int i=0; i< mang1.length();i++) {
                    boolean chk1 = false, chk2 = false;
                    JSONObject obj1 = mang1.getJSONObject(i);
                    JSONObject obj2 = mang2.getJSONObject(i);

                    int id1 = obj1.getInt("baidang_id");
                    String name1 = obj1.getString("name");
                    String link1 = obj1.getString("link");
                    String time1 = obj1.getString("time");
                    String noidung1 = obj1.getString("noidung");
                    int liked1 = obj1.getInt("liked");

                    int id2 = obj2.getInt("baidang_id");
                    String name2 = obj2.getString("name");
                    String link2 = obj2.getString("link");
                    String time2 = obj2.getString("time");
                    String noidung2 = obj2.getString("noidung");
                    int liked2 = obj2.getInt("liked");

                    try {
                        JSONArray mang3 = new JSONArray(str3);

                        for (int j=0; j< mang3.length();j++) {
                            JSONObject obj3 = mang3.getJSONObject(j);
                            if ((id1 == obj3.getInt("baidang_id")) && (uname.equals(obj3.getString("name")))) chk1 = true;
                            if ((id2 == obj3.getInt("baidang_id")) && (uname.equals(obj3.getString("name")))) chk2 = true;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    PostEntity pe1 = new PostEntity(id1, name1, time1, noidung1, link1, liked1, chk1);
                    PostEntity pe2 = new PostEntity(id2, name2, time2, noidung2, link2, liked2, chk2);

                    publishProgress(pe1, pe2);//Đưa vào onProgressUpdate xử lý
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(PostEntity... values) {
            //Thêm vào ArrayList và cập nhật lại ListView của 2 tab
            arrNew.add(values[0]);
            adapterNew.notifyDataSetChanged();
            arrTop.add(values[1]);
            adapterTop.notifyDataSetChanged();
            index++;
            if(pd.isShowing() && (index > 5)) pd.dismiss();//Dừng hiển thị progress dialog
        }

        protected void onPostExecute (Void result){
            if(pd.isShowing()) pd.dismiss();//Dừng hiển thị progress dialog
        }
    }

}
