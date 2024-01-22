CREATE TABLE IF NOT EXISTS communities (
    id uuid DEFAULT uuid_generate_v4 () primary key,
    title varchar(100),
    created_by uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);