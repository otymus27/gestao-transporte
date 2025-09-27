package br.com.carro.entities.DTO.formularios;

import br.com.carro.entities.PastaFormulario;

public record PastaFormularioDTO(
        Long id,
        String nomePasta,
        String descricao,
        String caminhoCompleto,
        long quantidadeFormularios,
        long tamanhoTotalBytes
) {

    public static PastaFormularioDTO fromEntity(PastaFormulario pasta,
                                                long quantidadeFormularios,
                                                long tamanhoTotalBytes) {
        return new PastaFormularioDTO(
                pasta.getId(),
                pasta.getNomePasta(),
                pasta.getDescricao(),
                pasta.getCaminhoCompleto(),
                quantidadeFormularios,
                tamanhoTotalBytes
        );
    }
}