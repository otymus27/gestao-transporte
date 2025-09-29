package com.br.sistema.services;

import com.br.sistema.entities.Motorista.DTO.MotoristaRequestDTO;
import com.br.sistema.entities.Motorista.DTO.MotoristaDTO;
import com.br.sistema.entities.Motorista.Motorista;
import com.br.sistema.entities.Solicitacao.DTO.SolicitacaoResumoDTO;
import com.br.sistema.entities.Solicitacao.Solicitacao;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.MotoristaRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;

    public MotoristaService(MotoristaRepository motoristaRepository) {
        this.motoristaRepository = motoristaRepository;
    }

    //Listagem simples com paginação
    @Transactional(readOnly = true)
    public Page<MotoristaDTO> listarTodosPaginado(Pageable pageable) {
        return motoristaRepository.findAll(pageable)
                .map(m -> MotoristaDTO.fromEntity(m, false)); // 🚫 sem solicitações
    }

    // ✅ Buscar um motorista por id
    @Transactional(readOnly = true)
    public MotoristaDTO buscarPorId(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));
        return MotoristaDTO.fromEntity(motorista, false); // 🚫 não traz solicitações
    }



    // ✅ Cadastro com validações e controle de permissão
    @Transactional(noRollbackFor = EntityExistsException.class)
    public MotoristaDTO cadastrar(
            MotoristaRequestDTO dto,
            Usuario usuarioLogado
    ) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.matricula() == null || dto.matricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (dto.telefone() == null || dto.telefone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar motoristas)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> r.getNome().equals("ADMIN"));

        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar motoristas.");
        }

        // 4️⃣ Validar duplicidade
        if (motoristaRepository.existsByMatricula(dto.matricula())) {
            throw new EntityExistsException("Já existe motorista com esta matrícula.");
        }

        // 5️⃣ Criar entidade Motorista
        Motorista motorista = new Motorista();
        motorista.setMatricula(dto.matricula().trim());
        motorista.setNome(dto.nome().trim());
        motorista.setTelefone(dto.telefone().trim());

        try {
            motoristaRepository.save(motorista);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao salvar: motorista já existe.");
        }

        // ✅ Agora converte a entidade para DTO
        return MotoristaDTO.fromEntity(motorista,false);
    }



    // ✅ Atualizar motorista (inclui matrícula também)
    @Transactional
    public MotoristaDTO atualizar(Long id, MotoristaRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // Apenas ADMIN pode atualizar
        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar motoristas.");
        }

        // Validações básicas
        if (dto.matricula() == null || dto.matricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatória.");
        }
        if (dto.telefone() == null || dto.telefone().isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }

        // Busca motorista existente
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));

        // Atualiza dados
        motorista.setMatricula(dto.matricula().trim());
        motorista.setNome(dto.nome().trim());
        motorista.setTelefone(dto.telefone().trim());

        motoristaRepository.save(motorista);


        motoristaRepository.save(motorista);

        // Retorna DTO sem solicitações
        return MotoristaDTO.fromEntity(motorista, false);
    }

    // ✅ Deletar motorista
    @Transactional
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // Apenas ADMIN pode deletar
        boolean isAdmin = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!isAdmin) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar motoristas.");
        }

        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));

        motoristaRepository.delete(motorista);
    }


    // ✅ Buscar todos (resumido: sem lista de solicitações)
    @Transactional(readOnly = true)
    public List<MotoristaDTO> listarTodosResumido() {
        return motoristaRepository.findAll().stream()
                .map(m -> new MotoristaDTO(
                        m.getId(),
                        m.getMatricula(),
                        m.getNome(),
                        m.getTelefone(),
                        List.of() // vazio para não pesar na listagem
                ))
                .toList();
    }



    // ✅ Buscar detalhado (motorista + solicitações atendidas)
    @Transactional(readOnly = true)
    public MotoristaDTO buscarDetalhado(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado."));
        return MotoristaDTO.fromEntity(motorista, true); // ✅ inclui solicitações
    }


    // 🔹 Método auxiliar para mapear solicitação em DTO enxuto
    private SolicitacaoResumoDTO mapSolicitacaoResumo(Solicitacao s) {
        return new SolicitacaoResumoDTO(
                s.getId(),
                s.getDataSolicitacao(),
                s.getStatus()
        );
    }
}
