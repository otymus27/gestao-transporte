package com.br.sistema.services;

import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO;
import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoDetalhadaDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoPorStatusDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRelatorioDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoRequestDTO;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResponseDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.DestinoRepository;
import com.br.sistema.repositories.MotoristaRepository;
import com.br.sistema.repositories.SetorRepository;
import com.br.sistema.repositories.SolicitacaoRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class SolicitacaoService {

    private static final Set<String> ROLES_ESCRITA  = Set.of("ADMIN", "GERENTE", "SUPERVISOR", "BASIC");
    private static final Set<String> ROLES_EXCLUSAO = Set.of("ADMIN");

    private final SolicitacaoRepository solicitacaoRepository;
    private final MotoristaRepository   motoristaRepository;
    private final SetorRepository       setorRepository;
    private final DestinoRepository     destinoRepository;

    // UsuarioRepository REMOVIDO — usuario está na ficha, não na solicitação
    // CarroRepository   REMOVIDO — placa está na ficha
    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            MotoristaRepository   motoristaRepository,
            SetorRepository       setorRepository,
            DestinoRepository     destinoRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.motoristaRepository   = motoristaRepository;
        this.setorRepository       = setorRepository;
        this.destinoRepository     = destinoRepository;
    }

    // =========================================================
    // LEITURA
    // =========================================================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarTodosPaginado(Pageable pageable) {
        return solicitacaoRepository.findAll(pageable)
                .map(SolicitacaoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        return SolicitacaoResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public SolicitacaoDetalhadaDTO buscarDetalhado(Long id) {
        return toDetalhadaDTO(buscarEntidade(id));
    }

    // =========================================================
    // ESCRITA INDIVIDUAL
    // Criação em lote → FichaSolicitacaoService
    // =========================================================

    @Transactional(noRollbackFor = EntityExistsException.class)
    public SolicitacaoResponseDTO cadastrar(SolicitacaoRequestDTO dto,
                                            Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_ESCRITA);

        Solicitacao sol = new Solicitacao();
        preencherCampos(sol, dto);

        try {
            solicitacaoRepository.save(sol);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: solicitação já existe.");
        }

        return SolicitacaoResponseDTO.fromEntity(sol);
    }

    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id,
                                            SolicitacaoRequestDTO dto,
                                            Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_ESCRITA);

        Solicitacao sol = buscarEntidade(id);
        preencherCampos(sol, dto);
        solicitacaoRepository.save(sol);

        return SolicitacaoResponseDTO.fromEntity(sol);
    }

    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_EXCLUSAO);

        try {
            solicitacaoRepository.delete(buscarEntidade(id));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há vínculos de integridade.");
        }
    }

    // =========================================================
    // ATUALIZAÇÃO DE STATUS
    // =========================================================

    @Transactional
    public SolicitacaoResponseDTO atualizarStatus(Long id,
                                                  String novoStatus,
                                                  Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, Set.of("ADMIN", "GERENTE", "SUPERVISOR"));

        Solicitacao sol = buscarEntidade(id);
        sol.setStatus(novoStatus);
        solicitacaoRepository.save(sol);

        return SolicitacaoResponseDTO.fromEntity(sol);
    }

    // =========================================================
    // FILTROS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarGenerico(
            Long id,
            String status,
            Long motoristaId,
            Long setorId,
            Long destinoId,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {
        return solicitacaoRepository
                .filtrarDinamico(id, status, motoristaId, setorId, destinoId, inicio, fim, pageable)
                .map(SolicitacaoResponseDTO::fromEntity);
    }

    // =========================================================
    // DASHBOARDS / RELATÓRIOS
    // =========================================================

    public List<SolicitacaoPorDiaDTO> buscarPorIntervalo(LocalDate inicio, LocalDate fim) {
        return solicitacaoRepository.buscarPorDatas(inicio, fim);
    }

    public List<SolicitacaoPorMotoristaDTO> buscarPorMotorista() {
        return solicitacaoRepository.buscarPorMotorista();
    }

    public List<SolicitacaoPorSetorDTO> buscarPorSetor() {
        return solicitacaoRepository.buscarPorSetor();
    }

    public List<SolicitacaoPorUsuarioDTO> buscarPorUsuario() {
        return solicitacaoRepository.buscarPorUsuario();
    }

    public List<SolicitacaoPorStatusDTO> buscarPorStatus() {
        return solicitacaoRepository.buscarPorStatus();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoRelatorioDTO> gerarRelatorio(String filtro) {
        return solicitacaoRepository.filtrarSemPaginacao(filtro).stream()
                .map(this::toRelatorioDTO)
                .toList();
    }

    // =========================================================
    // PRIVATE
    // =========================================================

    private void validarAutenticacao(Usuario u) {
        if (u == null) throw new SecurityException("Usuário não autenticado.");
    }

    private void validarPermissao(Usuario u, Set<String> roles) throws AccessDeniedException {
        boolean ok = u.getRoles().stream().anyMatch(r -> roles.contains(r.getNome()));
        if (!ok) throw new AccessDeniedException("Usuário sem permissão para esta operação.");
    }

    private Solicitacao buscarEntidade(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
    }

    /**
     * Preenche campos editáveis da solicitação.
     * NÃO preenche: dataSolicitacao, usuario, carro/placa — todos na ficha.
     */
    private void preencherCampos(Solicitacao sol, SolicitacaoRequestDTO dto) {
        sol.setStatus(dto.status());
        sol.setKmInicial(dto.kmInicial());
        sol.setKmFinal(dto.kmFinal());
        sol.setHoraSaida(dto.horaSaida() != null ? LocalTime.parse(dto.horaSaida()) : null);
        sol.setHoraChegada(dto.horaChegada() != null ? LocalTime.parse(dto.horaChegada()) : null);

        sol.setMotorista(motoristaRepository.findById(dto.idMotorista())
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado.")));
        sol.setSetor(setorRepository.findById(dto.idSetor())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado.")));
        sol.setDestino(destinoRepository.findById(dto.idDestino())
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado.")));
    }

    private SolicitacaoDetalhadaDTO toDetalhadaDTO(Solicitacao s) {
        String placa    = s.getFicha() != null ? s.getFicha().getPlacaVeiculo() : null;
        String usuario  = s.getFicha() != null && s.getFicha().getUsuario() != null
                ? s.getFicha().getUsuario().getNome() : null;

        return new SolicitacaoDetalhadaDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                placa,
                usuario,
                MotoristaDetalhadoDTO.fromEntity(s.getMotorista(), false),
                SetorDetalhadoDTO.fromEntity(s.getSetor(), false),
                DestinoDetalhadoDTO.fromEntity(s.getDestino(), false),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    private SolicitacaoRelatorioDTO toRelatorioDTO(Solicitacao s) {
        String placa   = s.getFicha() != null ? s.getFicha().getPlacaVeiculo() : null;
        String usuario = s.getFicha() != null && s.getFicha().getUsuario() != null
                ? s.getFicha().getUsuario().getNome() : null;

        return new SolicitacaoRelatorioDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                placa,
                s.getMotorista().getNome(),
                usuario,
                s.getSetor().getNome(),
                s.getDestino().getNome(),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }
}
