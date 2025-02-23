package com.github.aaronanderson.okta.h2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("h2-connector")
public class H2ConnectorConfig {

	private final Map<String, Instance> instances = new HashMap<>();

	public Map<String, Instance> getInstances() {
		return instances;
	}
	

	public static class Instance {
		private String name;
		private String jdbc_url;
		private String jdbc_username;
		private String jdbc_password;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getJdbc_url() {
			return jdbc_url;
		}

		public void setJdbc_url(String jdbc_url) {
			this.jdbc_url = jdbc_url;
		}

		public String getJdbc_username() {
			return jdbc_username;
		}

		public void setJdbc_username(String jdbc_username) {
			this.jdbc_username = jdbc_username;
		}

		public String getJdbc_password() {
			return jdbc_password;
		}

		public void setJdbc_password(String jdbc_password) {
			this.jdbc_password = jdbc_password;
		}

	}

}
