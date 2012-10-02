package com.kastlersteinhauser.instachat;

import java.util.Date;

import android.os.Parcelable;

/**
 * @author Myles Steinhauser <myles.steinhauser@gmail.com>
 *
 */
public abstract class IMessage implements Parcelable {
	protected String user;
	protected String message;
	protected Date sent;
	
	public IMessage(){}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getSent() {
		return sent;
	}
	public Long getSentAsEpoch() {
		if(sent != null){
			return sent.getTime();
		}
		return 0L;
	}
	public void setSent(Date sent) {
		this.sent = sent;
	}
}
