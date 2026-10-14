CREATE TABLE room (
    id       BIGSERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    floor    INT  NOT NULL,
    capacity INT  NOT NULL
);

CREATE TABLE speaker (
    id        BIGSERIAL PRIMARY KEY,
    name      TEXT NOT NULL,
    job_title TEXT NOT NULL,
    company   TEXT NOT NULL,
    country   TEXT NOT NULL,
    bio       TEXT NOT NULL
);

CREATE TABLE talk (
    id          BIGSERIAL PRIMARY KEY,
    title       TEXT      NOT NULL,
    description TEXT      NOT NULL,
    track       TEXT      NOT NULL,
    level       TEXT      NOT NULL,
    format      TEXT      NOT NULL,
    starts_at   TIMESTAMP NOT NULL,
    ends_at     TIMESTAMP NOT NULL,
    room_id     BIGINT    NOT NULL REFERENCES room (id)
);

CREATE TABLE talk_speaker (
    talk_id    BIGINT NOT NULL REFERENCES talk (id),
    speaker_id BIGINT NOT NULL REFERENCES speaker (id),
    PRIMARY KEY (talk_id, speaker_id)
);
