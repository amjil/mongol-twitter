CREATE TABLE IF NOT EXISTS followers (
    "followee_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "follower_id" uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "created_at" timestamp(0) without time zone NOT NULL,
    PRIMARY KEY ("followee_id", "follower_id")
);