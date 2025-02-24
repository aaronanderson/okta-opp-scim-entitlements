package com.github.aaronanderson.okta.scim.responsibility;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.apache.directory.scim.spec.resources.Entitlement;
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

	public List<Entitlement> getUserResponsibilities(String userId) {
		return jdbcTemplate.query("SELECT r.ID, r.NAME FROM RESPONSIBILITIES r, RESPONSIBILITY_ASSIGNMENTS e WHERE e.USER_ID = ? AND r.ID = e.RESPONSIBILITY_ID", (rs2, ri2) -> {
			Entitlement entitlement = new Entitlement();
			entitlement.setValue(rs2.getString(1));
			entitlement.setDisplay(rs2.getString(2));
			entitlement.setType("responsibilities");
			entitlement.setPrimary(true);
			return entitlement;
		}, userId);
	}
	
	public void updateUserResponsibilities(String userId, List<Entitlement> entitlements) {
		Set<String> newEntitlementIds = entitlements.stream().filter(e -> "responsibilities".equals(e.getType())).map(e -> e.getValue()).collect(Collectors.toSet());
		List<String> currentEntitlementIds = jdbcTemplate.query("SELECT RESPONSIBILITY_ID FROM RESPONSIBILITY_ASSIGNMENTS WHERE USER_ID = ?", (rs2, ri2) -> {
			return rs2.getString(1);
		}, userId);

		Set<String> removeEntitlementIds = new HashSet<>(currentEntitlementIds);
		removeEntitlementIds.removeAll(newEntitlementIds);

		for (String id : removeEntitlementIds) {
			jdbcTemplate.update("DELETE FROM RESPONSIBILITY_ASSIGNMENTS WHERE USER_ID = ? AND RESPONSIBILITY_ID = ?", userId, id);
		}

		Set<String> addEntitlementIds = new HashSet<>(newEntitlementIds);
		addEntitlementIds.removeAll(currentEntitlementIds);

		for (String id : addEntitlementIds) {
			jdbcTemplate.update("INSERT INTO RESPONSIBILITY_ASSIGNMENTS (USER_ID, RESPONSIBILITY_ID) VALUES ( ?, ? )", userId, id);
		}
	}

}
