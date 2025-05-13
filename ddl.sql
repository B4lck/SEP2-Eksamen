DROP TABLE IF EXISTS public;

CREATE TABLE profile
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(30),
    password TEXT
);

CREATE TABLE room
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(30)
);

CREATE TABLE room_user
(
    room_id    BIGINT,
    profile_id BIGINT,
    state      VARCHAR(10) CHECK ( state IN ('Muted', 'Admin', 'Regular')),

    PRIMARY KEY (room_id, profile_id),
    FOREIGN KEY (room_id) REFERENCES room (id),
    FOREIGN KEY (profile_id) REFERENCES profile (id)
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

CREATE TABLE attachment
(
    file_name  TEXT,
    message_id BIGINT,

    PRIMARY KEY (file_name, message_id),
    FOREIGN KEY (message_id) REFERENCES message (id)
);

CREATE TABLE reaction
(
    reacted_by BIGINT REFERENCES profile(id),
    message_id BIGINT REFERENCES message(id),
    reaction TEXT
);