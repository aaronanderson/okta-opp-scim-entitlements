package com.github.aaronanderson.okta.scim;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@ScimResourceType(id = ScimEntitlement.RESOURCE_NAME, name = ScimEntitlement.RESOURCE_NAME, schema = ScimEntitlement.SCHEMA_URI, description = "Okta Entitlement", endpoint = "")
public abstract class ScimEntitlement extends ScimResource implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String SCHEMA_URI = "urn:okta:scim:schemas:core:1.0:Entitlement";
	public static final String RESOURCE_NAME = "Entitlement";

	@XmlElement
	@ScimAttribute(description = "The name that appears in the Admin Console for the responsibility", required = true, name = "displayName")
	String displayName;

	@XmlElement
	@ScimAttribute(description = "Corresponds with the ResourceType name field for the responsibility", required = true, name = "type")
	String type;

	@XmlElement
	@ScimAttribute(description = "A human-readable description of the responsibility. This appears in the Governance tab.", name = "description")
	String description;
	
	public ScimEntitlement(String resourceName) {
		super(SCHEMA_URI, resourceName);
		
	}

	@Override
	public ScimEntitlement setSchemas(Set<String> schemas) {
		return (ScimEntitlement) super.setSchemas(schemas);
	}

	@Override
	public ScimEntitlement setMeta(@NotNull Meta meta) {
		return (ScimEntitlement) super.setMeta(meta);
	}

	@Override
	public ScimEntitlement addSchema(String urn) {
		return (ScimEntitlement) super.addSchema(urn);
	}

	@Override
	public ScimEntitlement addExtension(ScimExtension extension) {
		return (ScimEntitlement) super.addExtension(extension);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(description, displayName, getId(), type);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScimEntitlement other = (ScimEntitlement) obj;
		return Objects.equals(description, other.description) && Objects.equals(displayName, other.displayName) && Objects.equals(getId(), other.getId()) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "ScimEntitlement [id=" + getId() + ", displayName=" + displayName + ", type=" + type + ", description=" + description + "]";
	}

}
