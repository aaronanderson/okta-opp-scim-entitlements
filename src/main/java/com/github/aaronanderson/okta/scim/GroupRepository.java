package com.github.aaronanderson.okta.scim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.github.aaronanderson.okta.h2.db.DataSourceContextHolder;

@Service
public class GroupRepository implements Repository<ScimGroup> {

	private final SchemaRegistry schemaRegistry;

	@Autowired
	private DataSourceContextHolder dataSourceContextHolder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	public GroupRepository(SchemaRegistry schemaRegistry) {
		this.schemaRegistry = schemaRegistry;
	}

	@Override
	public Class<ScimGroup> getResourceClass() {
		return ScimGroup.class;
	}

	@Override
	public ScimGroup create(ScimGroup resource) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO GROUPS (NAME) VALUES ( ? )", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, resource.getDisplayName());
			return ps;
		}, keyHolder);

		resource.setId(keyHolder.getKey().toString());
		return resource;
	}

	@Override
	public ScimGroup update(String id, String version, ScimGroup resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		int cnt = jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement("UPDATE GROUPS SET (NAME) = ( ? ) WHERE ID = ?");
			ps.setString(1, resource.getDisplayName());
			ps.setString(2, id);
			return ps;
		});
		if (cnt == 1) {
			return get(id);
		}
		throw new ResourceException(500, "Group not found");
	}

	@Override
	public ScimGroup patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
		throw new ResourceException(500, "Patch not supported");
	}

	@Override
	public ScimGroup get(String id) throws ResourceException {
		dataSourceContextHolder.setBranchContext();
		return jdbcTemplate.queryForObject("SELECT ID, NAME FROM GROUPS WHERE ID = ?", (rs, ri) -> {
			return buildGroup(rs);
		}, id);
	}

	@Override
	public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
		dataSourceContextHolder.setBranchContext();

		List<ScimGroup> result = jdbcTemplate.query("SELECT ID, NAME FROM GROUPS", (rs, ri) -> {
			return buildGroup(rs);
		});

		System.out.format("find groups %s\n", result.size());
		return new FilterResponse<>(result, pageRequest, result.size());
	}

	private ScimGroup buildGroup(ResultSet rs) throws SQLException {
		ScimGroup scimGroup = new ScimGroup();
		scimGroup.setId(rs.getString(1));
		scimGroup.setDisplayName(rs.getString(2));

		List<GroupMembership> members = jdbcTemplate.query("SELECT gm.USER_ID, u.USERNAME FROM GROUP_MEMBERS gm, USERS u WHERE gm.GROUP_ID = ? AND u.ID = gm.GROUP_ID", (rs2, ri2) -> {
			GroupMembership member = new GroupMembership();
			member.setValue(rs2.getString(1));
			member.setType(GroupMembership.Type.USER);
			member.setDisplay(rs2.getString(2));
			return member;
		}, rs.getString(1));

		scimGroup.setMembers(members);
		return scimGroup;
	}

	public List<UserGroup> getUserGroups(String userId) {
		return jdbcTemplate.query("SELECT gm.GROUP_ID, g.NAME FROM GROUP_MEMBERS gm, GROUPS g WHERE gm.USER_ID = ? AND g.ID = gm.GROUP_ID", (rs2, ri2) -> {
			UserGroup group = new UserGroup();
			group.setValue(rs2.getString(1));
			group.setDisplay(rs2.getString(2));
			group.setType(UserGroup.Type.DIRECT);
			return group;
		}, userId);
	}

	@Override
	public void delete(String id) throws ResourceException {
		int cnt = jdbcTemplate.update("DELETE FROM GROUPS WHERE ID = ?", id);
		if (cnt != 1) {
			throw new ResourceException(500, "Group not found");
		}

	}

}
