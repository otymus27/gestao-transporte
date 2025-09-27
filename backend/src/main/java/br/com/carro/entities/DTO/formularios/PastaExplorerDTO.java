package br.com.carro.entities.DTO.formularios;

import br.com.carro.entities.PastaFormulario;

import java.time.LocalDateTime;
import java.util.List;

public record PastaExplorerDTO(
        Long id,
        String nomePasta,
        String descricao,
        String caminhoCompleto,
        long quantidade,
        long tamanho,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        List<FormularioDTO> formularios
) {
    public static PastaExplorerDTO fromEntity(PastaFormulario pasta,
                                              long quantidade,
                                              long tamanho,
                                              List<FormularioDTO> formularios) {
        return new PastaExplorerDTO(
                pasta.getId(),
                pasta.getNomePasta(),
                pasta.getDescricao(),
                pasta.getCaminhoCompleto(),
                quantidade,
                tamanho,
                pasta.getDataCriacao(),
                pasta.getDataAtualizacao(),
                formularios
        );
    }
}
