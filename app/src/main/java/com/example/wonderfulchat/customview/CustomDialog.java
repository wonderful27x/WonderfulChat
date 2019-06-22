package com.example.wonderfulchat.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.wonderfulchat.R;

public class CustomDialog extends Dialog {

    private ConfirmClickListener listener;
    private DefuEditText parameter1;
    private DefuEditText parameter2;
    private TextView parameterNote1;
    private TextView parameterNote2;
    private ImageView imageView1;
    private ImageView imageView2;
    private Button confirm;

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomDialog(Context context, boolean cancelable,DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_layout);

        //设置弹窗的宽度
        WindowManager manager = getWindow().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        Point size = new Point();
        display.getSize(size);
        params.width = (int)(size.x * 0.85);//是dialog的宽度为app界面的80%
        getWindow().setAttributes(params);

        parameter1 = findViewById(R.id.parameter1);
        parameter2 = findViewById(R.id.parameter2);
        parameterNote1 = findViewById(R.id.parameter_note1);
        parameterNote2 = findViewById(R.id.parameter_note2);
        imageView1 = findViewById(R.id.image1);
        imageView2 = findViewById(R.id.image2);
        confirm = findViewById(R.id.confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPass = parameter1.getText().toString();
                String newPass = parameter2.getText().toString();
                listener.parameterPass(oldPass,newPass);
            }
        });
    }

    public interface ConfirmClickListener{
        public void parameterPass(String parameter1,String parameter2);
    }

    public void setConfirmClickListener(ConfirmClickListener listener){
        this.listener = listener;
    }

    public void setParameterNote(String note1,String note2){
        parameterNote1.setText(note1);
        parameterNote2.setText(note2);
    }

    public void setImage(int imageResource1,int imageResource2){
        if (imageResource1 != -1){
            imageView1.setImageResource(imageResource1);
        }
        if (imageResource2 != -1){
            imageView2.setImageResource(imageResource2);
        }
    }

    public void lineHideShow(boolean show1,boolean show2){
        if (show1){
            parameterNote1.setVisibility(View.VISIBLE);
            parameter1.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.VISIBLE);
        }else {
            parameterNote1.setVisibility(View.GONE);
            parameter1.setVisibility(View.GONE);
            imageView1.setVisibility(View.GONE);
        }
        if (show2){
            parameterNote2.setVisibility(View.VISIBLE);
            parameter2.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);
        }else {
            parameterNote2.setVisibility(View.GONE);
            parameter2.setVisibility(View.GONE);
            imageView2.setVisibility(View.GONE);
        }
    }

    public void imageHideShow(boolean show1,boolean show2){
        if (show1){
            imageView1.setVisibility(View.VISIBLE);
        }else {
            imageView1.setVisibility(View.GONE);
        }
        if (show2){
            imageView2.setVisibility(View.VISIBLE);
        }else {
            imageView2.setVisibility(View.GONE);
        }
    }

    public void setConfirmText(String text){
        confirm.setText(text);
    }

}
