DROP SCHEMA IF EXISTS test CASCADE;
CREATE SCHEMA test;
SET SCHEMA 'test';

CREATE DOMAIN username AS TEXT;

CREATE TABLE profile
(
    id                   BIGSERIAL PRIMARY KEY,
    username             username,
    password             TEXT,
    latest_activity_time BIGINT
);

CREATE TABLE room
(
    id   BIGSERIAL PRIMARY KEY,
    name username,
    color TEXT NOT NULL DEFAULT '#ffffff',
    font TEXT NOT NULL DEFAULT 'Arial' CHECK ( font IN ('Arial', 'Comic Sans MS', 'Times New Roman', 'Courier New', 'Brush Script MT'))
);

CREATE TABLE message
(
    id         BIGSERIAL PRIMARY KEY,
    body       TEXT,
    sent_by_id BIGINT,
    room_id    BIGINT,
    time       BIGINT,

    FOREIGN KEY (sent_by_id) REFERENCES profile (id),
    FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE room_user
(
    room_id             BIGINT,
    profile_id          BIGINT,
    state               VARCHAR(7) CHECK ( state IN ('Muted', 'Admin', 'Regular')),
    nickname            username,
    latest_read_message BIGINT REFERENCES message (id),

    PRIMARY KEY (room_id, profile_id),
    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (profile_id) REFERENCES profile (id)
);

CREATE TABLE attachment
(
    file_name  TEXT,
    message_id BIGINT,

    PRIMARY KEY (file_name, message_id),
    FOREIGN KEY (message_id) REFERENCES message (id)
);

CREATE TABLE reaction
(
    reacted_by BIGINT REFERENCES profile (id),
    message_id BIGINT REFERENCES message (id),
    reaction   TEXT,

    PRIMARY KEY (reacted_by, message_id, reaction)
);

CREATE TABLE blocklist
(
    blocked_by BIGINT REFERENCES profile (id),
    blocked BIGINT REFERENCES profile (id)
);

-- PRIVILEGES

GRANT ALL PRIVILEGES ON DATABASE sep2_chat TO sep2_chat;
GRANT ALL PRIVILEGES ON SCHEMA test TO sep2_chat;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA test TO sep2_chat;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA test TO sep2_chat;
