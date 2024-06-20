package com.vladmihalcea.jooq.pgsql.crud;

import com.vladmihalcea.jooq.pgsql.util.AbstractJOOQPostgreSQLIntegrationTest;
import org.junit.Test;

import static com.vladmihalcea.jooq.pgsql.schema.crud.Tables.POST;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class CrudTest extends AbstractJOOQPostgreSQLIntegrationTest {

    @Override
    protected String ddlScript() {
        return "clean_schema.sql";
    }

    @Test
    public void testCrud() {
        doInJOOQ(sql -> {
            sql
            .deleteFrom(table("post"))
            .execute();

            assertEquals(1, sql
            .insertInto(table("post")).columns(field("id"), field("title"))
            .values(1L, "High-Performance Java Persistence")
            .execute());

            assertEquals("High-Performance Java Persistence", sql
            .select(field("title"))
            .from(table("post"))
            .where(field("id").eq(1))
            .fetch().getValue(0, "title"));

            sql
            .update(table("post"))
            .set(field("title"), "High-Performance Java Persistence Book")
            .where(field("id").eq(1))
            .execute();

            assertEquals("High-Performance Java Persistence Book", sql
                    .select(field("title"))
                    .from(table("post"))
                    .where(field("id").eq(1))
                    .fetch().getValue(0, "title"));
        });
    }

    @Test
    public void testCrudJavaSchema() {
        doInJOOQ(sql -> {
            sql
            .deleteFrom(POST)
            .execute();

            assertEquals(1, sql
                .insertInto(POST).columns(POST.ID, POST.TITLE)
                .values(1L, "High-Performance Java Persistence")
                .execute()
            );

            assertEquals("High-Performance Java Persistence", sql
                .select(POST.TITLE)
                .from(POST)
                .where(POST.ID.eq(1L))
                .fetch().getValue(0, POST.TITLE)
            );

            sql
            .update(POST)
            .set(POST.TITLE, "High-Performance Java Persistence Book")
            .where(POST.ID.eq(1L))
            .execute();

            assertEquals("High-Performance Java Persistence Book", sql
                .select(POST.TITLE)
                .from(POST)
                .where(POST.ID.eq(1L))
                .fetch().getValue(0, POST.TITLE)
            );
        });
    }
}
