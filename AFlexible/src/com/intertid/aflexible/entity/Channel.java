package com.intertid.aflexible.entity;

public class Channel {

	private String contents;
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getChildrens() {
		return childrens;
	}
	public void setChildrens(String childrens) {
		this.childrens = childrens;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	private String childrens;
	private String channelId;
	
	

}
