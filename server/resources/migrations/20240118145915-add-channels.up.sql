CREATE TABLE IF NOT EXISTS channels (
    id uuid DEFAULT uuid_generate_v4 () primary key,
    title text,
    comm_id uuid NOT NULL REFERENCES communities ("id") ON DELETE CASCADE,
    created_by uuid NOT NULL REFERENCES users ("id") ON DELETE CASCADE,
    "created_at" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);