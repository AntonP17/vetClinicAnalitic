--liquibase formatted sql
--changeset antoxakon:3  -- Формат: author:id
--comment: create index in table

create index if not exists visit_history_uuid_index
    on visit_history (visit_id);