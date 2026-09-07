-- Logo do time e banner do jogo (imagem pronta que o admin sobe, exibida no dashboard
-- pra todo mundo do time). Guardado direto no Postgres em vez de disco/S3 porque o
-- backend não tem armazenamento de arquivo persistente garantido entre deploys.
ALTER TABLE time ADD COLUMN logo bytea;
ALTER TABLE time ADD COLUMN logo_content_type varchar(50);
ALTER TABLE time ADD COLUMN banner bytea;
ALTER TABLE time ADD COLUMN banner_content_type varchar(50);
