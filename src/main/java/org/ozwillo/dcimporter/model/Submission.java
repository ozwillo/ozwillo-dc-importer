package org.ozwillo.dcimporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Submission {
	
	
	private String  channel;
	private Boolean  backoffice;
	
	public Submission() {
		super();
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Boolean getBackoffice() {
		return backoffice;
	}

	public void setBackoffice(Boolean backoffice) {
		this.backoffice = backoffice;
	}

	@Override
	public String toString() {
		return "Submission [channel=" + channel + ", backoffice=" + backoffice + "]";
	}
	
	
}
