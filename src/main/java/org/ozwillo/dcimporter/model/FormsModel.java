package org.ozwillo.dcimporter.model;

import java.util.Date;


public class FormsModel {
	
	private int id;

    private String url;
	
	private Date last_update_time;
	
	private Date receipt_time;
	
	
	
	public FormsModel() {
	}

	public FormsModel(int id, String url, Date last_update_time, Date receipt_time) {
		super();
		this.id = id;
		this.url = url;
		this.last_update_time = last_update_time;
		this.receipt_time = receipt_time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(Date last_update_time) {
		this.last_update_time = last_update_time;
	}

	public Date getReceipt_time() {
		return receipt_time;
	}

	public void setReceipt_time(Date receipt_time) {
		this.receipt_time = receipt_time;
	}

	@Override
	public String toString() {
		return "FormsModel [id=" + id + ", url=" + url + ", last_update_time=" + last_update_time + ", receipt_time="
				+ receipt_time + "]";
	}

	
}
