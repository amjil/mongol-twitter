CREATE TABLE IF NOT EXISTS notifications (
    "id" uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    "content" text,
    -- following notification 0 replies 1 favorites 2 retweets 3
    "types_of" smallint,
    "to_user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "from_user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "nty_id" uuid DEFAULT NULL
);
