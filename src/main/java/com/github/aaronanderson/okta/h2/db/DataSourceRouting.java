package com.github.aaronanderson.okta.h2.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import com.github.aaronanderson.okta.h2.H2ConnectorConfig;
import com.github.aaronanderson.okta.h2.H2ConnectorConfig.Instance;

@Component
public class DataSourceRouting extends AbstractRoutingDataSource {

	private DataSourceContextHolder dataSourceContextHolder;

	public DataSourceRouting(DataSourceContextHolder dataSourceContextHolder, H2ConnectorConfig connectorConfig) {
		this.dataSourceContextHolder = dataSourceContextHolder;

		Map<Object, Object> dataSourceMap = new HashMap<>();
		for (Entry<String, Instance> instance : connectorConfig.getInstances().entrySet()) {
			dataSourceMap.put(instance.getKey(), buildDataSource(instance.getValue()));
		}

		this.setTargetDataSources(dataSourceMap);
		if (dataSourceMap.size() > 0) {
			this.setDefaultTargetDataSource(dataSourceMap.values().iterator().next());
		} 
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return dataSourceContextHolder.getBranchContext();
	}

	public DataSource buildDataSource(Instance instance) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(instance.getJdbc_url());
		dataSource.setUsername(instance.getJdbc_username());
		dataSource.setPassword(instance.getJdbc_password());
		dataSource.setSchema("PUBLIC");
		return dataSource;
	}

}
