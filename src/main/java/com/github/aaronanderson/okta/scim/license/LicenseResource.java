package com.github.aaronanderson.okta.scim.license;

import org.apache.directory.scim.protocol.BaseResourceTypeResource;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Path;

//JAX-RS endpoint
@Path("Licenses")
@Tag(name="SCIM")
public interface LicenseResource extends BaseResourceTypeResource<ScimLicense> {
}
