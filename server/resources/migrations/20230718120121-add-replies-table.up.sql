CREATE TABLE IF NOT EXISTS replies (
    "tweet_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    "reply_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    PRIMARY KEY ("tweet_id", "reply_id")
);