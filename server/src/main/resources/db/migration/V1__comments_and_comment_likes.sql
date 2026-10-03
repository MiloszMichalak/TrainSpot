CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    spot_id UUID NOT NULL REFERENCES spots(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS comments_spot_id_created_at_id
    ON comments (spot_id, created_at, id);
CREATE INDEX IF NOT EXISTS comments_user_id
    ON comments (user_id);

CREATE TABLE IF NOT EXISTS comment_likes (
    comment_id UUID NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id, user_id)
);

CREATE INDEX IF NOT EXISTS comment_likes_user_id
    ON comment_likes (user_id);
