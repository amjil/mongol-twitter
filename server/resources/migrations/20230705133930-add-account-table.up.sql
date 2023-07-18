CREATE TABLE users (
    "id" uuid DEFAULT uuid_generate_v4 () primary key,
    username text,
    mobile text,
    email text,
    encrypted_password text,
    account_token text,
    account_token_send_at timestamp without time zone,
    reset_password_token text,
    reset_password_send_at timestamp without time zone,
    sign_in_count integer DEFAULT 0 NOT NULL,
    current_sign_in_at timestamp without time zone,
    current_sign_in_ip text,
    last_sign_in_ip text,
    failed_attempts integer DEFAULT 0 NOT NULL,
    unlock_token text,
    locked_at timestamp without time zone,
    status smallint DEFAULT 1 NOT NULL,
    level smallint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
--;;
create unique index users_mobile_index on users(mobile);
--;;
create unique index users_username_index on users(username);
--;;
create unique index users_email_index on users(email);
--;;
create unique index users_reset_password_token_index on users(reset_password_token);
--;;
create unique index users_unlock_token_index on users(unlock_token);
