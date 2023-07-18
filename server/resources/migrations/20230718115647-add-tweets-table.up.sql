CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--;;
CREATE TABLE IF NOT EXISTS tweets (
    "id" uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    "user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "content" varchar(280) CHECK (char_length("content") <= 280),
    "favorites_count" int,
    "replies_count" int,
    "created_at" timestamp(0) without time zone NOT NULL
);
--;;
CREATE INDEX IF NOT EXISTS tweets_created_at_idx ON tweets ("created_at");
