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
import com.br.sistema.entities.Usuario.DTO.UsuarioDetalhadoDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.DestinoRepository;
import com.br.sistema.repositories.MotoristaRepository;
import com.br.sistema.repositories.SetorRepository;
import com.br.sistema.repositories.SolicitacaoRepository;
import com.br.sistema.repositories.UsuarioRepository;
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

    private static final Set<String> ROLES_ESCRITA  = Set.of("ADMIN", "GERENTE", "BASIC");
    private static final Set<String> ROLES_EXCLUSAO = Set.of("ADMIN");

    private final SolicitacaoRepository solicitacaoRepository;
    private final MotoristaRepository   motoristaRepository;
    private final UsuarioRepository     usuarioRepository;
    private final SetorRepository       setorRepository;
    private final DestinoRepository     destinoRepository;

    // CarroRepository REMOVIDO — placa está na FichaSolicitacao
    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            MotoristaRepository   motoristaRepository,
            UsuarioRepository     usuarioRepository,
            SetorRepository       setorRepository,
            DestinoRepository     destinoRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.motoristaRepository   = motoristaRepository;
        this.usuarioRepository     = usuarioRepository;
        this.setorRepository       = setorRepository;
        this.destinoRepository     = destinoRepository;
    }

    // =========================================================
    // LEITURA
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

    // =========================================================
    // ESCRITA
    // Criação em lote → FichaSolicitacaoService
    // Aqui: cadastro avulso, atualização e exclusão individual
    // =========================================================

    @Transactional(noRollbackFor = EntityExistsException.class)
    public SolicitacaoResponseDTO cadastrar(SolicitacaoRequestDTO dto,
                                            Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_ESCRITA);

        Solicitacao solicitacao = new Solicitacao();
        preencherCampos(solicitacao, dto, usuarioLogado);


        try {
            solicitacaoRepository.save(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: solicitação já existe.");
        }

        return toResponseDTO(solicitacao);
    }

    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id,
                                            SolicitacaoRequestDTO dto,
                                            Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_ESCRITA);

        Solicitacao solicitacao = buscarEntidadePorId(id);
        preencherCampos(solicitacao, dto, usuarioLogado);
        solicitacaoRepository.save(solicitacao);

        return toResponseDTO(solicitacao);
    }

    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, ROLES_EXCLUSAO);

        Solicitacao solicitacao = buscarEntidadePorId(id);

        try {
            solicitacaoRepository.delete(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há vínculos de integridade.");
        }
    }

    // =========================================================
    // ATUALIZAÇÃO DE STATUS (aprovar / recusar)
    // =========================================================

    @Transactional
    public SolicitacaoResponseDTO atualizarStatus(Long id,
                                                  String novoStatus,
                                                  Usuario usuarioLogado) throws AccessDeniedException {
        validarAutenticacao(usuarioLogado);
        validarPermissao(usuarioLogado, Set.of("ADMIN", "GERENTE"));

        Solicitacao solicitacao = buscarEntidadePorId(id);
        solicitacao.setStatus(novoStatus);
        solicitacaoRepository.save(solicitacao);

        return toResponseDTO(solicitacao);
    }

    // =========================================================
    // FILTROS — carroId REMOVIDO
    // =========================================================

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarGenerico(
            Long id,
            String status,
            Long motoristaId,
            Long setorId,
            String username,
            Long destinoId,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {
        return solicitacaoRepository
                .filtrarDinamico(id, status, motoristaId, setorId, username, destinoId, inicio, fim, pageable)
                .map(this::toResponseDTO);
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
    // PRIVATE — validação
    // =========================================================

    private void validarAutenticacao(Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }
    }

    private void validarPermissao(Usuario usuarioLogado,
                                  Set<String> rolesPermitidas) throws AccessDeniedException {
        boolean permitido = usuarioLogado.getRoles().stream()
                .anyMatch(r -> rolesPermitidas.contains(r.getNome()));
        if (!permitido) {
            throw new AccessDeniedException("Usuário sem permissão para esta operação.");
        }
    }

    private Solicitacao buscarEntidadePorId(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
    }

    // =========================================================
    // PRIVATE — preenchimento
    // Não preenche: dataSolicitacao (vem da ficha), carro/placa (vem da ficha)
    // =========================================================

    private void preencherCampos(Solicitacao solicitacao, SolicitacaoRequestDTO dto, Usuario usuarioLogado) {
        solicitacao.setStatus(dto.status());
        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida() != null ? LocalTime.parse(dto.horaSaida()) : null);
        solicitacao.setHoraChegada(dto.horaChegada() != null ? LocalTime.parse(dto.horaChegada()) : null);

        solicitacao.setMotorista(motoristaRepository.findById(dto.idMotorista())
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado.")));
        solicitacao.setSetor(setorRepository.findById(dto.idSetor())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado.")));
        solicitacao.setDestino(destinoRepository.findById(dto.idDestino())
                .orElseThrow(() -> new EntityNotFoundException("Destino não encontrado.")));
        solicitacao.setUsuario(usuarioLogado); // vem do token, não do DTO
    }

    // =========================================================
    // PRIVATE — mapeamentos
    // Placa buscada via ficha master — null-safe para solicitações avulsas
    // =========================================================

    private SolicitacaoResponseDTO toResponseDTO(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getStatus(),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada(),
                s.getMotorista().getId(),
                s.getMotorista().getNome(),
                s.getSetor().getId(),
                s.getSetor().getNome(),
                s.getDestino().getId(),
                s.getDestino().getNome(),
                s.getUsuario().getId(),
                s.getUsuario().getNome()
        );
    }

    private SolicitacaoDetalhadaDTO toDetalhadaDTO(Solicitacao s) {
        String placa = s.getFicha() != null ? s.getFicha().getPlacaVeiculo() : null;

        return new SolicitacaoDetalhadaDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                placa,
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
        String placa = s.getFicha() != null ? s.getFicha().getPlacaVeiculo() : null;

        return new SolicitacaoRelatorioDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus(),
                placa,
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