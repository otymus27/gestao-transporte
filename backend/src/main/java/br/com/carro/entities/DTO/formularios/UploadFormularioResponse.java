package br.com.carro.entities.DTO.formularios;

public record UploadFormularioResponse(
        Long id,
        String nomeArquivo,
        long tamanhoBytes,
        Long pastaId,
        String pastaNome,
        String mensagem
) {}
