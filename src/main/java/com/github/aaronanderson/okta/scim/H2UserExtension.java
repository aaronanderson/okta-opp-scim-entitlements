package com.github.aaronanderson.okta.scim;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Mutability;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "UserExtension", namespace = "https://aaronanderson.github.com/okta/scim")
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = H2UserExtension.SCHEMA_URN, description = "H2 User", name = "H2User", required = true)
public class H2UserExtension implements ScimExtension {

	public static final String SCHEMA_URN = "urn:github-aaronanderson:scim:schemas:resource:extension:1.0:H2User";

	@ScimAttribute(returned = Schema.Attribute.Returned.DEFAULT, required = true, mutability = Mutability.READ_WRITE)
	@XmlElement
	private String databaseSchema;

	@Override
	public String getUrn() {
		return SCHEMA_URN;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

}
