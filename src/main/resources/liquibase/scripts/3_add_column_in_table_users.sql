--liquibase formatted sql

--changeset artak
--comment добавления колонки deleted в таблицу users
ALTER TABLE users ADD COLUMN deleted BOOLEAN NOT NULL default false;