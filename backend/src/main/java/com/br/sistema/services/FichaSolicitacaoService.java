package com.br.sistema.services;


import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoRequestDTO;
import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoResponseDTO;
import com.br.sistema.entities.FichaSolicitacao.FichaSolicitacao;
import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Setor.Setor;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoItemDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FichaSolicitacaoService {

    private final FichaSolicitacaoRepository fichaRepository;
    private final MotoristaRepository motoristaRepository;
    private final SetorRepository setorRepository;
    private final DestinoRepository destinoRepository;
    private final UsuarioRepository usuarioRepository;

    // =========================================================
    // SALVAR FICHA COM TODAS AS SOLICITAÇÕES DE UMA VEZ SÓ
    // =========================================================

    @Transactional
    public FichaSolicitacaoResponseDTO salvar(FichaSolicitacaoRequestDTO dto, Usuario usuarioLogado) {

        // 1. Monta a ficha (master)
        FichaSolicitacao ficha = new FichaSolicitacao();
        ficha.setDataViagem(dto.dataViagem());
        ficha.setPlacaVeiculo(dto.placaVeiculo().toUpperCase());
        ficha.setDataCriacao(LocalDateTime.now());
        ficha.setCriadoPor(usuarioLogado);

        // 2. Mapeia cada item do DTO para a entidade Solicitacao
        List<Solicitacao> solicitacoes = dto.solicitacoes().stream()
                .map(item -> montarSolicitacao(item, ficha, dto.dataViagem()))
                .toList();

        // 3. Vincula as solicitações à ficha
        ficha.setSolicitacoes(solicitacoes);

        // 4. Um único save persiste a ficha + todas as solicitações
        //    graças ao CascadeType.ALL definido na entidade
        FichaSolicitacao salva = fichaRepository.save(ficha);

        return FichaSolicitacaoResponseDTO.fromEntity(salva);
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<FichaSolicitacaoResponseDTO> listar(Pageable pageable) {
        return fichaRepository.findAll(pageable)
                .map(FichaSolicitacaoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public FichaSolicitacaoResponseDTO buscarPorId(Long id) {
        FichaSolicitacao ficha = fichaRepository.findByIdComSolicitacoes(id);
        if (ficha == null) {
            throw new EntityNotFoundException("Ficha de solicitação não encontrada com id: " + id);
        }
        return FichaSolicitacaoResponseDTO.fromEntity(ficha);
    }

    @Transactional(readOnly = true)
    public Page<FichaSolicitacaoResponseDTO> filtrar(
            String placa,
            LocalDate inicio,
            LocalDate fim,
            Long usuarioId,
            Pageable pageable
    ) {
        return fichaRepository.filtrar(placa, inicio, fim, usuarioId, pageable)
                .map(FichaSolicitacaoResponseDTO::fromEntity);
    }

    // =========================================================
    // EXCLUIR
    // =========================================================

    @Transactional
    public void excluir(Long id) {
        FichaSolicitacao ficha = fichaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha não encontrada com id: " + id));
        // ON DELETE CASCADE no banco + orphanRemoval=true na entidade
        // garantem que as solicitações filhas também sejam removidas
        fichaRepository.delete(ficha);
    }

    // =========================================================
    // MÉTODO PRIVADO: monta cada Solicitacao a partir do DTO
    // =========================================================

    private Solicitacao montarSolicitacao(SolicitacaoItemDTO item,
                                          FichaSolicitacao ficha,
                                          LocalDate dataViagem) {

        Motorista motorista = motoristaRepository.findById(item.motoristaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Motorista não encontrado com id: " + item.motoristaId()));

        Setor setor = setorRepository.findById(item.setorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Setor não encontrado com id: " + item.setorId()));

        Destino destino = destinoRepository.findById(item.destinoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Destino não encontrado com id: " + item.destinoId()));

        Usuario usuario = usuarioRepository.findById(item.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado com id: " + item.usuarioId()));

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDataSolicitacao(dataViagem);
        solicitacao.setStatus(item.status() != null ? item.status() : "PENDENTE");
        solicitacao.setKmInicial(item.kmInicial());
        solicitacao.setKmFinal(item.kmFinal());
        solicitacao.setHoraSaida(item.horaSaida());
        solicitacao.setHoraChegada(item.horaChegada());
        solicitacao.setMotorista(motorista);
        solicitacao.setSetor(setor);
        solicitacao.setDestino(destino);
        solicitacao.setUsuario(usuario);
        solicitacao.setFicha(ficha); // vínculo com o master

        return solicitacao;
    }
}