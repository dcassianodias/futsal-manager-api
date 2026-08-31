-- Uma mensalidade cancelada não deve travar a geração de uma nova pro mesmo
-- time/jogador/mês — só pode existir uma mensalidade ATIVA (não cancelada) por vez.
DROP INDEX uk_pagamento_mensalidade;

CREATE UNIQUE INDEX uk_pagamento_mensalidade
    ON pagamento(time_id, usuario_id, mes_referencia)
    WHERE tipo = 'MENSALIDADE' AND status <> 'CANCELADO';
