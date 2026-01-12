package com.br.sistema.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.sistema.entities.Carro.DTO.CarroDetalhadoDTO;
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
import com.br.sistema.repositories.CarroRepository;
import com.br.sistema.repositories.DestinoRepository;
import com.br.sistema.repositories.MotoristaRepository;
import com.br.sistema.repositories.SetorRepository;
import com.br.sistema.repositories.SolicitacaoRepository;
import com.br.sistema.repositories.UsuarioRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class SolicitacaoService {

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

    // ✅ Listagem paginada (resumida)
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listarTodosPaginado(Pageable pageable) {
        return solicitacaoRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Buscar por ID (resumido)
    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
        return toResponseDTO(solicitacao);
    }

    // ✅ Buscar detalhado
    @Transactional(readOnly = true)
    public SolicitacaoDetalhadaDTO buscarDetalhado(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));
        return toDetalhadaDTO(solicitacao);
    }

    // ✅ Cadastro
    @Transactional(noRollbackFor = EntityExistsException.class)
    public SolicitacaoResponseDTO cadastrar(SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 🔐 Apenas ADMIN e GERENTE podem cadastrar solicitações
        boolean permitido = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN") || r.getNome().equals("GERENTE") || r.getNome().equals("BASIC"));
        if (!permitido) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar solicitações.");
        }

        // Criar entidade
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDataSolicitacao(dto.dataSolicitacao());
        solicitacao.setStatus(dto.status());

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

        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setHoraChegada(dto.horaChegada());

        try {
            solicitacaoRepository.save(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: solicitação já existe.");
        }

        return toResponseDTO(solicitacao);
    }

    // ✅ Atualização
    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id, SolicitacaoRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 🔐 Apenas ADMIN e GERENTE podem atualizar solicitações
        boolean permitido = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN") || r.getNome().equals("GERENTE") || r.getNome().equals("BASIC"));
        if (!permitido) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar solicitações.");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        solicitacao.setDataSolicitacao(dto.dataSolicitacao());
        solicitacao.setStatus(dto.status());
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
        solicitacao.setKmInicial(dto.kmInicial());
        solicitacao.setKmFinal(dto.kmFinal());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setHoraChegada(dto.horaChegada());

        solicitacaoRepository.save(solicitacao);

        return toResponseDTO(solicitacao);
    }

    // ✅ Exclusão
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir solicitações.");
        }

        var solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        try {
            solicitacaoRepository.delete(solicitacao);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há vínculos de integridade.");
        }
    }

    // 🔹 Mapear entidade → DTOs
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
                CarroDetalhadoDTO.fromEntity(s.getCarro(),false),
                MotoristaDetalhadoDTO.fromEntity(s.getMotorista(), false),
                UsuarioDetalhadoDTO.fromEntity(s.getUsuario(), false),
                SetorDetalhadoDTO.fromEntity(s.getSetor(),false),
                DestinoDetalhadoDTO.fromEntity(s.getDestino(), false),
                s.getKmInicial(),
                s.getKmFinal(),
                s.getHoraSaida(),
                s.getHoraChegada()
        );
    }

    // ✅ Filtrar por status
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorStatus(String status, Pageable pageable) {
        return solicitacaoRepository.findByStatus(status, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por motorista
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorMotorista(Long motoristaId, Pageable pageable) {
        return solicitacaoRepository.findByMotorista_Id(motoristaId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por carro
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorCarro(Long carroId, Pageable pageable) {
        return solicitacaoRepository.findByCarro_Id(carroId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por setor
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorSetor(Long setorId, Pageable pageable) {
        return solicitacaoRepository.findBySetor_Id(setorId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por usuário
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorUsuario(Long usuarioId, Pageable pageable) {
        return solicitacaoRepository.findByUsuario_Id(usuarioId, pageable)
                .map(this::toResponseDTO);
    }

    // ✅ Filtrar por destino
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> filtrarPorDestino(Long destinoId, Pageable pageable) {
        return solicitacaoRepository.findByDestino_Id(destinoId, pageable)
                .map(this::toResponseDTO);
    }


    // ✅ Filtro genérico por múltiplos parâmetros
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

        // Caso só um filtro seja usado, direcionamos pro repository certo
        if (id != null) {
            return solicitacaoRepository.findById(id, pageable).map(this::toResponseDTO);
        }
        if (status != null) {
            return solicitacaoRepository.findByStatus(status, pageable).map(this::toResponseDTO);
        }
        if (motoristaId != null) {
            return solicitacaoRepository.findByMotorista_Id(motoristaId, pageable).map(this::toResponseDTO);
        }
        if (carroId != null) {
            return solicitacaoRepository.findByCarro_Id(carroId, pageable).map(this::toResponseDTO);
        }
        if (setorId != null) {
            return solicitacaoRepository.findBySetor_Id(setorId, pageable).map(this::toResponseDTO);
        }
        if (username != null) {
            return solicitacaoRepository.findByUsernameContainingIgnoreCase(username, pageable).map(this::toResponseDTO);
        }

        // ✅ Novo filtro: intervalo de datas
        if (inicio != null && fim != null) {
            return solicitacaoRepository
                    .findByDataSolicitacaoBetween(inicio, fim, pageable)
                    .map(this::toResponseDTO);
        }

        if (destinoId != null) {
            return solicitacaoRepository.findByDestino_Id(destinoId, pageable).map(this::toResponseDTO);
        }

        // 🔹 Se nenhum filtro informado, retorna tudo
        return solicitacaoRepository.findAll(pageable).map(this::toResponseDTO);
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

// Método para gerar relatorio completo da lista
// ✅ Gerar lista de DTOs com filtros aplicados
@Transactional(readOnly = true)
public List<SolicitacaoRelatorioDTO> gerarRelatorio(String filtro) {
    return solicitacaoRepository.filtrarSemPaginacao(filtro).stream()
            .map(s -> new SolicitacaoRelatorioDTO(
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
            ))
            .toList();
}

}
