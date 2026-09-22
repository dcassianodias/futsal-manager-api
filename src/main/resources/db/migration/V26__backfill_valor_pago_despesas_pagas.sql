-- Despesas marcadas como PAGO antes do abatimento existir (V25) ficaram com valor_pago
-- zerado (default da coluna nova), fazendo a barra de progresso mostrar 0% mesmo pagas.
UPDATE despesa SET valor_pago = valor WHERE status = 'PAGO';
