--liquibase formatted sql

--changeset artak
--comment добавление таблицы user
CREATE TABLE users
(
    id      SERIAL PRIMARY KEY,
    chat_id INT UNIQUE NOT NULL,
    state   VARCHAR(250) UNIQUE   NOT NULL
);