package com.vladmihalcea.jooq.pgsql.batching;

import com.vladmihalcea.jooq.pgsql.util.AbstractJOOQPostgreSQLIntegrationTest;
import com.vladmihalcea.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.jooq.DSLContext;
import org.jooq.InsertOnDuplicateSetMoreStep;
import org.jooq.impl.DSL;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.vladmihalcea.jooq.pgsql.schema.crud.tables.PostDetails.POST_DETAILS;
import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class HibernateFlushTest extends AbstractJOOQPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Post.class,
            PostComment.class,
        };
    }

    @Override
    protected String ddlScript() {
        return "clean_schema.sql";
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Post post = new Post(1L);
            post.title = "Postit";

            PostComment comment1 = new PostComment();
            comment1.id = 1L;
            comment1.review = "Good";

            PostComment comment2 = new PostComment();
            comment2.id = 2L;
            comment2.review = "Excellent";

            post.addComment(comment1);
            post.addComment(comment2);
            entityManager.persist(post);

            DSLContext sql = entityManager.unwrap(Session.class).doReturningWork(
                connection -> DSL.using(connection, sqlDialect())
            );

            /*entityManager.flush();
            upsertPostDetails(sql, 1L, "Alice", LocalDateTime.now());*/

            InsertOnDuplicateSetMoreStep jooqQuery = sql
                .insertInto(POST_DETAILS)
                .columns(POST_DETAILS.ID, POST_DETAILS.CREATED_BY, POST_DETAILS.CREATED_ON)
                .values(1L, "Alice", LocalDateTime.now())
                .onDuplicateKeyUpdate()
                .set(POST_DETAILS.UPDATED_BY, "Alice")
                .set(POST_DETAILS.UPDATED_ON, LocalDateTime.now());

            List<Object> bindValues = jooqQuery.getBindValues();

            int paramIndex = 1;

            int rowCount = entityManager.createNativeQuery(
                jooqQuery.getSQL())
            .setParameter(paramIndex++, bindValues.get(paramIndex - 2))
            .setParameter(paramIndex++, bindValues.get(paramIndex - 2))
            .setParameter(paramIndex++, bindValues.get(paramIndex - 2))
            .setParameter(paramIndex++, bindValues.get(paramIndex - 2))
            .setParameter(paramIndex++, bindValues.get(paramIndex - 2))
            .executeUpdate();
            assertEquals(1, rowCount);
        });
    }

    private void upsertPostDetails(DSLContext sql, Long id, String owner, LocalDateTime timestamp) {
        sql
            .insertInto(POST_DETAILS)
            .columns(POST_DETAILS.ID, POST_DETAILS.CREATED_BY, POST_DETAILS.CREATED_ON)
            .values(id, owner, timestamp)
            .onDuplicateKeyUpdate()
            .set(POST_DETAILS.UPDATED_BY, owner)
            .set(POST_DETAILS.UPDATED_ON, timestamp)
            .execute();
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        private Long id;

        private String title;

        public Post() {}

        public Post(Long id) {
            this.id = id;
        }

        public Post(String title) {
            this.title = title;
        }

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post",
                orphanRemoval = true)
        private List<PostComment> comments = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<PostComment> getComments() {
            return comments;
        }

        public void addComment(PostComment comment) {
            comments.add(comment);
            comment.setPost(this);
        }
    }

    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment {

        @Id
        private Long id;

        @ManyToOne
        private Post post;

        private String review;

        public PostComment() {}

        public PostComment(String review) {
            this.review = review;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }
}
