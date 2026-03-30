package com.br.sistema.services;

import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
import com.br.sistema.entities.DTO.SolicitacaoPorDiaDTO;
import com.br.sistema.entities.Destino.DTO.DestinoDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDetalhadoDTO;
import com.br.sistema.entities.Motorista.DTO.SolicitacaoPorMotoristaDTO;
import com.br.sistema.entities.Setor.DTO.SetorDetalhadoDTO;
import com.br.sistema.entities.Setor.DTO.SolicitacaoPorSetorDTO;
import com.br.sistema.entities.Solicitacao.DTO.*;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.DTO.SolicitacaoPorUsuarioDTO;
import com.br.sistema.entities.Usuario.DTO.UsuarioDetalhadoDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class SolicitacaoService {

    private static final Set<String> ROLES_PERMITIDAS = Set.of("ADMIN", "GERENTE", "BASIC");

    private final SolicitacaoRepository solicitacaoRepository;
    private final CarroRepository carroRepository;
    private final MotoristaRepository motoristaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;
    private final DestinoRepository destinoRepository;

    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            CarroRepository carroRepository,
            MotoristaRepository motoristaRepository,
            UsuarioRepository usuarioRepository,
            SetorRepository setorRepository,
            DestinoRepository destinoRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.carroRepository = carroRepository;
        this.motoristaRepository = motoristaRepository;
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        this.destinoRepository = destinoRepository;
    }

    // =========================================================
    // CRUD
    // =========================================================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarTodosPaginado(Pageable pageable) {
        return solicitacaoRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public SolicitacaoDetalhadaDTO buscarDetalhado(Long id) {
        return toDetalhadaDTO(buscarEntidadePorId(id));
    }

    @Transactional(noRollbackFor = EntityExistsException.class)
    public SolicitacaoResponseDTO cadastrar(SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_PERMITIDAS);

        Solicitacao solicitacao = new Solicitacao();
        preencherCampos(solicitacao, dto);

        try {
            solicitacaoRepository.save(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: solicitação já existe.");
        }

        return toResponseDTO(solicitacao);
    }

    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id, SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_PERMITIDAS);

        Solicitacao solicitacao = buscarEntidadePorId(id);
        preencherCampos(solicitacao, dto);
        solicitacaoRepository.save(solicitacao);

        return toResponseDTO(solicitacao);
    }

    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, Set.of("ADMIN"));

        Solicitacao solicitacao = buscarEntidadePorId(id);

        try {
            solicitacaoRepository.delete(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há vínculos de integridade.");
        }
    }

    // =========================================================
    // FILTROS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarGenerico(
            Long id,
            String status,
            Long motoristaId,
            Long carroId,
            Long setorId,
            String username,
            Long destinoId,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {
        // Delega ao repository a query dinâmica com todos os filtros combinados
        return solicitacaoRepository
                .filtrarDinamico(id, status, motoristaId, carroId, setorId, username, destinoId, inicio, fim, pageable)
                .map(this::toResponseDTO);
    }

    // =========================================================
    // RELATÓRIOS / DASHBOARDS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitacaoRelatorioDTO> gerarRelatorio(String filtro) {
        return solicitacaoRepository.filtrarSemPaginacao(filtro).stream()
                .map(this::toRelatorioDTO)
                .toList();
    }

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

    // =========================================================
    // MÉTODOS PRIVADOS
    // =========================================================

    /** Garante que o usuário está autenticado. */
    private void validarAutenticacao(Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }
    }

    /** Garante que o usuário possui ao menos uma das roles exigidas. */
    private void validarPermissao(Usuario usuarioLogado, Set<String> rolesPermitidas) throws AccessDeniedException {
        boolean permitido = usuarioLogado.getRoles().stream()
                .anyMatch(r -> rolesPermitidas.contains(r.getNome()));
        if (!permitido) {
            throw new AccessDeniedException("Usuário não tem permissão para realizar esta operação.");
        }
    }

    /** Busca a entidade pelo ID ou lança exceção padronizada. */
    private Solicitacao buscarEntidadePorId(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
    }

    /**
     * Preenche os campos da entidade a partir do DTO.
     * Usado tanto no cadastro quanto na atualização — elimina duplicação.
     */
    private void preencherCampos(Solicitacao solicitacao, SolicitacaoRequestDTO dto) {
        // dataSolicitacao removido daqui — vem da FichaSolicitacao
        solicitacao.setStatus(dto.status());
        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setHoraChegada(dto.horaChegada());

        solicitacao.setCarro(carroRepository.findById(dto.carroId())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado.")));
        solicitacao.setMotorista(motoristaRepository.findById(dto.motoristaId())
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado.")));
        solicitacao.setUsuario(usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado.")));
        solicitacao.setSetor(setorRepository.findById(dto.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado.")));
        solicitacao.setDestino(destinoRepository.findById(dto.destinoId())
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado.")));
    }

    private SolicitacaoResponseDTO toResponseDTO(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                s.getCarro().getId(),
                s.getCarro().getPlaca(),
                s.getCarro().getModelo(),
                s.getMotorista().getId(),
                s.getMotorista().getNome(),
                s.getUsuario().getId(),
                s.getUsuario().getNome(),
                s.getUsuario().getUsername(),
                s.getSetor().getId(),
                s.getSetor().getNome(),
                s.getDestino().getId(),
                s.getDestino().getNome(),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    private SolicitacaoDetalhadaDTO toDetalhadaDTO(Solicitacao s) {
        return new SolicitacaoDetalhadaDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                CarroDetalhadoDTO.fromEntity(s.getCarro(), false),
                MotoristaDetalhadoDTO.fromEntity(s.getMotorista(), false),
                UsuarioDetalhadoDTO.fromEntity(s.getUsuario(), false),
                SetorDetalhadoDTO.fromEntity(s.getSetor(), false),
                DestinoDetalhadoDTO.fromEntity(s.getDestino(), false),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    private SolicitacaoRelatorioDTO toRelatorioDTO(Solicitacao s) {
        return new SolicitacaoRelatorioDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                s.getCarro().getPlaca(),
                s.getMotorista().getNome(),
                s.getUsuario().getNome(),
                s.getSetor().getNome(),
                s.getDestino().getNome(),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }
}