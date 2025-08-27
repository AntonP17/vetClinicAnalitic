--liquibase formatted sql
--changeset antoxakon:1  -- Формат: author:id
--comment: create schema vet_visits_history_status

create schema if not exists vet_visits_history_status;

