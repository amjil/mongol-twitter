CREATE TABLE IF NOT EXISTS feeds (
    "id" uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    "user_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);