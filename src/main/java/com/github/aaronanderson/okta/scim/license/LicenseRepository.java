package com.github.aaronanderson.okta.scim.license;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.directory.scim.core.repository.InvalidRepositoryException;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.github.aaronanderson.okta.h2.WebConfigurerAdapter;
import com.github.aaronanderson.okta.h2.db.DataSourceContextHolder;
import com.github.aaronanderson.okta.scim.responsibility.ScimResponsibilities;

@Service
public class LicenseRepository implements Repository<ScimLicense> {

	@Autowired
	private DataSourceContextHolder dataSourceContextHolder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private final SchemaRegistry schemaRegistry;

	public LicenseRepository(SchemaRegistry schemaRegistry) {
		this.schemaRegistry = schemaRegistry;
	}

	@Override
	public Class<ScimLicense> getResourceClass() {
		return ScimLicense.class;
	}

	@Override
	public ScimLicense create(ScimLicense resource) throws ResourceException {
		System.out.format("create license\n");
		return null;
	}

	@Override
	public ScimLicense update(String id, String version, ScimLicense resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		System.out.format("update license\n");
		return null;
	}

	@Override
	public ScimLicense patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		System.out.format("patch license\n");
		return null;
	}

	@Override
	public ScimLicense get(String id) throws ResourceException {
		System.out.format("get license\n");
		return null;
	}

	@Override
	public FilterResponse<ScimLicense> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		
		List<ScimLicense> result = new LinkedList<>();

		jdbcTemplate.query("SELECT ID, NAME, DESCRIPTION FROM LICENSES", (rs) -> {
			ScimLicense scimLicense = new ScimLicense();
			scimLicense.setId(String.valueOf(rs.getLong(1)));
			scimLicense.setDisplayName(rs.getString(2));
			scimLicense.setType("license");
			scimLicense.setDescription(rs.getString(3));
			result.add(scimLicense);
		});
		System.out.format("find licenses %s\n", result.size());
		return new FilterResponse<>(result, pageRequest, result.size());
	}

	@Override
	public void delete(String id) throws ResourceException {
		System.out.format("delete license\n");

	}

	@Override
	public List<Class<? extends ScimExtension>> getExtensionList() throws InvalidRepositoryException {
		return Collections.EMPTY_LIST;
	}

}
