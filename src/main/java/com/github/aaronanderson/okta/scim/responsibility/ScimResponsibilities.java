package com.github.aaronanderson.okta.scim.responsibility;

import org.apache.directory.scim.spec.annotation.ScimResourceType;

import com.github.aaronanderson.okta.scim.ScimEntitlement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@ScimResourceType(id = ScimResponsibilities.RESOURCE_NAME, name = ScimResponsibilities.RESOURCE_NAME, schema = ScimEntitlement.SCHEMA_URI, description = "H2 Responsibilities", endpoint = "/Responsibilities")
@XmlRootElement(name = ScimResponsibilities.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
public class ScimResponsibilities extends ScimEntitlement {

	private static final long serialVersionUID = 1L;
	
	public static final String RESOURCE_NAME = "responsibilities";

	public ScimResponsibilities() {
		super(RESOURCE_NAME);

	}

}
