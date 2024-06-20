package com.vladmihalcea.jooq.oracle.crud;

import com.vladmihalcea.jooq.oracle.util.AbstractJOOQOracleSQLIntegrationTest;
import org.junit.Test;

import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class SQLInjectionTest extends AbstractJOOQOracleSQLIntegrationTest {

    @Override
    protected String ddlScript() {
        return "clean_schema.sql";
    }

    @Test
    public void testLiteral() {
        doInJOOQ(sql -> {
            sql
            .deleteFrom(table("post"))
            .execute();

            assertEquals(1, sql
                .insertInto(table("post")).columns(field("id"), field("title"))
                .values(1L, "High-Performance Java Persistence")
                .execute());
        });

        doInJDBC(connection -> {
            DSLContext sql = DSL.using(
                    connection,
                    sqlDialect(),
                    new Settings().withStatementType( StatementType.STATIC_STATEMENT)
            );

            String sqlInjected = ((char)0xbf5c) + " or 1 >= ALL ( SELECT 1 FROM pg_locks, pg_sleep(10) ) --'";
            //String sqlInjected = ((char)0x815c) + " or 1 >= ALL ( SELECT 1 FROM pg_locks, pg_sleep(10) ) --'";

            sql
            .select(field("title"))
            .from(table("post"))
            .where(field("title").eq( sqlInjected ))
            .fetch();
        });
    }
}
