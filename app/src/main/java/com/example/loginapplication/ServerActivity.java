package com.example.loginapplication;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerActivity extends AppCompatActivity implements View.OnTouchListener,
        AdapterView.OnItemClickListener{

    private RelativeLayout layout_server;
    private EditText et_address;
    private ListPopupWindow lpw;
    private List<String> list;
    private SharedPreferences address_sp;
    private int count = 0;
    private Set<String> set;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_server);

        layout_server = (RelativeLayout)findViewById(R.id.layout_server);
        layout_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击EditText以外的地方隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        //创建名为"address"的xml文件
        address_sp = getSharedPreferences("address",Context.MODE_PRIVATE);

        set = new HashSet<String>();

        et_address = (EditText) findViewById(R.id.et_address);
        et_address.setOnTouchListener(this);

        list = new ArrayList<String>();
        getAddress();

        lpw = new ListPopupWindow(this);
        lpw.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list));

        lpw.setAnchorView(et_address);
        lpw.setModal(true);
        lpw.setOnItemClickListener(this);

        SharedPreferences address_sp = getSharedPreferences("address", Context.MODE_PRIVATE);
        String address = address_sp.getString("address0","");
        if (!TextUtils.isEmpty(address)){
            et_address.setText(address);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
            /*if (!TextUtils.isEmpty(et_address.getText().toString().trim())){
                String address0 = et_address.getText().toString().trim();
                Intent intent = new Intent(ServerActivity.this,LoginActivity.class);
                intent.putExtra("address",address0);
                startActivity(intent);
                finish();
            }*/
    }

    private void getAddress(){
        for (int i=0;i<address_sp.getInt("addressCount",0);i++){
            String name = "address"+i;
            System.out.println("bbbb:"+address_sp.getString(name,""));
            list.add(address_sp.getString(name,""));
        }
    }

    public void click(View view){

        //先遍歷address的xml文件 存到set集合中，以便下一步的查重
        for (int i=0;i<address_sp.getInt("addressCount",0);i++){
            String name = "address"+i;
            System.out.println("bbbb:"+address_sp.getString(name,""));
            set.add(address_sp.getString(name,""));
        }

        String address = et_address.getText().toString().trim();

        //保存用户输入的服务器地址
        SharedPreferences.Editor editor = address_sp.edit();

        Boolean isRepeat = set.add(address);
        if (isRepeat){//服务器地址不重复则保存
            for (String str : set){
                String name = "address"+count;
             System.out.println("aaaaaa:"+str);
                editor.putString(name,str);
                count++;
            }
            System.out.println("set.size():"+set.size());
            editor.putInt("addressCount",set.size());
            editor.commit();
        }

        Intent intent = new Intent(ServerActivity.this,LoginActivity.class);
        //intent.putExtra("address",address);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int DRAWABLE_RIGHT = 2;

        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
            if (motionEvent.getX() >= (view.getWidth() - ((EditText)view)
                            .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())){
                lpw.show();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String item = list.get(i);
        String temp = "";
        for (int a=0;a<i;a++){
            temp = list.get(0);
            list.remove(0);
            list.add(temp);
        }
        et_address.setText(item);
        lpw.dismiss();
    }
}
