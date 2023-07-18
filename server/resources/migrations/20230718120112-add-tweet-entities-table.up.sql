CREATE TABLE IF NOT EXISTS tweet_entities (
    "tweet_id" uuid REFERENCES tweets ON DELETE CASCADE,
    "media_links" text[] CHECK (array_length("media_links", 1) <= 6),
    "created_at" timestamp(0) without time zone NOT NULL
);