package com.example.wonderfulchat.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.example.wonderfulchat.R;

/**
 * @Author wonderful
 * @Description 加载弹窗
 * @Date 2019-8-30
 */
public class LoadingDialog extends Dialog {

    private AnimationDrawable animationDrawable;

    public LoadingDialog(Context context) {
        this(context,R.style.LoadingDialogTheme);
    }

    protected LoadingDialog(Context context, int theme) {
        super(context,theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_dialog_layout);

        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(params);

        ImageView imageView = findViewById(R.id.loadImage);
        animationDrawable = (AnimationDrawable)imageView.getDrawable();
        setCancelable(false);

    }

    public void dialogShow(){
        show();
        if (!animationDrawable.isRunning()){
            animationDrawable.start();
        }
    }

    public void dialogDismiss(){
        dismiss();
        if (animationDrawable.isRunning()){
            animationDrawable.stop();
        }
    }
}
