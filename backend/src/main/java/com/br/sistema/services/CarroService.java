package com.br.sistema.services;

import com.br.sistema.entities.Carro.Carro;
import com.br.sistema.entities.Carro.DTO.CarroDTO;
import com.br.sistema.entities.Carro.DTO.CarroRequestDTO;
import com.br.sistema.entities.Usuario.Usuario;
import com.br.sistema.repositories.CarroRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
public class CarroService {

    @Autowired
    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    // ✅ Cadastro com validações e controle de permissão (padrão Motorista)
    @Transactional(noRollbackFor = EntityExistsException.class)
    public CarroDTO cadastrar(CarroRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.marca() == null || dto.marca().isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória.");
        }
        if (dto.modelo() == null || dto.modelo().isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório.");
        }
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode cadastrar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar carros.");
        }

        // 4️⃣ Validar duplicidade de placa
        if (carroRepository.existsByPlaca(dto.placa().trim())) {
            throw new EntityExistsException("Já existe carro com esta placa.");
        }

        // 5️⃣ Criar entidade Carro
        Carro carro = new Carro();
        carro.setMarca(dto.marca().trim());
        carro.setModelo(dto.modelo().trim());
        carro.setPlaca(dto.placa().trim());

        try {
            carroRepository.save(carro);
        } catch (DataIntegrityViolationException e) {
            // (unique constraint em placa, por exemplo)
            throw new EntityExistsException("Erro ao salvar: carro já existe.");
        }

        // ✅ Converte entidade para DTO (sem solicitações no cadastro)
        return CarroDTO.fromEntity(carro, false);
    }

    // ✅ Atualização com mesmas regras de permissão/validação
    @Transactional(noRollbackFor = EntityExistsException.class)
    public CarroDTO atualizar(Long id, CarroRequestDTO dto, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Validar dados obrigatórios
        if (dto.marca() == null || dto.marca().isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória.");
        }
        if (dto.modelo() == null || dto.modelo().isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório.");
        }
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }

        // 3️⃣ Verificar permissões (somente ADMIN pode atualizar carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar carros.");
        }

        // 4️⃣ Buscar entidade
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        // 5️⃣ Checar duplicidade de placa (ignora o próprio carro)
        carroRepository.findByPlaca(dto.placa().trim())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> { throw new EntityExistsException("Já existe outro carro com esta placa."); });

        // 6️⃣ Aplicar alterações
        carro.setMarca(dto.marca().trim());
        carro.setModelo(dto.modelo().trim());
        carro.setPlaca(dto.placa().trim());

        try {
            carroRepository.save(carro);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException("Erro ao atualizar: placa já em uso.");
        }

        return CarroDTO.fromEntity(carro, false);
    }

    // ✅ Listagem paginada (sem solicitações para ficar leve)
    @Transactional(readOnly = true)
    public Page<CarroDTO> listar(Pageable pageable) {
        return carroRepository.findAll(pageable)
                .map(c -> CarroDTO.fromEntity(c, false));
    }

    // ✅ Detalhe por ID (permite controlar se inclui solicitações)
    @Transactional(readOnly = true)
    public CarroDTO buscarPorId(Long id, boolean incluirSolicitacoes) {
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        return CarroDTO.fromEntity(carro, incluirSolicitacoes);
    }

    // ✅ Busca por placa (útil para telas/relatórios)
    @Transactional(readOnly = true)
    public CarroDTO buscarPorPlaca(String placa, boolean incluirSolicitacoes) {
        Carro carro = carroRepository.findByPlaca(placa.trim())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com placa " + placa));
        return CarroDTO.fromEntity(carro, incluirSolicitacoes);
    }

    // ✅ Exclusão com checagem de permissão e tratamento de FK
    @Transactional(noRollbackFor = EntityNotFoundException.class)
    public void deletar(Long id, Usuario usuarioLogado) throws AccessDeniedException {

        // 1️⃣ Validar usuário autenticado
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // 2️⃣ Verificar permissões (somente ADMIN pode excluir carros)
        boolean temPermissao = usuarioLogado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getNome()));
        if (!temPermissao) {
            throw new AccessDeniedException("Usuário não tem permissão para excluir carros.");
        }

        // 3️⃣ Validar existência ANTES de excluir
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com id " + id));

        // 4️⃣ Excluir com tratamento de integridade (FK em solicitações)
        try {
            carroRepository.delete(carro); // ✅ agora deletamos a entidade já carregada
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Não é possível excluir: há solicitações vinculadas a este carro.");
        }
    }


}
