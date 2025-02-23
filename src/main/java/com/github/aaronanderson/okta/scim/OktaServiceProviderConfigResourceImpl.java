package com.github.aaronanderson.okta.scim;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.scim.protocol.ServiceProviderConfigResource;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.rest.EtagGenerator;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

//https://help.okta.com/en-us/content/topics/provisioning/opp/opp-provision-scim-messages.htm
@ApplicationScoped
public class OktaServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {

	@Override
	public Response getServiceProviderConfiguration(UriInfo uriInfo) {
		ServiceProviderConfiguration serviceProviderConfiguration = new ServiceProviderConfiguration();

		BulkConfiguration bulk = new BulkConfiguration();
		bulk.setSupported(false);
		//bulk.setMaxOperations(100);
		//bulk.setMaxPayloadSize(1024);

		FilterConfiguration filter = new FilterConfiguration();
		filter.setSupported(true);
		filter.setMaxResults(100);

		Meta meta = new Meta();
		String location = uriInfo.getAbsolutePath().toString();
		String resourceType = "ServiceProviderConfig";
		LocalDateTime now = LocalDateTime.now();
		meta.setCreated(now);
		meta.setLastModified(now);
		meta.setLocation(location);
		meta.setResourceType(resourceType);

		OktaProviderScimExtension oktaProviderScimExtension = new OktaProviderScimExtension();
		oktaProviderScimExtension.getUserManagementCapabilities().add("IMPORT_NEW_USERS");
		oktaProviderScimExtension.getUserManagementCapabilities().add("OPP_SCIM_INCREMENTAL_IMPORTS");
		oktaProviderScimExtension.getUserManagementCapabilities().add("IMPORT_PROFILE_UPDATES");
		oktaProviderScimExtension.getUserManagementCapabilities().add("PUSH_NEW_USERS");
		oktaProviderScimExtension.getUserManagementCapabilities().add("PUSH_PENDING_USERS");
		oktaProviderScimExtension.getUserManagementCapabilities().add("PUSH_PASSWORD_UPDATES");
		oktaProviderScimExtension.getUserManagementCapabilities().add("PUSH_PROFILE_UPDATES");
		oktaProviderScimExtension.getUserManagementCapabilities().add("PUSH_USER_DEACTIVATION");
		oktaProviderScimExtension.getUserManagementCapabilities().add("REACTIVATE_USERS");
		oktaProviderScimExtension.getUserManagementCapabilities().add("GROUP_PUSH");
		oktaProviderScimExtension.getUserManagementCapabilities().add("IMPORT_USER_SCHEMA");
		//oktaProviderScimExtension.getUserManagementCapabilities().add("PROFILE_MASTERING");
		//oktaProviderScimExtension.getUserManagementCapabilities().add("SCIM_PROVISIONING");		
		

		serviceProviderConfiguration.setAuthenticationSchemes(Collections.EMPTY_LIST);
		serviceProviderConfiguration.setBulk(bulk);
		serviceProviderConfiguration.setChangePassword(createSupportedConfiguration(true));
		serviceProviderConfiguration.setDocumentationUrl("https://support.okta.com/scim-fake-page.html");
		serviceProviderConfiguration.setEtag(createSupportedConfiguration(false));
		serviceProviderConfiguration.setFilter(filter);
		serviceProviderConfiguration.setMeta(meta);
		serviceProviderConfiguration.setPatch(createSupportedConfiguration(false));
		serviceProviderConfiguration.setSort(createSupportedConfiguration(false));
		serviceProviderConfiguration.addExtension(oktaProviderScimExtension);

		return Response.ok(serviceProviderConfiguration).build();

	}

	private SupportedConfiguration createSupportedConfiguration(boolean supported) {
		SupportedConfiguration supportedConfiguration = new SupportedConfiguration();
		supportedConfiguration.setSupported(supported);
		return supportedConfiguration;
	}

	@XmlRootElement(name = "OktaProviderScimExtension", namespace = "https://okta.com/schemas/okta-scim")
	@XmlAccessorType(XmlAccessType.NONE)
	@Data
	@ScimExtensionType(id = OktaProviderScimExtension.SCHEMA_URN, description = "Okta Provider Config", name = "OktaProvider", required = true)
	public static class OktaProviderScimExtension implements ScimExtension {

		public static final String SCHEMA_URN = "urn:okta:schemas:scim:providerconfig:1.0";

		@ScimAttribute(returned = Schema.Attribute.Returned.DEFAULT, required = true)
		@XmlElement
		private List<String> userManagementCapabilities = new LinkedList<>();

		@Override
		public String getUrn() {
			return SCHEMA_URN;
		}

		public List<String> getUserManagementCapabilities() {
			return userManagementCapabilities;
		}

	}
}
