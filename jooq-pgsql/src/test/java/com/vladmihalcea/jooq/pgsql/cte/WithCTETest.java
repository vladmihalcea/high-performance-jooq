package com.vladmihalcea.jooq.pgsql.cte;

import com.vladmihalcea.jooq.pgsql.schema.crud.Tables;
import com.vladmihalcea.jooq.pgsql.util.AbstractJOOQPostgreSQLIntegrationTest;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.junit.Test;

import java.util.List;

import static com.vladmihalcea.jooq.pgsql.schema.crud.tables.Post.POST;
import static com.vladmihalcea.jooq.pgsql.schema.crud.tables.PostComment.POST_COMMENT;
import static org.jooq.impl.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vlad Mihalcea
 */
public class WithCTETest extends AbstractJOOQPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            PostComment.class,
        };
    }

    @Override
    protected String ddlScript() {
        return "clean_schema.sql";
    }

    /**
     * post
     * ----
     *
     * | id | title    |
     * |----|----------|
     * | 1  | SQL:2016 |
     * | 2  | SQL:2011 |
     * | 3  | SQL:2008 |
     * | 4  | JPA 3.0  |
     * | 5  | JPA 2.2  |
     * | 6  | JPA 2.1  |
     * | 7  | JPA 2.0  |
     * | 8  | JPA 1.0  |
     *
     * post_comment
     * -------------
     *
     * | id | review                 | post_id |
     * |----|------------------------|---------|
     * | 1  | SQL:2016 is great!     | 1       |
     * | 2  | SQL:2016 is excellent! | 1       |
     * | 3  | SQL:2016 is awesome!   | 1       |
     * | 4  | SQL:2011 is great!     | 2       |
     * | 5  | SQL:2011 is excellent! | 2       |
     * | 6  | SQL:2008 is great!     | 3       |
     */
    @Override
    public void afterInit() {
        doInJPA(entityManager -> {
            short[] latestSQLStandards = {
                2016,
                2011,
                2008
            };

            String[] comments = {
                "great",
                "excellent",
                "awesome"
            };

            long post_id = 1;
            long comment_id = 1;

            for (int i = 0; i < latestSQLStandards.length; i++) {
                short sqlStandard = latestSQLStandards[i];
                Post post = new Post()
                    .setId(post_id++)
                    .setTitle(String.format("SQL:%d", sqlStandard));

                entityManager.persist(post);

                for (int j = 0; j < comments.length - i; j++) {
                    entityManager.persist(
                        new PostComment()
                            .setId(comment_id++)
                            .setReview(String.format("SQL:%d is %s!", sqlStandard, comments[j]))
                            .setPost(post)
                    );
                }
            }

            entityManager.persist(
                new Post()
                    .setId(post_id++)
                    .setTitle("JPA 3.0")
            );
            entityManager.persist(
                new Post()
                    .setId(post_id++)
                    .setTitle("JPA 2.2")
            );
            entityManager.persist(
                new Post()
                    .setId(post_id++)
                    .setTitle("JPA 2.1")
            );
            entityManager.persist(
                new Post()
                    .setId(post_id++)
                    .setTitle("JPA 2.0")
            );
            entityManager.persist(
                new Post()
                    .setId(post_id++)
                    .setTitle("JPA 1.0")
            );
        });
    }

    @Test
    public void testLeftJoin() {
        List<Tuple> tuples = doInJPA(entityManager -> {
            return entityManager.createNativeQuery("""
                SELECT
                  p.id AS post_id,
                  p.title AS post_title,
                  pc.id AS comment_id,
                  pc.review AS comment_review,
                  COUNT(post_id) OVER(PARTITION BY post_id) AS comment_count
                FROM post p
                LEFT JOIN post_comment pc ON p.id = pc.post_id
                WHERE p.title LIKE :title
                ORDER BY p.id, pc.id
                """, Tuple.class)
            .setParameter("title", "SQL%")
            .getResultList();
        });

        assertEquals(6, tuples.size());

        assertEquals(1L, ((Number) tuples.get(0).get("post_id")).longValue());
        assertEquals("SQL:2016", tuples.get(0).get("post_title"));
        assertEquals(1L, ((Number) tuples.get(0).get("comment_id")).longValue());
        assertEquals("SQL:2016 is great!", tuples.get(0).get("comment_review"));
        assertEquals(3, ((Number) tuples.get(0).get("comment_count")).intValue());

        assertEquals(2L, ((Number) tuples.get(3).get("post_id")).longValue());
        assertEquals("SQL:2011", tuples.get(3).get("post_title"));
        assertEquals(4L, ((Number) tuples.get(3).get("comment_id")).longValue());
        assertEquals("SQL:2011 is great!", tuples.get(3).get("comment_review"));
        assertEquals(2, ((Number) tuples.get(3).get("comment_count")).intValue());

        assertEquals(3L, ((Number) tuples.get(5).get("post_id")).longValue());
        assertEquals("SQL:2008", tuples.get(5).get("post_title"));
        assertEquals(6L, ((Number) tuples.get(5).get("comment_id")).longValue());
        assertEquals("SQL:2008 is great!", tuples.get(5).get("comment_review"));
        assertEquals(1, ((Number) tuples.get(5).get("comment_count")).intValue());

        Result<Record5<Long, String, Long, String, Integer>> posts = doInJOOQ(sql -> {
            return sql
            .select(
                POST.ID.as("post_id"),
                POST.TITLE.as("post_title"),
                POST_COMMENT.ID.as("comment_id"),
                POST_COMMENT.REVIEW.as("comment_review"),
                count(POST_COMMENT.POST_ID).over(partitionBy(POST_COMMENT.POST_ID)).as("comment_count")
            )
            .from(POST)
            .leftJoin(POST_COMMENT).on(POST.ID.eq(POST_COMMENT.POST_ID))
            .where(POST.TITLE.like("SQL%"))
            .orderBy(POST.ID, POST_COMMENT.ID)
            .fetch();
        });

        assertEquals(6, posts.size());
        assertEquals(1L, posts.get(0).get("post_id"));
        assertEquals("SQL:2016", posts.get(0).get("post_title"));
        assertEquals(1L, posts.get(0).get("comment_id"));
        assertEquals("SQL:2016 is great!", posts.get(0).get("comment_review"));
        assertEquals(3, posts.get(0).get("comment_count"));

        assertEquals(2L, posts.get(3).get("post_id"));
        assertEquals("SQL:2011", posts.get(3).get("post_title"));
        assertEquals(4L, posts.get(3).get("comment_id"));
        assertEquals("SQL:2011 is great!", posts.get(3).get("comment_review"));
        assertEquals(2, posts.get(3).get("comment_count"));

        assertEquals(2L, posts.get(4).get("post_id"));
        assertEquals("SQL:2011", posts.get(4).get("post_title"));
        assertEquals(5L, posts.get(4).get("comment_id"));
        assertEquals("SQL:2011 is excellent!", posts.get(4).get("comment_review"));

        assertEquals(3L, posts.get(5).get("post_id"));
        assertEquals("SQL:2008", tuples.get(5).get("post_title"));
        assertEquals(6L, posts.get(5).get("comment_id"));
        assertEquals("SQL:2008 is great!", tuples.get(5).get("comment_review"));
        assertEquals(1, posts.get(5).get("comment_count"));
    }

    @Test
    public void testWithCTE() {
        List<Tuple> tuples = doInJPA(entityManager -> {
            return entityManager.createNativeQuery("""
                WITH
                p_pc AS (
                  SELECT
                    p.id AS post_id,
                    p.title AS post_title,
                    pc.id AS comment_id,
                    pc.review AS comment_review,
                    COUNT(post_id) OVER(PARTITION BY post_id) AS comment_count
                  FROM post p
                  LEFT JOIN post_comment pc ON p.id = pc.post_id
                  WHERE p.title LIKE :title
                ),
                p_pc_r AS (
                  SELECT
                    post_id,
                    post_title,
                    comment_id,
                    comment_review,
                    DENSE_RANK() OVER (ORDER BY p_pc.comment_count DESC) AS ranking
                  FROM p_pc
                )
                SELECT *
                FROM p_pc_r
                WHERE p_pc_r.ranking <= :ranking
                ORDER BY post_id, comment_id
                """, Tuple.class)
            .setParameter("title", "SQL%")
            .setParameter("ranking", 2)
            .getResultList();
        });

        assertEquals(5, tuples.size());

        assertEquals(1L, ((Number) tuples.get(0).get("post_id")).longValue());
        assertEquals("SQL:2016", tuples.get(0).get("post_title"));
        assertEquals(1L, ((Number) tuples.get(0).get("comment_id")).longValue());
        assertEquals("SQL:2016 is great!", tuples.get(0).get("comment_review"));

        assertEquals(2L, ((Number) tuples.get(3).get("post_id")).longValue());
        assertEquals("SQL:2011", tuples.get(3).get("post_title"));
        assertEquals(4L, ((Number) tuples.get(3).get("comment_id")).longValue());
        assertEquals("SQL:2011 is great!", tuples.get(3).get("comment_review"));

        assertEquals(2L, ((Number) tuples.get(4).get("post_id")).longValue());
        assertEquals("SQL:2011", tuples.get(4).get("post_title"));
        assertEquals(5L, ((Number) tuples.get(4).get("comment_id")).longValue());
        assertEquals("SQL:2011 is excellent!", tuples.get(4).get("comment_review"));

        Result<Record5<Long, String, Long, String, Integer>> posts = doInJOOQ(sql -> {
            // First CTE: p_pc
            CommonTableExpression<Record5<Long, String, Long, String, Integer>> pPc = name("p_pc")
                .fields("post_id", "post_title", "comment_id", "comment_review", "comment_count")
                .as(
                    select(
                        POST.ID.as("post_id"),
                        POST.TITLE.as("post_title"),
                        POST_COMMENT.ID.as("comment_id"),
                        POST_COMMENT.REVIEW.as("comment_review"),
                        count(POST_COMMENT.POST_ID).over(partitionBy(POST_COMMENT.POST_ID)).as("comment_count")
                    )
                    .from(POST)
                    .leftJoin(POST_COMMENT).on(POST.ID.eq(POST_COMMENT.POST_ID))
                    .where(POST.TITLE.like("SQL%"))
                );

            // Second CTE: p_pc_r
            Field<Long> postIdField = field(name("post_id"), Long.class);
            Field<String> postTitleField = field(name("post_title"), String.class);
            Field<Long> commentIdField = field(name("comment_id"), Long.class);
            Field<String> commentReviewField = field(name("comment_review"), String.class);
            Field<Integer> commentCountField = field(name("comment_count"), Integer.class);

            CommonTableExpression<Record5<Long, String, Long, String, Integer>> pPcR = name("p_pc_r")
                .fields("post_id", "post_title", "comment_id", "comment_review", "ranking")
                .as(
                    select(
                        postIdField,
                        postTitleField,
                        commentIdField,
                        commentReviewField,
                        denseRank().over(orderBy(commentCountField.desc())).as("ranking")
                    )
                    .from(pPc)
                );

            // Main query using CTEs
            return sql
                .with(pPc)
                .with(pPcR)
                .select(
                    field(name("post_id"), Long.class),
                    field(name("post_title"), String.class),
                    field(name("comment_id"), Long.class),
                    field(name("comment_review"), String.class),
                    field(name("ranking"), Integer.class)
                )
                .from(pPcR)
                .where(field(name("ranking"), Integer.class).le(2))
                .orderBy(field(name("post_id")), field(name("comment_id")))
                .fetch();
        });

        assertEquals(5, posts.size());

        assertEquals(1L, posts.get(0).get("post_id"));
        assertEquals("SQL:2016", posts.get(0).get("post_title"));
        assertEquals(1L, posts.get(0).get("comment_id"));
        assertEquals("SQL:2016 is great!", posts.get(0).get("comment_review"));

        assertEquals(2L, posts.get(3).get("post_id"));
        assertEquals("SQL:2011", posts.get(3).get("post_title"));
        assertEquals(4L, posts.get(3).get("comment_id"));
        assertEquals("SQL:2011 is great!", posts.get(3).get("comment_review"));

        assertEquals(2L, posts.get(4).get("post_id"));
        assertEquals("SQL:2011", posts.get(4).get("post_title"));
        assertEquals(5L, posts.get(4).get("comment_id"));
        assertEquals("SQL:2011 is excellent!", posts.get(4).get("comment_review"));
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        private Long id;

        private String title;

        public Long getId() {
            return id;
        }

        public Post setId(Long id) {
            this.id = id;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Post setTitle(String title) {
            this.title = title;
            return this;
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

        public PostComment setId(Long id) {
            this.id = id;
            return this;
        }

        public Post getPost() {
            return post;
        }

        public PostComment setPost(Post post) {
            this.post = post;
            return this;
        }

        public String getReview() {
            return review;
        }

        public PostComment setReview(String review) {
            this.review = review;
            return this;
        }
    }
}
