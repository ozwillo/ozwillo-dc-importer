package org.ozwillo.dcimporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fields {

	
	private String  nom_famille;
	private String  prenom;
	private String  telephone;
	
	public Fields() {
		super();
	}

	public Fields(String nom_famille, String prenom, String telephone) {
		super();
		this.nom_famille = nom_famille;
		this.prenom = prenom;
		this.telephone = telephone;
	}

	public String getNom_famille() {
		return nom_famille;
	}

	public void setNom_famille(String nom_famille) {
		this.nom_famille = nom_famille;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Override
	public String toString() {
		return "Fields [nom_famille=" + nom_famille + ", prenom=" + prenom + ", telephone=" + telephone + "]";
	}
	
	
}
