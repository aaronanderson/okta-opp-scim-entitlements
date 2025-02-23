package com.github.aaronanderson.okta.scim.responsibility;

import org.apache.directory.scim.protocol.BaseResourceTypeResource;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Path;

//JAX-RS endpoint
@Path("Responsibilities")
@Tag(name="SCIM")
public interface ResponsibilitiesResource extends BaseResourceTypeResource<ScimResponsibilities> {
}
