CREATE TABLE IF NOT EXISTS feed_tweets (
    "tweet_id" uuid NOT NULL REFERENCES tweets ("id") ON DELETE CASCADE,
    "feed_id" uuid NOT NULL REFERENCES feeds ("id") ON DELETE CASCADE,
    PRIMARY KEY ("tweet_id", "feed_id")
);
