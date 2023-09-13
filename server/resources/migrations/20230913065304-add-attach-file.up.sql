--drop table specimen_file;
--;;
create table attach_file (
	id serial primary key,
    filename text,
	url text,
	created_at timestamp default now()
);