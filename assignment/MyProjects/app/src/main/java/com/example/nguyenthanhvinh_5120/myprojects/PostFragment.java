package com.example.nguyenthanhvinh_5120.myprojects;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements OnClickListener {

    String localhost = "http://serverandroid.esy.es/androidapi/";//link server database
    Button chonanh;
    ImageView xemanh;
    Button dang;
    EditText txtChk;
    String username = null, timeNow, ba1, nd = "";
    Bitmap selectedBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_post, container, false);

        Bundle bundle = getArguments();
        username = bundle.getString("username");

        txtChk = (EditText) v.findViewById(R.id.edtText);

        chonanh = (Button) v.findViewById(R.id.btnSelect);
        xemanh = (ImageView) v.findViewById(R.id.ivImage);
        dang = (Button) v.findViewById(R.id.btnPost);

        //Xử lý sự kiện click button Chọn ảnh
        chonanh.setOnClickListener(this);

        return v;
    }

    //Sự kiện click button Chọn ảnh
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 0);//Mở ứng dụng Thư viện để Chọn ảnh và lấy Path của ảnh
    }

    //Hàm xử lý sự kiện lấy path ảnh
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == Activity.RESULT_OK && data != null) {
            String realPath = "";
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity(), data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(getActivity(), data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(getActivity(), data.getData());

            startPost(realPath);//Gọi hàm startPost truyền
        }
    }

    private void startPost(final String realPath) {

        //Chuyển path ảnh thành bitmap và hiển thị lên ImageView
        Picasso.with(getActivity()).load(new File(realPath)).into(xemanh);


        //Lấy text nội dung từ EditText
        nd = txtChk.getText().toString();

        //Lấy thời gian đăng bài
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        timeNow = today.format("%k:%M:%S") + " " + today.monthDay + "/" + (today.month + 1) + "/" + today.year;


        //Xử lý sự kiện click button Đăng bài
        dang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((nd == "") && (realPath == "")) Toast.makeText(getContext(), "Viết nội dung hoặc chọn ảnh muốn đăng!", Toast.LENGTH_SHORT).show();
                else
                {
                    String imagename = System.currentTimeMillis() + ".jpg";//Đặt tên ảnh bằng thời gian thực tính theo milisecond
                    selectedBitmap=getThumbnail(realPath);
                    uploadPictureToServer(imagename);//Gọi hàm đưa ảnh lên server

                    //Gọi AsyncTask đưa bài đăng lên server đồng thời thông báo hoàn thành
                    UploadTask task2 = new UploadTask();
                    task2.execute(imagename);
                    Toast.makeText(getContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                    Intent it = getActivity().getIntent();
                    getActivity().setResult(114, it);
                }
            }
        });
    }

    //Hàm đưa ảnh lên server
    private void uploadPictureToServer(String in) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        // Upload hình  lên server
        UploadImageTask uploadToServer=new UploadImageTask();
        uploadToServer.execute(in);
    }

    //Chuyển path hình thành Bitmap
    public Bitmap getThumbnail(String pathHinh)
    {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathHinh, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;
        int originalSize = (bounds.outHeight > bounds.outWidth) ?
                bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / 500;
        return BitmapFactory.decodeFile(pathHinh, opts);
    }

    /*Class UploadImageTask là AsyncTask tạo liên kết với Database
    đưa ảnh lên server*/
    class UploadImageTask extends AsyncTask<String,Void,Void> {
        protected void onPreExecute() {

        }
        protected Void doInBackground(String... str) {
            URL url;

            //Đưa ảnh lên server thông qua file index.php trên server
            try {
                JSONObject json = new JSONObject();
                json.put("base64", ba1);
                json.put("ImageName", str[0]);
                json.put("id", username);
                String s = json.toString();
                url = new URL(localhost);
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute (Void result){

        }
    }

    /*Class UploadTask là AsyncTask tạo liên kết với Database
    đưa bài đăng lên Database trên server*/
    class UploadTask extends AsyncTask<String,Void,Void> {

        String name,link,time,text;

        protected void onPreExecute() {
            //display progress dialog.

            //Lấy các thông tin của bài đăng
            name = username;
            time = timeNow;
            text = txtChk.getText().toString();

        }
        protected Void doInBackground(String... str) {
            URL url = null;
            link = username + str[0];

            //Đưa ảnh lên server thông qua file index.php trong thư mục upload/ trên server
            try {
                JSONObject json = new JSONObject();
                json.put("name",name);
                json.put("link",link);
                json.put("time",time);
                json.put("noidung",text);
                String s = json.toString();
                url = new URL(localhost + "upload/");
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute (Void result){

        }
    }

}

//Class RealPathUtil giúp lấy path ảnh
class RealPathUtil {

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.indexOf(":") > -1 ? wholeID.split(":")[1] : wholeID.indexOf(";") > -1 ? wholeID
                .split(";")[1] : wholeID;

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
