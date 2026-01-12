package com.br.sistema.entities.Solicitacao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitacaoRelatorioDTO {

    private Long id;
    private LocalDate dataSolicitacao;
    private String status;
    private String carro;
    private String motorista;
    private String usuario;
    private String setor;
    private String destino;
    private Integer kmInicial;
    private Integer kmFinal;
    private LocalTime horaSaida;
    private LocalTime horaChegada;

    // ✅ Campo calculado (Jasper reconhece como getter)
    public String getKmTotal() {
        if (kmInicial == null || kmFinal == null) {
            return "-"; // Solicitação pendente ou incompleta
        }
        return String.valueOf(kmFinal - kmInicial);
    }
}
