package com.futsalmanager.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de despesas do time")
public enum TipoDespesa {

    @Schema(description = "Custo com aluguel da quadra")
    ALUGUEL_QUADRA,

    @Schema(description = "Compra de uniformes")
    UNIFORME,

    @Schema(description = "Custos relacionados a eventos")
    EVENTO,

    @Schema(description = "Outros tipos de despesas")
    OUTROS
}
