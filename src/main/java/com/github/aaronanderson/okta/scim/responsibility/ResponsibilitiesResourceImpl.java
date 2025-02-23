package com.github.aaronanderson.okta.scim.responsibility;

import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.rest.BaseResourceTypeResourceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ResponsibilitiesResourceImpl extends BaseResourceTypeResourceImpl<ScimResponsibilities> implements ResponsibilitiesResource {

  @Inject
  public ResponsibilitiesResourceImpl(SchemaRegistry schemaRegistry, RepositoryRegistry repositoryRegistry) {
    super(schemaRegistry, repositoryRegistry, ScimResponsibilities.class);
  }

  public ResponsibilitiesResourceImpl() {
    // CDI
    this(null, null);
  }
}


