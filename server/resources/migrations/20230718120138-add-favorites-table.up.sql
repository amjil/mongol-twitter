CREATE TABLE IF NOT EXISTS favorites (
    "user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "tweet_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    PRIMARY KEY ("tweet_id", "user_id")
);
