--drop table specimen_file;
--;;
create table attach_file (
	id serial primary key,
    filename text,
	url text,
	user_id uuid,
	created_at timestamp default now()
);