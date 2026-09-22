package com.futsalmanager.domain.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "despesa_abatimento")
public class DespesaAbatimento {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despesa_id", nullable = false)
    private Despesa despesa;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(length = 255)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    protected DespesaAbatimento() {}

    public DespesaAbatimento(Despesa despesa, BigDecimal valor, LocalDate dataPagamento,
                              String observacao, Usuario registradoPor) {
        this.despesa = despesa;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.observacao = observacao;
        this.registradoPor = registradoPor;
    }

    public UUID getId() {
        return id;
    }

    public Despesa getDespesa() {
        return despesa;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DespesaAbatimento that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
