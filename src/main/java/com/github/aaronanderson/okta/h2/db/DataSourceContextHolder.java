package com.github.aaronanderson.okta.h2.db;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.aaronanderson.okta.h2.WebConfigurerAdapter;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DataSourceContextHolder {
	private static ThreadLocal<String> threadLocal;

	public DataSourceContextHolder() {
		threadLocal = new ThreadLocal<>();
	}
	
	public void setBranchContext() {
		String applicationName = WebConfigurerAdapter.getApplicationName();
		setBranchContext(applicationName);
	}

	public void setBranchContext(String dataSourceEnum) {
		threadLocal.set(dataSourceEnum);
	}

	public String getBranchContext() {
		return threadLocal.get();
	}

	public static void clearBranchContext() {
		threadLocal.remove();
	}
}