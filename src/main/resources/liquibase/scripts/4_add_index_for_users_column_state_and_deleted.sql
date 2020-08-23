--liquibase formatted sql

--changeset artak
--comment добавления indexa для users (state, deleted)
CREATE index ON users (state, deleted);