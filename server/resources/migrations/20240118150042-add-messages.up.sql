CREATE TABLE IF NOT EXISTS messages (
    id uuid DEFAULT uuid_generate_v4 () primary key,
    is_system smallint default 0,
    content text,
    channel_id uuid NOT NULL REFERENCES channels ("id") ON DELETE CASCADE,
    from_user_id uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);