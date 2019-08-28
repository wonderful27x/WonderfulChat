package com.example.wonderfulchat.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wonderfulchat.R;
import com.example.wonderfulchat.model.UserModel;

public class FriendRequestDialog extends Dialog {

    private DialogClickListener dialogClickListener;
    private ImageView close;
    private ImageView headImage;
    private DefuEditText account;
    private DefuEditText nickname;
    private DefuEditText lifeMotto;
    private Button agree;
    private Button refuse;
    private UserModel friendModel;
    private int requestPosition;
    private RequestOptions options;
    private Context context;

    public FriendRequestDialog(Context context,UserModel friendModel,int requestPosition) {
        super(context);
        this.context = context;
        this.friendModel = friendModel;
        this.requestPosition = requestPosition;
    }

    public FriendRequestDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.friend_request_dialog_layout);

        //设置弹窗的宽度
        WindowManager manager = getWindow().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        Point size = new Point();
        display.getSize(size);
        params.width = (int)(size.x * 0.85);//是dialog的宽度为app界面的85%
        getWindow().setAttributes(params);

        close = findViewById(R.id.close);
        headImage = findViewById(R.id.headImage);
        account = findViewById(R.id.account);
        nickname = findViewById(R.id.nickname);
        lifeMotto = findViewById(R.id.lifeMotto);
        agree = findViewById(R.id.save);
        refuse = findViewById(R.id.friendDelete);

        options = new RequestOptions()
                .placeholder(R.drawable.default_head_image)
                .fallback(R.drawable.default_head_image)
                .error(R.drawable.default_head_image);

        Glide.with(context)
                .load(friendModel.getImageUrl())
                .apply(options)
                .into(headImage);

        account.setText(friendModel.getAccount());
        nickname.setText(friendModel.getNickname());
        lifeMotto.setText(friendModel.getLifeMotto());

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null){
                    dialogClickListener.agreeClick(friendModel,requestPosition);
                    dismiss();
                }
            }
        });

        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null){
                    dialogClickListener.refuseClick(friendModel,requestPosition);
                    dismiss();
                }
            }
        });

    }

    public interface DialogClickListener{
        public void agreeClick(UserModel model,int position);
        public void refuseClick(UserModel model,int position);
    }

    public void setDialogClickListener(DialogClickListener dialogClickListener){
        this.dialogClickListener = dialogClickListener;
    }

}
