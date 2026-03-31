package com.br.sistema.services;

import com.br.sistema.entities.Destino.Destino;
import com.br.sistema.entities.FichaSolicitacao.*;
import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoRequestDTO;
import com.br.sistema.entities.FichaSolicitacao.DTO.FichaSolicitacaoResponseDTO;
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
    private final MotoristaRepository        motoristaRepository;
    private final SetorRepository            setorRepository;
    private final DestinoRepository          destinoRepository;
    // UsuarioRepository REMOVIDO das solicitações — usuario vem do token via controller

    // =========================================================
    // SALVAR — persiste ficha + todas as solicitações de uma vez
    // =========================================================

    @Transactional
    public FichaSolicitacaoResponseDTO salvar(FichaSolicitacaoRequestDTO dto,
                                              Usuario usuarioLogado) {
        FichaSolicitacao ficha = new FichaSolicitacao();
        ficha.setDataViagem(dto.dataViagem());
        ficha.setPlacaVeiculo(dto.placaVeiculo().toUpperCase());
        ficha.setDataCriacao(LocalDateTime.now());
        ficha.setUsuario(usuarioLogado); // usuário logado é dono da ficha

        List<Solicitacao> solicitacoes = dto.solicitacoes().stream()
                .map(item -> montarSolicitacao(item, ficha, dto.dataViagem()))
                .toList();

        ficha.setSolicitacoes(solicitacoes);

        return FichaSolicitacaoResponseDTO.fromEntity(fichaRepository.save(ficha));
    }

    // =========================================================
    // ATUALIZAR — atualiza dados da ficha e substitui solicitações
    // =========================================================

    @Transactional
    public FichaSolicitacaoResponseDTO atualizar(Long id,
                                                 FichaSolicitacaoRequestDTO dto,
                                                 Usuario usuarioLogado) {
        FichaSolicitacao ficha = fichaRepository.findByIdComSolicitacoes(id);
        if (ficha == null) throwNotFound(id);

        ficha.setDataViagem(dto.dataViagem());
        ficha.setPlacaVeiculo(dto.placaVeiculo().toUpperCase());
        ficha.setDataAtualizacao(LocalDateTime.now());
        ficha.setUsuario(usuarioLogado);

        // Limpa a lista existente — orphanRemoval=true cuida dos DELETEs
        ficha.getSolicitacoes().clear();

        // Adiciona na MESMA lista, sem trocar a referência
        dto.solicitacoes().stream()
                .map(item -> montarSolicitacao(item, ficha, dto.dataViagem()))
                .forEach(ficha.getSolicitacoes()::add);

        // saveAndFlush garante que o clear é processado antes dos INSERTs
        return FichaSolicitacaoResponseDTO.fromEntity(fichaRepository.saveAndFlush(ficha));
    }

    // =========================================================
    // LISTAGEM / CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<FichaSolicitacaoResponseDTO> listar(Pageable pageable) {
        return fichaRepository.findAll(pageable)
                .map(FichaSolicitacaoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public FichaSolicitacaoResponseDTO buscarPorId(Long id) {
        FichaSolicitacao ficha = fichaRepository.findByIdComSolicitacoes(id);
        if (ficha == null) throwNotFound(id);
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
        fichaRepository.delete(buscarEntidade(id));
    }

    // =========================================================
    // PRIVATE
    // =========================================================

    private Solicitacao montarSolicitacao(SolicitacaoItemDTO item,
                                          FichaSolicitacao ficha,
                                          LocalDate dataViagem) {
        Motorista motorista = motoristaRepository.findById(item.motoristaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Motorista não encontrado: id=" + item.motoristaId()));

        Setor setor = setorRepository.findById(item.setorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Setor não encontrado: id=" + item.setorId()));

        Destino destino = destinoRepository.findById(item.destinoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Destino não encontrado: id=" + item.destinoId()));

        Solicitacao sol = new Solicitacao();
        sol.setDataSolicitacao(dataViagem);
        sol.setStatus(item.status() != null ? item.status() : "PENDENTE");
        sol.setKmInicial(item.kmInicial());
        sol.setKmFinal(item.kmFinal());
        sol.setHoraSaida(item.horaSaida());
        sol.setHoraChegada(item.horaChegada());
        sol.setMotorista(motorista);
        sol.setSetor(setor);
        sol.setDestino(destino);
        sol.setFicha(ficha);

        return sol;
    }

    private FichaSolicitacao buscarEntidade(Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ficha não encontrada: id=" + id));
    }

    private FichaSolicitacao throwNotFound(Long id) {
        throw new EntityNotFoundException("Ficha não encontrada: id=" + id);
    }

    @Transactional(readOnly = true)
    public long contarFichas() {
        return fichaRepository.count();
    }
}