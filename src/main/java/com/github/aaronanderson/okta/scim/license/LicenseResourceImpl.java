package com.github.aaronanderson.okta.scim.license;

import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.rest.BaseResourceTypeResourceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class LicenseResourceImpl extends BaseResourceTypeResourceImpl<ScimLicense> implements LicenseResource {

  @Inject
  public LicenseResourceImpl(SchemaRegistry schemaRegistry, RepositoryRegistry repositoryRegistry) {
    super(schemaRegistry, repositoryRegistry, ScimLicense.class);
  }

  public LicenseResourceImpl() {
    // CDI
    this(null, null);
  }
}


