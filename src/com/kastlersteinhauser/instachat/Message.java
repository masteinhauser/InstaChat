package com.kastlersteinhauser.instachat;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Message extends IMessage {
	public Message(){}
	
	public int describeContents() {
		return 0;
	}

	// Write Message data to the passed in Parcel
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(super.getUser());
		dest.writeString(super.getMessage());
		dest.writeLong(super.getSentAsEpoch());
	}
	
	// Regenerate object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<IMessage> CREATOR = new Parcelable.Creator<IMessage>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public IMessage[] newArray(int size) {
            return new IMessage[size];
        }
    };

    // Constructor takes a Parcel and returns message populated with it's values
    private Message(Parcel in) {
        super.setUser(in.readString());
        super.setMessage(in.readString());
        super.setSent(new Date(in.readLong()));
    }
}
