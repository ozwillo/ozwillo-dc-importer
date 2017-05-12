package org.ozwillo.dcimporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModel {
	
	
	private String  display_id;
	private String  last_update_time;
	private String  display_name;
	private Submission submission;
	private String  url;
	private Fields fields;
	private String  receipt_time;
	private UserInformations user;
	private Integer  criticality_level;
	private String  id;
	
	
	public FormModel() {
		super();
	}


	public FormModel(String display_id, String last_update_time, String display_name, Submission submission, String url,
			Fields fields, String receipt_time, UserInformations user, Integer criticality_level, String id) {
		super();
		this.display_id = display_id;
		this.last_update_time = last_update_time;
		this.display_name = display_name;
		this.submission = submission;
		this.url = url;
		this.fields = fields;
		this.receipt_time = receipt_time;
		this.user = user;
		this.criticality_level = criticality_level;
		this.id = id;
	}


	public String getDisplay_id() {
		return display_id;
	}


	public void setDisplay_id(String display_id) {
		this.display_id = display_id;
	}


	public String getLast_update_time() {
		return last_update_time;
	}


	public void setLast_update_time(String last_update_time) {
		this.last_update_time = last_update_time;
	}


	public String getDisplay_name() {
		return display_name;
	}


	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}


	public Submission getSubmission() {
		return submission;
	}


	public void setSubmission(Submission submission) {
		this.submission = submission;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public Fields getFields() {
		return fields;
	}


	public void setFields(Fields fields) {
		this.fields = fields;
	}


	public String getReceipt_time() {
		return receipt_time;
	}


	public void setReceipt_time(String receipt_time) {
		this.receipt_time = receipt_time;
	}


	public UserInformations getUser() {
		return user;
	}


	public void setUser(UserInformations user) {
		this.user = user;
	}


	public Integer getCriticality_level() {
		return criticality_level;
	}


	public void setCriticality_level(Integer criticality_level) {
		this.criticality_level = criticality_level;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	@Override
	public String toString() {
		return "FormModel [display_id=" + display_id + ", last_update_time=" + last_update_time + ", display_name="
				+ display_name + ", submission=" + submission + ", url=" + url + ", fields=" + fields
				+ ", receipt_time=" + receipt_time + ", user=" + user + ", criticality_level=" + criticality_level
				+ ", id=" + id + "]";
	}

}
