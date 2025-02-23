package com.github.aaronanderson.okta.h2;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public final class SCIMTest {

	@LocalServerPort
	private int port;

	@Test
	void serviceProviderConfig() {
		System.out.format("%s\n", restTemplate().getForObject(String.format("https://localhost:%d/scim/v2/ServiceProviderConfig", port), String.class));
	}

	public RestTemplate restTemplate() {		
		
		try {

			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(new AuthScope("localhost", port), new UsernamePasswordCredentials("db1", "okta".toCharArray()));

			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, acceptingTrustStrategy).build();

			PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext)).build();
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connectionManager).build();
			HttpComponentsClientHttpRequestFactory customRequestFactory = new HttpComponentsClientHttpRequestFactory();
			customRequestFactory.setHttpClient(httpClient);
			return new RestTemplate(customRequestFactory);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
