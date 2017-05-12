package org.ozwillo.dcimporter.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInformations {

	
	private String  email;
	private String[] NameID;
	private Integer  id;
	private String  name;

	public UserInformations() {
		super();
	}
	public UserInformations(String email, String[] nameID, Integer id, String name) {
		super();
		this.email = email;
		this.NameID = nameID;
		this.id = id;
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String[] getNameID() {
		return NameID;
	}
	public void setNameID(String[] nameID) {
		this.NameID = nameID;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "UserInformations [email=" + email + ", NameID=" + Arrays.toString(NameID) + ", id=" + id + ", name="
				+ name + "]";
	}
	
}
