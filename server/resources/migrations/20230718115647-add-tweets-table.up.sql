CREATE TABLE IF NOT EXISTS tweets (
    "id" uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    "user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "content" varchar(280) CHECK (char_length("content") <= 280),
    "favorites_count" int DEFAULT 0,
    "replies_count" int DEFAULT 0,
    "reshare_count" int DEFAULT 0,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
--;;
CREATE INDEX IF NOT EXISTS tweets_created_at_idx ON tweets ("created_at");
