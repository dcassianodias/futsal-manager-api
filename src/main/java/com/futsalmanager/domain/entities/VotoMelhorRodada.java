package com.futsalmanager.domain.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "voto_melhor_rodada")
public class VotoMelhorRodada {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "votante_id", nullable = false)
    private Usuario votante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "votado_id", nullable = false)
    private Usuario votado;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected VotoMelhorRodada() {
    }

    public VotoMelhorRodada(Jogo jogo, Usuario votante, Usuario votado) {
        this.jogo = jogo;
        this.votante = votante;
        this.votado = votado;
    }

    public UUID getId() {
        return id;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public Usuario getVotante() {
        return votante;
    }

    public void setVotante(Usuario votante) {
        this.votante = votante;
    }

    public Usuario getVotado() {
        return votado;
    }

    public void setVotado(Usuario votado) {
        this.votado = votado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VotoMelhorRodada that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
