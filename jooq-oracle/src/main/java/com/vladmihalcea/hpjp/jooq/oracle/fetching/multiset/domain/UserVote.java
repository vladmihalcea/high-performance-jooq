package com.vladmihalcea.jooq.oracle.fetching.multiset.domain;

import jakarta.persistence.*;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "UserVote")
@Table(name = "user_vote")
public class UserVote {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private PostComment comment;

    @Column(name = "vote_type")
    @Enumerated(EnumType.ORDINAL)
    private VoteType voteType;

    public Long getId() {
        return id;
    }

    public UserVote setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserVote setUser(User user) {
        this.user = user;
        return this;
    }

    public PostComment getComment() {
        return comment;
    }

    public UserVote setComment(PostComment comment) {
        this.comment = comment;
        return this;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public UserVote setVoteType(VoteType voteType) {
        this.voteType = voteType;
        return this;
    }
}
