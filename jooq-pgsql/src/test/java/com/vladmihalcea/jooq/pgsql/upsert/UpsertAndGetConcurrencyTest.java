package com.vladmihalcea.jooq.pgsql.upsert;

import com.vladmihalcea.jooq.pgsql.schema.crud.tables.records.PostDetailsRecord;
import com.vladmihalcea.jooq.pgsql.schema.crud.tables.records.PostRecord;
import com.vladmihalcea.jooq.pgsql.util.AbstractJOOQPostgreSQLIntegrationTest;
import com.vladmihalcea.util.exception.ExceptionUtil;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.vladmihalcea.jooq.pgsql.schema.crud.Sequences.HIBERNATE_SEQUENCE;
import static com.vladmihalcea.jooq.pgsql.schema.crud.Tables.POST;
import static com.vladmihalcea.jooq.pgsql.schema.crud.tables.PostDetails.POST_DETAILS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;
import static org.junit.Assert.assertTrue;

/**
 * @author Vlad Mihalcea
 */
public class UpsertAndGetConcurrencyTest extends AbstractJOOQPostgreSQLIntegrationTest {

    @Override
    protected String ddlScript() {
        return "clean_schema.sql";
    }

    private final CountDownLatch aliceLatch = new CountDownLatch(1);

    @Test
    public void testUpsert() {
        doInJOOQ(sql -> {
            sql.delete(POST_DETAILS).execute();
            sql.delete(POST).execute();

            PostRecord postRecord = sql
            .insertInto(POST).columns(POST.ID, POST.TITLE)
            .values(HIBERNATE_SEQUENCE.nextval(), val("High-Performance Java Persistence"))
            .returning(POST.ID)
            .fetchOne();

            final Long postId = postRecord.getId();

            sql
            .insertInto(POST_DETAILS)
            .columns(POST_DETAILS.ID, POST_DETAILS.CREATED_BY, POST_DETAILS.CREATED_ON)
            .values(postId, "Alice", LocalDateTime.now())
            .onDuplicateKeyIgnore()
            .execute();

            final AtomicBoolean preventedByLocking = new AtomicBoolean();

            executeAsync(() -> {
                try {
                    doInJOOQ(_sql -> {
                        setJdbcTimeout(_sql.configuration().connectionProvider().acquire());

                        _sql
                        .insertInto(POST_DETAILS)
                        .columns(POST_DETAILS.ID, POST_DETAILS.CREATED_BY, POST_DETAILS.CREATED_ON)
                        .values(postId, "Bob", LocalDateTime.now())
                        .onDuplicateKeyIgnore()
                        .execute();
                    });
                } catch (Exception e) {
                    if( ExceptionUtil.isLockTimeout( e )) {
                        preventedByLocking.set( true );
                    }
                }

                aliceLatch.countDown();
            });

            awaitOnLatch(aliceLatch);

            PostDetailsRecord postDetailsRecord = sql.selectFrom(POST_DETAILS)
            .where(field(POST_DETAILS.ID).eq(postId))
            .fetchOne();

            assertTrue(preventedByLocking.get());
        });
    }
}
