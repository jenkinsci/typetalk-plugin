package org.jenkinsci.plugins.typetalk.api;

import com.google.api.client.util.Key;

public class MessageEntity {

	@Key
	private String message;

	@Key
	private Long[] talkIds;

	@Key
	private Long replyTo;

	@Key
	private String[] fileKeys;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long[] getTalkIds() {
		return talkIds;
	}

	public void setTalkIds(Long[] talkIds) {
		this.talkIds = talkIds;
	}

	public Long getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(Long replyTo) {
		this.replyTo = replyTo;
	}

	public String[] getFileKeys() {
		return fileKeys;
	}

	public void setFileKeys(String[] fileKeys) {
		this.fileKeys = fileKeys;
	}

}
