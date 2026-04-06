ALTER TABLE time ADD COLUMN codigo varchar(50);

CREATE UNIQUE INDEX uk_time_codigo ON time(codigo);