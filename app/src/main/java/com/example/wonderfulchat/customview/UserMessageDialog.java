package com.example.wonderfulchat.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.model.UserModel;

public class UserMessageDialog extends Dialog {

    private DialogClickListener dialogClickListener;
    private ImageView headImage;
    private DefuEditText account;
    private DefuEditText nickname;
    private DefuEditText remark;
    private DefuEditText lifeMotto;
    private Button save;
    private Button friendDelete;
    private UserModel userModel;
    private UserModel friendModel;
    private Context context;

    public UserMessageDialog(Context context, UserModel userModel,UserModel friendModel) {
        super(context);
        this.context = context;
        this.userModel = userModel;
        this.friendModel = friendModel;
    }

    public UserMessageDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected UserMessageDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_message_dialog_layout);

        //设置弹窗的宽度
        WindowManager manager = getWindow().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        Point size = new Point();
        display.getSize(size);
        params.width = (int)(size.x * 0.85);//是dialog的宽度为app界面的85%
        getWindow().setAttributes(params);

        headImage = findViewById(R.id.headImage);
        account = findViewById(R.id.account);
        nickname = findViewById(R.id.nickname);
        remark = findViewById(R.id.remark);
        lifeMotto = findViewById(R.id.lifeMotto);
        save = findViewById(R.id.save);
        friendDelete = findViewById(R.id.friendDelete);

        Glide.with(context).load(friendModel.getImageUrl()).into(headImage);
        account.setText(friendModel.getAccount());
        nickname.setText(friendModel.getNickname());
        remark.setText(friendModel.getRemark() == null ? "" : friendModel.getRemark());
        lifeMotto.setText(friendModel.getLifeMotto());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null){
                    UserModel model = new UserModel(friendModel);
                    model.setRemark(remark.getText().toString());
                    dialogClickListener.save(model);
                    dismiss();
                }
            }
        });

        friendDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null){
                    UserModel model = new UserModel(friendModel);
                    model.setRemark(remark.getText().toString());
                    dialogClickListener.deleteClick(model);
                }
            }
        });

        friendDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                UserModel model = new UserModel(friendModel);
                model.setRemark(remark.getText().toString());
                dialogClickListener.deleteLongClick(model);
                dismiss();
                return true;
            }
        });

    }

    public interface DialogClickListener{
        public void save(UserModel model);
        public void deleteClick(UserModel model);
        public void deleteLongClick(UserModel model);
    }

    public void setDialogClickListener(DialogClickListener dialogClickListener){
        this.dialogClickListener = dialogClickListener;
    }

}
