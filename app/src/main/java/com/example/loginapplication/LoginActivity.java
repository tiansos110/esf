package com.example.loginapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private RelativeLayout layout_rootview;
    private Button btn_back;
    private XCRoundImageView xciv_head;
    private EditText et_name;
    private EditText et_pwd;
    private Button btn_login;
    private RadioGroup radiogroup;
    private RadioButton rb_pwd;
    private RadioButton rb_login;
    private String name;
    private String pwd;
    private String address;
    private String json_username;
    private Handler handler = new Handler();
    private Boolean isSuccess;
    private SharedPreferences cookie_sp;
    private SharedPreferences user_sp;
    private SharedPreferences.Editor user_sp_editor;
    private SharedPreferences address_sp;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建名为"cookie"的xml文件
        cookie_sp = getSharedPreferences("cookie", Context.MODE_PRIVATE);
        //创建名为"user"的xml文件
        user_sp = getSharedPreferences("user", Context.MODE_PRIVATE);
        user_sp_editor = user_sp.edit();
        //创建名为"address"的xml文件
        address_sp = getSharedPreferences("address",Context.MODE_PRIVATE);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        layout_rootview = (RelativeLayout)findViewById(R.id.layout_rootview);
        btn_back = (Button)findViewById(R.id.btn_back);
        //找到頭像
        xciv_head = (XCRoundImageView)findViewById(R.id.xciv_head);
        //判断是否已设置头像
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures/","my_head.jpg");
        if (file.exists()){
            xciv_head.setImageURI(Uri.fromFile(file));
        }

        et_name = (EditText)findViewById(R.id.et_name);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn_login = (Button)findViewById(R.id.btn_login);

        //偏好設置
        radiogroup = (RadioGroup)findViewById(R.id.rg);
        rb_pwd = (RadioButton)findViewById(R.id.rb_pwd);
        rb_login = (RadioButton)findViewById(R.id.rb_login);

        //Intent intent = getIntent();
        address = address_sp.getString("address0",""); //得到輸入的服務器地址


        layout_rootview.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        //radiogroup.setOnCheckedChangeListener(new MyCheckedChangeListener());
        btn_login.setOnClickListener(this);

        //如果用戶之前勾選了記住密碼
        if (user_sp.getBoolean("isPwd",false)){
            System.out.println("記住密碼");
            rb_pwd.setChecked(true);

            String n = user_sp.getString("name","");
            String p = user_sp.getString("pwd","");

            if (!(TextUtils.isEmpty(n)) && !(TextUtils.isEmpty(p))){
                et_name.setText(n);
                et_pwd.setText(p);
            }
        }
        //如果用戶之前勾選了自動登陸
        if(user_sp.getBoolean("isLogin",false)){

            rb_login.setChecked(true);

            name = user_sp.getString("name","");
            pwd = user_sp.getString("pwd","");

            if (!(TextUtils.isEmpty(name)) && !(TextUtils.isEmpty(pwd))){
                et_name.setText(name);
                et_pwd.setText(pwd);
            }

            dialog = CustomProgressDialog.show(LoginActivity.this,null,false,null);
            connect();
        }

    }


    private void setProgressDialog(){

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                dialog.dismiss();

                if (isSuccess){
                    user_sp_editor.putString("json_username",json_username);
                    user_sp_editor.commit();

                    Toast.makeText(getApplicationContext(),"登陸成功",Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this,IndexActivity.class);
                    startActivity(intent);
                    finish();
                }else {

                    //登陸失敗則把數據清掉

                    SharedPreferences.Editor editor = cookie_sp.edit();
                    editor.clear();
                    editor.commit();

                    SharedPreferences.Editor editor2 = user_sp.edit();
                    editor2.clear();
                    editor2.commit();

                    SharedPreferences.Editor editor3 = address_sp.edit();
                    editor3.clear();
                    editor3.commit();

                    Toast.makeText(getApplicationContext(),"登陸失败",Toast.LENGTH_LONG).show();
                }
            }
        },1000);
    }


    private void connect(){
        new Thread(){
            @Override
            public void run() {

                //网络连接的url
                String url = "http://"+address+"/learning/j_spring_security_check_api";

                //创建OkhttpClient实例 并通过cookieJar自动化管理返回的cookie 用cookie来记录用户登录状态
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .cookieJar(new CookieJar() {
                            private Map<String,List<Cookie>> cookieStore = new HashMap<>();

                            //从响应中获取cookie 通过SharedPreferences保存在xml中
                            @Override
                            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

                                //以服务器域名为键 将cookie暂存在Map中
                                cookieStore.put(url.host(),cookies);
                                List<Cookie> list = cookieStore.get(url.host());

                                Cookie cookie = list.get(0);
                                System.out.println("cookieStr:"+cookie.toString());//打印cookie字符串

                                //保存在名为"cookie"的xml中
                                SharedPreferences.Editor editor = cookie_sp.edit();

                                System.out.println("persistent():"+cookie.persistent());
                                //保存用户名
                                //editor.putString("username",name);
                                //将cookie的主要参数取出并保存
                                editor.putString("domain",cookie.domain());
                                editor.putString("name",cookie.name());
                                editor.putString("value",cookie.value());
                                editor.putString("path",cookie.path());
                                editor.putBoolean("httpOnly",cookie.httpOnly());
                                editor.putBoolean("isExist",true);//判断xml文件是否有录入参数

                                editor.commit();
                            }

                            //进行请求时携带cookie
                            @Override
                            public List<Cookie> loadForRequest(HttpUrl url) {
                                System.out.println("loadForRequest");
                                /*List<Cookie> cookies = cookieStore.get(url.host());

                                return cookies != null ? cookies : new ArrayList<Cookie>();*/
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


                //构造请求体
                FormBody body = new FormBody.Builder()
                        .add("j_username",name)
                        .add("j_password",pwd)
                        .build();

                //以post方式进行请求
                final Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                //进行异步网络请求
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //登陆失败
                        isSuccess = false;
                        System.out.println("onFailure!!!");
                        //更新ui
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //设置进度条
                                setProgressDialog();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response){

                        try {
                            if (response.isSuccessful()){

                                //登陆成功
                                isSuccess = true;
                                System.out.println("success!!!");

                                //Content-Type:text/html;charset=UTF-8
                                //Content-Language:zh-TW
                                byte[] b = response.body().bytes();
                                String responseStr = new String(b,"UTF-8");
                                System.out.println("返回數據:"+responseStr);
                                try {
                                    JSONObject jsonObject = new JSONObject(responseStr);
                                    json_username = jsonObject.getString("username");
                                    System.out.println("jusername:"+json_username);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }else {

                                //登陆失败
                                isSuccess = false;
                                System.out.println("failed!!!");
                            }

                            //更新ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //设置进度条
                                    setProgressDialog();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.layout_rootview:
                //点击EditText以外的地方隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            case R.id.btn_back:
                Intent intent = new Intent(LoginActivity.this,ServerActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_login:

                //記錄用戶偏好設置
                if (rb_pwd.isChecked()){//記住密碼
                    user_sp_editor.putBoolean("isPwd",true);
                    user_sp_editor.putBoolean("isLogin",false);
                }

                if (rb_login.isChecked()){//自動登陸
                    user_sp_editor.putBoolean("isPwd",false);
                    user_sp_editor.putBoolean("isLogin",true);
                }

                //获取用户输入用户名和密码
                name = et_name.getText().toString().trim();
                pwd = et_pwd.getText().toString().trim();

                //判断用户名或密码是否为空
                if (TextUtils.isEmpty(name)||TextUtils.isEmpty(pwd)){
                    Toast.makeText(getApplicationContext(),"用户名或密码为空",Toast.LENGTH_LONG).show();
                    return;
                }else {
                    //SharedPreferences.Editor editor = user_sp.edit();
                    user_sp_editor.putString("name",name);
                    user_sp_editor.putString("pwd",pwd);
                    //user_sp_editor.commit();
                }

                //开始网络请求时加载一个进度条
                dialog = CustomProgressDialog.show(LoginActivity.this,null,false,null);
                connect();
                break;
        }
    }
}
