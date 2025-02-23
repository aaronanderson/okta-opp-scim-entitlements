package com.github.aaronanderson.okta.scim.responsibility;

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

@Service
public class ResponsibilitiesRepository implements Repository<ScimResponsibilities> {

	@Autowired
	private DataSourceContextHolder dataSourceContextHolder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private final SchemaRegistry schemaRegistry;

	public ResponsibilitiesRepository(SchemaRegistry schemaRegistry) {
		this.schemaRegistry = schemaRegistry;

	}

	@Override
	public Class<ScimResponsibilities> getResourceClass() {
		return ScimResponsibilities.class;
	}

	@Override
	public ScimResponsibilities create(ScimResponsibilities resource) throws ResourceException {
		System.out.format("create responsibility\n");
		return null;
	}

	@Override
	public ScimResponsibilities update(String id, String version, ScimResponsibilities resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		System.out.format("update responsibility\n");
		return null;
	}

	@Override
	public ScimResponsibilities patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		System.out.format("patch responsibility\n");
		return null;
	}

	@Override
	public ScimResponsibilities get(String id) throws ResourceException {
		System.out.format("get responsibility\n");
		return null;
	}

	@Override
	public FilterResponse<ScimResponsibilities> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		List<ScimResponsibilities> result = new LinkedList<>();

		jdbcTemplate.query("SELECT ID, NAME, DESCRIPTION FROM RESPONSIBILITIES", (rs) -> {
			ScimResponsibilities scimResponsibility = new ScimResponsibilities();
			scimResponsibility.setId(String.valueOf(rs.getLong(1)));
			scimResponsibility.setDisplayName(rs.getString(2));
			scimResponsibility.setType("responsibilities");
			scimResponsibility.setDescription(rs.getString(3));
			result.add(scimResponsibility);
		});
		System.out.format("find responsibilities %s\n", result.size());
		return new FilterResponse<>(result, pageRequest, result.size());
	}

	@Override
	public void delete(String id) throws ResourceException {
		System.out.format("delete responsibility\n");

	}

	@Override
	public List<Class<? extends ScimExtension>> getExtensionList() throws InvalidRepositoryException {
		return Collections.EMPTY_LIST;
	}

}
