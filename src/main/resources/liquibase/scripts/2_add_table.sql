--liquibase formatted sql

--changeset artak
--comment добавление таблицы user_credentials
CREATE  TABLE  user_credentials
(
    id      SERIAL PRIMARY KEY,
    access_token text UNIQUE NOT NULL,
    refresh_token   text UNIQUE   NOT NULL,
    time_to_expired DECIMAL NOT NULL,
    users_id int references users (id) ON DELETE CASCADE NOT NULL
);