package com.github.aaronanderson.okta.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public class OktaConfig {
	
	
	public static final String PREVIEW_URL = System.getProperty("okta_preview_url", "");
	public static final String PREVIEW_TOKEN = System.getProperty("okta_preview_token", "");
	
	public static final Client PREVIEW_CLIENT = ClientBuilder.newBuilder().register(new OktaAuthenticator(PREVIEW_TOKEN)).register(new OktaRateGovernor()).build();
	public static final WebTarget PREVIEW_BASE = PREVIEW_CLIENT.target(PREVIEW_URL);


	public static void main(String[] args) {
	}


	public static class OktaAuthenticator implements ClientRequestFilter {

		private final String token;

		public OktaAuthenticator(String token) {
			this.token = token;
		}

		public void filter(ClientRequestContext requestContext) throws IOException {
			MultivaluedMap<String, Object> headers = requestContext.getHeaders();
			headers.add("Authorization", "SSWS " + token);

		}

	}

	public static class OktaRateGovernor implements ClientRequestFilter {

		public OktaRateGovernor() {

		}

		public void filter(ClientRequestContext requestContext) throws IOException {
			
		}

	}

}
