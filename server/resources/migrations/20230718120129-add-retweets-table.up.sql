CREATE TABLE IF NOT EXISTS retweets (
    "tweet_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    "retweet_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    "user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    PRIMARY KEY ("tweet_id", "retweet_id")
);