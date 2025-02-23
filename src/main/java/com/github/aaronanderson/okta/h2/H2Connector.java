package com.github.aaronanderson.okta.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.rest.ScimResourceHelper;
import org.apache.directory.scim.server.rest.ServiceProviderConfigResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.aaronanderson.okta.h2.db.DataSourceContextHolder;
import com.github.aaronanderson.okta.scim.OktaServiceProviderConfigResourceImpl;
import com.github.aaronanderson.okta.scim.ScimEntitlement;
import com.github.aaronanderson.okta.scim.license.LicenseResourceImpl;
import com.github.aaronanderson.okta.scim.responsibility.ResponsibilitiesResourceImpl;

import jakarta.ws.rs.core.Application;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = { "com.github.aaronanderson.okta.h2", "com.github.aaronanderson.okta.scim" })
public class H2Connector extends SpringBootServletInitializer {
	private final Logger log = LoggerFactory.getLogger(H2Connector.class);

	@Autowired
	private SchemaRegistry schemaRegistry;

	@Autowired
	private DataSourceContextHolder dataSourceContextHolder;
	
	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	private H2ConnectorConfig connectorConfig;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() throws SQLException {
		schemaRegistry.getSchema(ScimEntitlement.SCHEMA_URI).setName("Entitlement").setDescription("Okta Entitlement");

		//ensure all databases are initialized
		for (String db : connectorConfig.getInstances().keySet()) {
			dataSourceContextHolder.setBranchContext(db);
			try (Connection con = jdbcTemplate.getDataSource().getConnection()) {
				DatabaseMetaData meta = con.getMetaData();
				try (ResultSet resultSet = meta.getTables(null, "PUBLIC", "USERS", new String[] { "TABLE" });) {
					if (!resultSet.next()) {
						log.info("Initializing DB {}", db);
						DatabaseInitializationSettings initSettings = new DatabaseInitializationSettings();
						initSettings.setMode(DatabaseInitializationMode.ALWAYS);
						initSettings.setSchemaLocations(List.of("optional:classpath*:schema.sql"));
						initSettings.setDataLocations(List.of("optional:classpath*:data.sql"));
						DataSourceScriptDatabaseInitializer initializer = new DataSourceScriptDatabaseInitializer(jdbcTemplate.getDataSource(), initSettings);
						initializer.setResourceLoader(context);
						initializer.initializeDatabase(); 
					} else {
						log.info("Already initialized DB {}", db);
					}
				}
//				try (ResultSet resultSet = meta.getTables(null, null, null, new String[] { "TABLE" });) {
//					while (resultSet.next()) {
//						for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
//							//System.out.format("\t%s\n", resultSet.getMetaData().getColumnLabel(i));
//						}
//
//						System.out.format("%s - %s %s %s\n", db, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3));
//
//					}
//				}
			}

		}
	}

	@Bean
	Application jaxrsApplication() {
		return new H2JaxRsApplication();
	}

	static class H2JaxRsApplication extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = new HashSet<>();
			classes.addAll(ScimResourceHelper.scimpleFeatureAndResourceClasses());
			classes.add(ResponsibilitiesResourceImpl.class);
			classes.add(LicenseResourceImpl.class);
			classes.add(OktaServiceProviderConfigResourceImpl.class);
			classes.remove(ServiceProviderConfigResourceImpl.class);
			return classes;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(H2Connector.class, new String[0]);
	}

	public String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return authentication.getName();
		}
		return null;
	}

}
