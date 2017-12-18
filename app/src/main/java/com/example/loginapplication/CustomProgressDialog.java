package com.example.loginapplication;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(@NonNull Context context) {
        super(context);
    }

    public CustomProgressDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public static CustomProgressDialog show(Context context,CharSequence message,boolean cancelable,OnCancelListener cancelListener){

        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.Custom_Progress);

        dialog.setTitle("");
        dialog.setContentView(R.layout.layout_progress_dialog);

        if (message == null || message.length() == 0){
            dialog.findViewById(R.id.tv_message).setVisibility(View.GONE);
        }else {
            TextView tv_message = (TextView)dialog.findViewById(R.id.tv_message);
            tv_message.setText(message);
        }
        //按返回键是否取消
        dialog.setCancelable(cancelable);
        //监听返回键处理
        dialog.setOnCancelListener(cancelListener);
        //设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        //设置背景层透明度
        lp.dimAmount = 0.1f;
        dialog.getWindow().setAttributes(lp);
        dialog.show();

        return dialog;
    }

}
