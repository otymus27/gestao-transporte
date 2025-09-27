package br.com.carro.entities.DTO.formularios;

import br.com.carro.entities.Formulario;
import java.time.LocalDateTime;

public record FormularioDTO(
        Long id,
        String nomeArquivo,
        String extensao,
        long tamanho,
        LocalDateTime dataUpload,
        LocalDateTime dataAtualizacao
) {
    public static FormularioDTO fromEntity(Formulario f) {
        if (f == null) {
            throw new IllegalArgumentException("O formulário não pode ser nulo.");
        }

        String nomeArquivo = f.getNomeArquivo() != null ? f.getNomeArquivo() : "";
        String ext = "";

        int i = nomeArquivo.lastIndexOf('.');
        if (i >= 0 && i < nomeArquivo.length() - 1) {
            ext = nomeArquivo.substring(i + 1).toLowerCase(); // normaliza para minúsculo
        }

        return new FormularioDTO(
                f.getId(),
                nomeArquivo,
                ext,
                f.getTamanho(),
                f.getDataUpload(),
                f.getDataAtualizacao()
        );
    }
}
