package com.futsalmanager.domain.entities;

import com.futsalmanager.domain.enums.ResultadoJogo;
import com.futsalmanager.domain.enums.StatusJogo;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "jogo")
public class Jogo {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    @Column(nullable = false, length = 150)
    private String adversario;

    @Column(nullable = false, length = 100)
    private String local;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private StatusJogo statusJogo;

    /** Placar total do confronto — sempre a soma do 1º com o 2º quadro (quando houve os dois). */
    @Column(name = "gols_time")
    private Integer golsTime;

    @Column(name = "gols_adversario")
    private Integer golsAdversario;

    @Column(name = "gols_time_quadro1")
    private Integer golsTimeQuadro1;

    @Column(name = "gols_adversario_quadro1")
    private Integer golsAdversarioQuadro1;

    /** Nulo quando o confronto não teve segundo quadro. */
    @Column(name = "gols_time_quadro2")
    private Integer golsTimeQuadro2;

    @Column(name = "gols_adversario_quadro2")
    private Integer golsAdversarioQuadro2;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResultadoJogo resultado;

    @Column(columnDefinition = "text")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    protected Jogo() {
    }

     public Jogo(UUID id, Time time, String adversario, String local, LocalDateTime dataHora, StatusJogo statusJogo,
                 String observacoes, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.time = time;
        this.adversario = adversario;
        this.local = local;
        this.dataHora = dataHora;
        this.statusJogo = statusJogo;
        this.observacoes = observacoes;
        this.dataCriacao = dataCriacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getAdversario() {
        return adversario;
    }

    public void setAdversario(String adversario) {
        this.adversario = adversario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusJogo getStatusJogo() {
        return statusJogo;
    }

    public void setStatusJogo(StatusJogo statusJogo) {
        this.statusJogo = statusJogo;
    }

    public Integer getGolsTime() { return golsTime; }
    public void setGolsTime(Integer golsTime) { this.golsTime = golsTime; }

    public Integer getGolsAdversario() { return golsAdversario; }
    public void setGolsAdversario(Integer golsAdversario) { this.golsAdversario = golsAdversario; }

    public Integer getGolsTimeQuadro1() { return golsTimeQuadro1; }
    public void setGolsTimeQuadro1(Integer golsTimeQuadro1) { this.golsTimeQuadro1 = golsTimeQuadro1; }

    public Integer getGolsAdversarioQuadro1() { return golsAdversarioQuadro1; }
    public void setGolsAdversarioQuadro1(Integer golsAdversarioQuadro1) { this.golsAdversarioQuadro1 = golsAdversarioQuadro1; }

    public Integer getGolsTimeQuadro2() { return golsTimeQuadro2; }
    public void setGolsTimeQuadro2(Integer golsTimeQuadro2) { this.golsTimeQuadro2 = golsTimeQuadro2; }

    public Integer getGolsAdversarioQuadro2() { return golsAdversarioQuadro2; }
    public void setGolsAdversarioQuadro2(Integer golsAdversarioQuadro2) { this.golsAdversarioQuadro2 = golsAdversarioQuadro2; }

    public ResultadoJogo getResultado() { return resultado; }
    public void setResultado(ResultadoJogo resultado) { this.resultado = resultado; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jogo jogo)) return false;
        return id != null && id.equals(jogo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
