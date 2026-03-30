package com.br.sistema.entities.Solicitacao.DTO;

import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Usuario.DTO.UsuarioDetalhadoDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitacaoDetalhadaDTO(
        Long id,
        LocalDate dataSolicitacao,
        String status,
        String placaVeiculo,        // substituiu CarroDetalhadoDTO
        MotoristaDetalhadoDTO motorista,
        UsuarioDetalhadoDTO usuario,
        SetorDetalhadoDTO setor,
        DestinoDetalhadoDTO destino,
        Integer kmInicial,
        Integer kmFinal,
        LocalTime horaSaida,
        LocalTime horaChegada
) {}