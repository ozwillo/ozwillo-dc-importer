package org.ozwillo.dcimporter.model.publik;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

	
	private String email;
	@JsonProperty("NameID")
	private String[] nameID;
	private Integer id;
	private String name;

	public User() {
		super();
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String[] getNameID() {
		return nameID;
	}
	public void setNameID(String[] nameID) {
		this.nameID = nameID;
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
		return "UserInformations [email=" + email + ", NameID=" + Arrays.toString(nameID) + ", id=" + id + ", name="
				+ name + "]";
	}
	
}
