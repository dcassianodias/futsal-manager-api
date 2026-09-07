-- Banner do jogo (imagem fixa por time) foi descontinuado: o admin agora gera e baixa
-- a imagem direto em Jogos, sem guardar/exibir um banner fixo no dashboard/perfil.
-- O logo do time continua (colunas logo/logo_content_type intactas).
ALTER TABLE time DROP COLUMN banner;
ALTER TABLE time DROP COLUMN banner_content_type;
