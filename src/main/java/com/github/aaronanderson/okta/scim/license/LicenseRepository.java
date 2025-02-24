package com.github.aaronanderson.okta.scim.license;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

import com.github.aaronanderson.okta.h2.db.DataSourceContextHolder;

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

	public List<Entitlement> getUserLicense(String userId) {
		return jdbcTemplate.query("SELECT l.ID, l.NAME FROM LICENSES l, LICENSE_ASSIGNMENTS e WHERE e.USER_ID = ? AND l.ID = e.LICENSE_ID", (rs2, ri2) -> {
			Entitlement entitlement = new Entitlement();
			entitlement.setValue(rs2.getString(1));
			entitlement.setDisplay(rs2.getString(2));
			entitlement.setType("license");
			entitlement.setPrimary(true);
			return entitlement;
		}, userId);
	}

	public void updateUserLicense(String userId, List<Entitlement> entitlements) {
		Set<String> newEntitlementIds = entitlements.stream().filter(e -> "license".equals(e.getType())).map(e -> e.getValue()).collect(Collectors.toSet());
		List<String> currentEntitlementIds = jdbcTemplate.query("SELECT LICENSE_ID FROM LICENSE_ASSIGNMENTS WHERE USER_ID = ?", (rs2, ri2) -> {
			return rs2.getString(1);
		}, userId);

		Set<String> removeEntitlementIds = new HashSet<>(currentEntitlementIds);
		removeEntitlementIds.removeAll(newEntitlementIds);

		for (String id : removeEntitlementIds) {
			jdbcTemplate.update("DELETE FROM LICENSE_ASSIGNMENTS WHERE USER_ID = ? AND LICENSE_ID = ?", userId, id);
		}

		Set<String> addEntitlementIds = new HashSet<>(newEntitlementIds);
		addEntitlementIds.removeAll(currentEntitlementIds);

		for (String id : addEntitlementIds) {
			jdbcTemplate.update("INSERT INTO LICENSE_ASSIGNMENTS (USER_ID, LICENSE_ID) VALUES ( ?, ? )", userId, id);
		}

	}

}
