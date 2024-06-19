package com.vladmihalcea.jooq.oracle.util;

import com.vladmihalcea.jooq.AbstractJOOQIntegrationTest;
import com.vladmihalcea.util.providers.DataSourceProvider;
import com.vladmihalcea.util.providers.OracleDataSourceProvider;
import org.jooq.SQLDialect;

/**
 * @author Vlad Mihalcea
 */
public abstract class AbstractJOOQOracleSQLIntegrationTest extends AbstractJOOQIntegrationTest {

    @Override
    protected String ddlFolder() {
        return "oracle";
    }

    @Override
    protected SQLDialect sqlDialect() {
        return SQLDialect.ORACLE18C;
    }

    protected DataSourceProvider dataSourceProvider() {
        return new OracleDataSourceProvider();
    }
}
