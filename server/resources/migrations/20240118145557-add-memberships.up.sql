CREATE TABLE IF NOT EXISTS memberships (
    id uuid DEFAULT uuid_generate_v4 () primary key,
    user_id uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    comm_id uuid NOT NULL REFERENCES communities ("id") ON DELETE CASCADE,
    is_admin smallint default 0,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);