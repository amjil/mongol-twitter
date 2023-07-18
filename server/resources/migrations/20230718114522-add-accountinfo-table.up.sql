CREATE TABLE user_info (
  id uuid primary key,
  screen_name varchar(255) DEFAULT NULL ,
  sex smallint DEFAULT NULL ,
  bio varchar(100) DEFAULT NULL ,
  "location" text DEFAULT NULL,
  "birth_date" date DEFAULT NULL,
  profile_image_url text,
  profile_banner_url text,
  followers_count int DEFAULT 0,
  followings_count int DEFAULT 0,
  created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

--;;
CREATE INDEX user_info_screen_name ON user_info (screen_name);
