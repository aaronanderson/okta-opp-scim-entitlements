package com.github.aaronanderson.okta.scim.license;

import org.apache.directory.scim.spec.annotation.ScimResourceType;

import com.github.aaronanderson.okta.scim.ScimEntitlement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@ScimResourceType(id = ScimLicense.RESOURCE_NAME, name = ScimLicense.RESOURCE_NAME, schema = ScimEntitlement.SCHEMA_URI, description = "H2 License", endpoint = "/Licenses")
@XmlRootElement(name = ScimLicense.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class ScimLicense extends ScimEntitlement {

	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_NAME = "license";

	public ScimLicense() {
		super(RESOURCE_NAME);

	}

}
