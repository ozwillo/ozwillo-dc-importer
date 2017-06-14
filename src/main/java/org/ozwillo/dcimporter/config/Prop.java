package org.ozwillo.dcimporter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "publik")
public class Prop {

	private List<Map<String, String>> instance = new ArrayList<>();

	public List<Map<String, String>> getInstance() {
		return this.instance;
	}

	public Prop() {
		super();
	}

	public void setInstance(List<Map<String, String>> instance) {
		this.instance = instance;
	}

	@Override
	public String toString() {
		return "Prop [instance=" + instance + "]";
	}

}
