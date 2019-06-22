package com.example.wonderfulchat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageModel implements Parcelable {

    public static final int TYPE_RECEIVE = 0;
    public static final int TYPE_SEND = 1;
    private String sender;
    private String senderImage;
    private String senderAccount;
    private String receiver;
    private String receiverAccount;
    private String message;
    private String time;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sender);
        parcel.writeString(senderImage);
        parcel.writeString(senderAccount);
        parcel.writeString(receiver);
        parcel.writeString(receiverAccount);
        parcel.writeString(message);
        parcel.writeString(time);
        parcel.writeInt(type);
    }

    public static final Parcelable.Creator<MessageModel> CREATOR = new Parcelable.Creator<MessageModel>(){

        @Override
        public MessageModel createFromParcel(Parcel parcel) {
            MessageModel messageModel = new MessageModel();
            messageModel.sender = parcel.readString();
            messageModel.senderImage = parcel.readString();
            messageModel.senderAccount = parcel.readString();
            messageModel.receiver = parcel.readString();
            messageModel.receiverAccount = parcel.readString();
            messageModel.message = parcel.readString();
            messageModel.time = parcel.readString();
            messageModel.type = parcel.readInt();
            return messageModel;
        }

        @Override
        public MessageModel[] newArray(int i) {
            return new MessageModel[i];
        }
    };

}
