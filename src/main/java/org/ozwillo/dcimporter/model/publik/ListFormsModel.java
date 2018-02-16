package org.ozwillo.dcimporter.model.publik;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListFormsModel {
	
	private int id;

    private String url;
	
	private String last_update_time;
	
	private String receipt_time;
	
	
	
	public ListFormsModel() {
		super();
	}

	public ListFormsModel(int id, String url, String last_update_time, String receipt_time) {
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

	public String getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(String last_update_time) {
		this.last_update_time = last_update_time;
	}

	public String getReceipt_time() {
		return receipt_time;
	}

	public void setReceipt_time(String receipt_time) {
		this.receipt_time = receipt_time;
	}

	@Override
	public String toString() {
		return "Form "+id+" [id=" + id + ", url=" + url + ", last_update_time=" + last_update_time + ", receipt_time="
				+ receipt_time + "]";
	}

	
}
