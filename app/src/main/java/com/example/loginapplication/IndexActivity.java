package com.example.loginapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_menu;
    private ImageView iv_testroom,iv_practice,iv_lessons,iv_browse,iv_circular;
    private TextView tv_exit;
    private TextView tv_username;
    private ImageView iv_head;
    private DrawerLayout layout_index;
    private MyScrollView menu_main;
    private NavigationView menu_sideslip;
    private SharedPreferences cookie_sp;
    private SharedPreferences user_sp;
    //以下為更換頭像的
    private final int CHOOSE_PICTURE = 0;
    private final int TAKE_PICTURE = 1;
    private final int CROP_PICTURE = 2;
    private Uri tempUri;
    private Bitmap mbitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        cookie_sp = getSharedPreferences("cookie", Context.MODE_PRIVATE);
        user_sp = getSharedPreferences("user", Context.MODE_PRIVATE);


        btn_menu = (Button)findViewById(R.id.btn_menu);//按钮弹出侧滑菜单
        iv_testroom = (ImageView)findViewById(R.id.iv_testroom);//测验室按钮
        iv_practice = (ImageView)findViewById(R.id.iv_practice);//练习按钮
        iv_lessons = (ImageView)findViewById(R.id.iv_lessons);//功课按钮按钮
        iv_browse = (ImageView)findViewById(R.id.iv_browse);//浏览教案按钮
        iv_circular = (ImageView)findViewById(R.id.iv_circular);//校园通告按钮
        //整个界面的布局
        layout_index = (DrawerLayout)findViewById(R.id.layout_index);
        //自定義ScrollView
        menu_main = (MyScrollView)findViewById(R.id.menu_main);

        //侧滑菜单
        menu_sideslip = (NavigationView)findViewById(R.id.menu_sideslip);

        //找到侧滑菜单的头视图
        View headerView = menu_sideslip.getHeaderView(0);
        //头视图的退出登陆
        tv_exit = (TextView) headerView.findViewById(R.id.tv_exit);
        //头视图的显示登陆名
        tv_username = (TextView)headerView.findViewById(R.id.tv_username);
        //頭視圖的頭像
        iv_head = (ImageView)headerView.findViewById(R.id.iv_head);

        String user_name = user_sp.getString("json_username","");
        tv_username.setText(user_name);

        //设置点击监听
        tv_exit.setOnClickListener(this);
        btn_menu.setOnClickListener(this);
        iv_head.setOnClickListener(this);

        iv_testroom.setOnClickListener(this);
        iv_practice.setOnClickListener(this);
        iv_lessons.setOnClickListener(this);
        iv_browse.setOnClickListener(this);
        iv_circular.setOnClickListener(this);

        //给DrawerLayout设置监听器
        layout_index.addDrawerListener(new MyDrawerListener());

        //给侧滑菜单的菜单项设置点击监听
        menu_sideslip.setNavigationItemSelectedListener(new MyNavigationItemSelectedListener());

        //判断是否已设置头像
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures/","my_head.jpg");
        if (file.exists()){
            iv_head.setImageURI(Uri.fromFile(file));
        }
    }

    //按钮点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.tv_exit://退出登陸
                //退出前 清空cookie
                SharedPreferences.Editor editor = cookie_sp.edit();
                editor.clear();
                editor.commit();

                SharedPreferences.Editor editor2 = user_sp.edit();
                editor2.clear();
                editor2.commit();

                //刪除頭像
                File file = new File(Environment.getExternalStorageDirectory()+"/Pictures/","my_head.jpg");
                if (file.exists()){
                    file.delete();
                }

                Toast.makeText(getApplication(),"点击了退出登陆",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(IndexActivity.this,LoginActivity.class);
                startActivity(intent);
                IndexActivity.this.finish();
                break;
            case R.id.btn_menu://弹出侧滑菜单
                layout_index.openDrawer(menu_sideslip);
                break;
            case R.id.iv_testroom://测验室
                Toast.makeText(IndexActivity.this,"你点击了测验室",Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_practice://练习
                Toast.makeText(IndexActivity.this,"你点击了练习",Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_lessons://功课
                Toast.makeText(IndexActivity.this,"你点击了功课",Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_browse://浏览教案
                Toast.makeText(IndexActivity.this,"你点击了浏览教案",Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_circular://校园通告
                Toast.makeText(IndexActivity.this,"你点击了校园通告",Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_head://更換頭像
                showChoosePicDialog();
                break;
        }
    }

    //DrawerLayout的监听器
    private class MyDrawerListener implements DrawerLayout.DrawerListener{
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            //让主要内容随菜单的拉出移动
            menu_main.setX(drawerView.getWidth()*slideOffset);
        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }

    //NavigationView点击事件监听器
    private class MyNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.item_privacy://隐私声明菜单项
                    Toast.makeText(getApplication(),"点击了隐私声明",Toast.LENGTH_LONG).show();
                    break;
                case R.id.item_exit://退出软件菜单项

                    //Toast.makeText(getApplication(),"点击了提出軟件",Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(IndexActivity.this).setTitle("確定退出嗎？")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IndexActivity.this.finish();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();

                    break;
            }
            return true;
        }
    }

    /*
        以下201-307行代碼為更換頭像的操作
     */
    //点击图片弹出对话框 选择图片来源
    private void showChoosePicDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
        builder.setTitle("选择图片");
        builder.setNegativeButton("取消",null);
        String[] items = {"本地相册","拍照"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case CHOOSE_PICTURE://从图库中选择
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent,CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE://拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures/","my_head.jpg");
                        if (file.exists()) {
                            file.delete();
                        }
                        tempUri = Uri.fromFile(file);
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,tempUri);
                        startActivityForResult(openCameraIntent,TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.show();
    }

    //裁剪图片
    private void cutImage(Uri uri){

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop",true);

        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);

        intent.putExtra("outputX",120);
        intent.putExtra("outputY",120);

        intent.putExtra("return_data",true);
        startActivityForResult(intent,CROP_PICTURE);
    }
    //设置图片到控件上
    public void setImageToView(Intent data){
        String path = null;
        //android 4.4以上 data.getData()只返回“content://...”的uri,不再返回路径
        //用内容解析者解析 可获得裁剪后图片路径
        Cursor cursor = getContentResolver().query(data.getData(),null,null,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        //根据路径取出图片
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            mbitmap = BitmapFactory.decodeStream(fis);
            iv_head.setImageBitmap(mbitmap);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case CHOOSE_PICTURE:
                    cutImage(data.getData());
                    break;
                case TAKE_PICTURE:
                    cutImage(tempUri);
                    break;
                case CROP_PICTURE:
                    if (data!=null){
                        setImageToView(data);

                        //将图片重命名存好
                        try {
                            File file = new File(Environment.getExternalStorageDirectory()+"/Pictures/","my_head.jpg");
                            if (file.exists()){
                                file.delete();
                            }
                            FileOutputStream fos = new FileOutputStream(file);
                            mbitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                            fos.flush();
                            fos.close();
                            //通知图库更新
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(file));
                            IndexActivity.this.sendBroadcast(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    //示范如何携带cookie进行连接（可删）
    private void connect(){
        new Thread(){
            @Override
            public void run() {

                String url = "http://202.175.64.187:8080/learning/servlet/learning/api/login";

                //OkHttpClient
                OkHttpClient mClient = new OkHttpClient.Builder()
                        .cookieJar(new CookieJar() {
                            @Override
                            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

                            }

                            //进行请求时执行该方法 若cookie不为空则携带cookie
                            @Override
                            public List<Cookie> loadForRequest(HttpUrl url) {
                                List<Cookie> cookies = new ArrayList<Cookie>();

                                //把cookie的主要参数拿出
                                String domain = cookie_sp.getString("domain","");
                                String name = cookie_sp.getString("name","");
                                String value = cookie_sp.getString("value","");
                                String path = cookie_sp.getString("path","");
                                Boolean httpOnly = cookie_sp.getBoolean("httpOnly",false);

                                if (!TextUtils.isEmpty(domain)&&!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(value)&&!TextUtils.isEmpty(path)&&httpOnly){
                                    //把从xml文件中取出的参数组装成cookie
                                    Cookie cookie = new Cookie.Builder()
                                            .domain(domain)
                                            .name(name)
                                            .value(value)
                                            .path(path)
                                            .httpOnly()
                                            .build();
                                    System.out.println("cookieString:"+cookie.toString());//打印cookie字符串

                                    cookies.add(cookie);
                                }

                                return cookies != null ? cookies : new ArrayList<Cookie>();
                            }
                        })
                        .build();
                //以get方式进行请求
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try {
                    Response response = mClient.newCall(request).execute();

                    if (response.isSuccessful()){
                        System.out.println("返回數據:"+response.body().string());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
