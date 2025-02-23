package com.github.aaronanderson.okta.scim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.resources.UserGroup;
import org.apache.directory.scim.spec.resources.UserGroup.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.github.aaronanderson.okta.h2.db.DataSourceContextHolder;

@Service
public class UserRepository implements Repository<ScimUser> {

	@Autowired
	private DataSourceContextHolder dataSourceContextHolder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private final SchemaRegistry schemaRegistry;

	public UserRepository(SchemaRegistry schemaRegistry) {
		this.schemaRegistry = schemaRegistry;
	}

	@Override
	public Class<ScimUser> getResourceClass() {
		return ScimUser.class;
	}

	@Override
	public ScimUser create(ScimUser resource) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO USERS (USERNAME, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, ACTIVE) VALUES ( ?, ?, ?, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, resource.getUserName());
			ps.setString(2, resource.getEmails().stream().filter(e -> e.getPrimary() && "primary".equals(e.getType())).findFirst().map(e -> e.getValue()).orElse(null));
			ps.setString(3, resource.getName().getGivenName());
			ps.setString(4, resource.getName().getFamilyName());
			ps.setString(5, resource.getPassword());
			ps.setBoolean(6, resource.getActive());
			return ps;
		}, keyHolder);

		resource.setId(keyHolder.getKey().toString());
		return resource;
	}

	@Override
	public ScimUser update(String id, String version, ScimUser resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		int cnt = jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement("UPDATE USERS SET (USERNAME, EMAIL, FIRST_NAME, LAST_NAME, ACTIVE) = ( ?, ?, ?, ?, ? ) WHERE ID = ?");
			ps.setString(1, resource.getUserName());
			ps.setString(2, resource.getEmails().stream().filter(e -> e.getPrimary() && "primary".equals(e.getType())).findFirst().map(e -> e.getValue()).orElse(null));
			ps.setString(3, resource.getName().getGivenName());
			ps.setString(4, resource.getName().getFamilyName());
			ps.setBoolean(5, resource.getActive());

			ps.setString(6, id);
			return ps;
		});
		if (cnt == 1) {
			return get(id);
		}
		throw new ResourceException(500, "User not found");
	}

	@Override
	public ScimUser patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		throw new ResourceException(500, "Patch not supported");
	}

	@Override
	public ScimUser get(String id) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		return jdbcTemplate.queryForObject("SELECT ID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME, ACTIVE FROM USERS WHERE ID = ?", (rs, ri) -> {
			return buildUser(rs);
		}, id);
	}

	@Override
	public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
		dataSourceContextHolder.setBranchContext();

		List<ScimUser> result = List.of();
		if (filter != null) {
			//TODO implement a org.apache.directory.scim.spec.filter.BaseFilterExpressionMapper to parse the filter expression and generate a SQL query. 
			//For now, manually parse and analyze the filter
			String[] query = filter.getExpression().toUnqualifiedFilter().split("\s");
			if ("userName".equals(query[0]) && "EQ".equals(query[1])) {
				result = jdbcTemplate.query("SELECT ID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME, ACTIVE FROM USERS WHERE USERNAME = ?", (rs, ri) -> {
					return buildUser(rs);
				}, query[2]);
			} else if ("lastModified".equals(query[0]) && "GT".equals(query[1])) {
				DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
				ZonedDateTime incrementalTime = ZonedDateTime.parse(query[2].replaceAll("\"", ""), f);
				Timestamp ts = new Timestamp(incrementalTime.toEpochSecond());
				Set<String> ids = new HashSet<>();
				ids.addAll(jdbcTemplate.query("SELECT ID FROM USERS WHERE LAST_MODIFIED > ?", (rs, ri) -> {
					return rs.getString(1);
				}, ts));
				ids.addAll(jdbcTemplate.query("SELECT USER_ID FROM GROUP_MEMBERS WHERE LAST_MODIFIED > ?", (rs, ri) -> {
					return rs.getString(1);
				}, ts));
				ids.addAll(jdbcTemplate.query("SELECT USER_ID FROM LICENSE_ASSIGNMENTS WHERE LAST_MODIFIED > ?", (rs, ri) -> {
					return rs.getString(1);
				}, ts));
				ids.addAll(jdbcTemplate.query("SELECT USER_ID FROM RESPONSIBILITY_ASSIGNMENTS WHERE LAST_MODIFIED > ?", (rs, ri) -> {
					return rs.getString(1);
				}, ts));

				ids.stream().forEach(i -> jdbcTemplate.queryForObject("SELECT ID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME, ACTIVE FROM USERS WHERE ID = ?", (rs2, ri2) -> {
					return buildUser(rs2);
				}, i));

			} else {
				throw new ResourceException(500, "Query not supported " + filter.getExpression().toUnqualifiedFilter());
			}
		} else {
			result = jdbcTemplate.query("SELECT ID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME, ACTIVE FROM USERS ORDER BY USERNAME", (rs, ri) -> {
				return buildUser(rs);
			});

		}

		System.out.format("find users %s\n", result.size());
		return new FilterResponse<>(result, pageRequest, result.size());
	}

	private ScimUser buildUser(ResultSet rs) throws SQLException {
		ScimUser scimUser = new ScimUser();
		scimUser.setId(rs.getString(1));
		scimUser.setUserName(rs.getString(2));
		Email workEmail = new Email();
		workEmail.setType("primary");
		workEmail.setPrimary(true);
		workEmail.setValue(rs.getString(3));
		scimUser.setEmails(List.of(workEmail));
		Name name = new Name();
		name.setGivenName(rs.getString(4));
		name.setFamilyName(rs.getString(5));
		scimUser.setName(name);
		scimUser.setActive(rs.getBoolean(6));
		scimUser.setEntitlements(List.of());

		List<UserGroup> groups = jdbcTemplate.query("SELECT gm.GROUP_ID, g.NAME FROM GROUP_MEMBERS gm, GROUPS g WHERE gm.USER_ID = ? AND g.ID = gm.GROUP_ID", (rs2, ri2) -> {
			UserGroup group = new UserGroup();
			group.setValue(rs.getString(1));
			group.setDisplay(rs.getString(2));
			group.setType(Type.DIRECT);
			return group;
		}, rs.getString(1));
		scimUser.setGroups(groups);
		return scimUser;
	}

	@Override
	public void delete(String id) throws ResourceException {
		int cnt = jdbcTemplate.update("DELETE FROM USERS WHERE ID = ?", id);
		if (cnt != 1) {
			throw new ResourceException(500, "User not found");
		}

	}

	@Override
	public List<Class<? extends ScimExtension>> getExtensionList() {
		//return List.of(H2UserExtension.class);
		return List.of();
	}

}
