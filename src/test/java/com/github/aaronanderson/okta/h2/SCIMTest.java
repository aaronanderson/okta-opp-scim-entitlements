package com.github.aaronanderson.okta.h2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.directory.scim.client.rest.ScimJacksonXmlBindJsonProvider;
import org.apache.directory.scim.client.rest.ScimUserClient;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import com.github.aaronanderson.okta.scim.OktaServiceProviderConfigResourceImpl.OktaProviderScimExtension;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

//TODO research further to see how test web server instance can be full loaded instead of isolated test class injection.
//This test class currently requires the SCIM server to be running locally. Comment in/out test methods as needed.
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SpringBootTest(classes = { SchemaRegistry.class, ScimJacksonXmlBindJsonProvider.class })
@ActiveProfiles("dev")
public final class SCIMTest {

	//@LocalServerPort
	//private int port;
	private int port = 8444;

	@Value("${server.ssl.key-store}")
	private Resource keystore;

	@Value("${server.ssl.key-store-password}")
	private String password;

	@Autowired
	private ScimJacksonXmlBindJsonProvider provider;

	@Autowired
	SchemaRegistry registry;

	private Client client;

	private String baseURL;

	@PostConstruct
	void setup() throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(keystore.getURL(), password.toCharArray()).build();
		client = ClientBuilder.newBuilder().register(provider).sslContext(sslContext).register(new BasicAuthenticator("db1", "okta")).build();
		baseURL = String.format("https://localhost:%d/scim/v2/", port);
		registry.addExtension(ServiceProviderConfiguration.class, OktaProviderScimExtension.class);
	}

	//@Test
	void getUser() throws ScimException {
		try (ScimUserClient userClient = new ScimUserClient(client, baseURL);) {
			testUserTemplate();
			Optional<ScimUser> scimUser = userClient.getById("2");
			if (scimUser.isPresent()) {
				System.out.format("%s\n", scimUser.get().toString());
			}
		}
	}

	//@Test
	void disableAccount() throws ScimException {
		try (ScimUserClient userClient = new ScimUserClient(client, baseURL);) {
			ScimUser scimUser = testUserTemplate();
			scimUser.setActive(false);
			userClient.update(scimUser.getId(), scimUser);
		}
	}

	//@Test
	void enableAccount() throws ScimException {
		try (ScimUserClient userClient = new ScimUserClient(client, baseURL);) {
			ScimUser scimUser = testUserTemplate();
			scimUser.setActive(true);
			userClient.update(scimUser.getId(), scimUser);
		}
	}

	private ScimUser testUserTemplate() {
		ScimUser scimUser = new ScimUser();
		scimUser.setId("2");
		scimUser.setUserName("jrambo@acme.com");
		Email workEmail = new Email();
		workEmail.setType("primary");
		workEmail.setPrimary(true);
		workEmail.setValue(scimUser.getUserName());
		scimUser.setEmails(List.of(workEmail));
		Name name = new Name();
		name.setGivenName("John");
		name.setFamilyName("Rambo");
		scimUser.setName(name);
		scimUser.setPassword("changeme");
		scimUser.setEntitlements(List.of());
		scimUser.setGroups(List.of());
		scimUser.setActive(true);
		return scimUser;
	}

	//@Test
	void serviceProviderConfig() {
		Response response = client.target(baseURL).path("ServiceProviderConfig").request().get();
		ServiceProviderConfiguration config = response.readEntity(ServiceProviderConfiguration.class);
		System.out.format("Extensions: %s\n", config.getExtensions());

		//System.out.format("%s\n", restTemplate().getForObject(String.format("https://localhost:%d/scim/v2/ServiceProviderConfig", port), String.class));

	}
	
	public static class BasicAuthenticator implements ClientRequestFilter {

		private final String user;
		private final String password;

		public BasicAuthenticator(String user, String password) {
			this.user = user;
			this.password = password;
		}

		public void filter(ClientRequestContext requestContext) throws IOException {
			MultivaluedMap<String, Object> headers = requestContext.getHeaders();
			final String basicAuthentication = getBasicAuthentication();
			headers.add("Authorization", basicAuthentication);

		}

		private String getBasicAuthentication() {
			String token = this.user + ":" + this.password;
			try {
				return "BASIC " + Base64.getEncoder().encodeToString(token.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Cannot encode with UTF-8", ex);
			}
		}
	}

	//	public RestTemplate restTemplate() {		
	//		
	//		try {
	//
	//			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	//			credentialsProvider.setCredentials(new AuthScope("localhost", port), new UsernamePasswordCredentials("db1", "okta".toCharArray()));
	//
	//			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
	//			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, acceptingTrustStrategy).build();
	//
	//			PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext)).build();
	//			CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connectionManager).build();
	//			HttpComponentsClientHttpRequestFactory customRequestFactory = new HttpComponentsClientHttpRequestFactory();
	//			customRequestFactory.setHttpClient(httpClient);
	//			return new RestTemplate(customRequestFactory);
	//		} catch (Throwable e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

}
