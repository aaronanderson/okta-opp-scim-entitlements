package com.github.aaronanderson.okta.h2;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public final class OktaTest {

	//Add these Java System properties to the test runner, i.e. -Dokta.url=https://acme.okta.com -Dokta.token=
	private static final String PREVIEW_URL = System.getProperty("okta.url");
	private static final String PREVIEW_TOKEN = System.getProperty("okta.token");

	private static Client client;

	private static WebTarget baseURL;

	@BeforeAll
	static void setup() throws Exception {
		client = ClientBuilder.newBuilder().register(new OktaAuthenticator(PREVIEW_TOKEN)).register(new OktaRateGovernor()).build();
		baseURL = client.target(PREVIEW_URL);
	}

	//@Test
	void listEntitlements() throws Exception {
		String resourceId = "0oakkshfhqCrs20Pt1d7";

		ObjectMapper om = new ObjectMapper();
		ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

		WebTarget request = baseURL.path("/governance/api/v1/entitlements").queryParam("filter", String.format("parent.externalId eq \"%s\" AND parent.type eq \"APPLICATION\"", resourceId));
		String nextURL = request.getUri().toString();
		while (nextURL != null) {
			request = client.target(nextURL);
			Response response = request.request().header("Accept", "application/json").get();
			List<Object> links = response.getHeaders().get("link");
			if (links != null && links.size() == 2) {
				nextURL = (String) links.get(0);
				nextURL = nextURL.substring(1, nextURL.indexOf(";") - 1);
			} else {
				nextURL = null;
			}
			ObjectNode results = response.readEntity(ObjectNode.class);
			ArrayNode data = (ArrayNode) results.get("data");
			for (JsonNode entry : data) {
				ObjectNode entryNode = (ObjectNode) entry;
				System.out.println(writer.writeValueAsString(entryNode));
			}
		}
	}

	//@Test
	//Non functional due to error "Entitlements cannot be modified when Provisioning activated." Leaving for future reference.
	void updateLicenseEntitlement() throws Exception {
		String resourceId = "0oakkshfhqCrs20Pt1d7";

		ObjectMapper om = new ObjectMapper();
		ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

		WebTarget request = baseURL.path("/governance/api/v1/entitlements").queryParam("filter", String.format("parent.externalId eq \"%s\" AND parent.type eq \"APPLICATION\"", resourceId));
		Response response = request.request().header("Accept", "application/json").get();
		List<Object> links = response.getHeaders().get("link");
		ObjectNode results = response.readEntity(ObjectNode.class);
		ArrayNode data = (ArrayNode) results.get("data");
		for (JsonNode entry : data) {
			ObjectNode entryNode = (ObjectNode) entry;
			//System.out.println(writer.writeValueAsString(entryNode));
			if ("license".equals(entryNode.get("name").asText())) {
				String entitlementId = entryNode.get("id").asText();
				//get the entitlement values
				request = baseURL.path("/governance/api/v1/entitlements").path(entitlementId).path("values").queryParam("limit", "200");
				response = request.request().header("Accept", "application/json").get();
				results = response.readEntity(ObjectNode.class);
				data = (ArrayNode) results.get("data");

				entryNode.set("multiValue", BooleanNode.TRUE);
				entryNode.set("dataType", new TextNode("string"));
				entryNode.set("values", data);

				System.out.format("License update: %s\n", writer.writeValueAsString(entryNode));
				request = baseURL.path("/governance/api/v1/entitlements").path(entitlementId);
				response = request.request().header(HttpHeaders.ACCEPT, "application/json").put(Entity.entity(entryNode, "application/json"));
				System.out.format("Status of setting license as single value: %d %s\n", response.getStatus(), response.readEntity(String.class));
				break;
			}
		}

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
