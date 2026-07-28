package com.br.sistema.entities.Usuario.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProducaoUsuarioDTO {

    private Long usuarioId;
    private String usuarioNome;
    private String username;
    private Long quantidadeFichas;
    private Long quantidadeSolicitacoes;

    // ✅ Campo calculado (Jasper reconhece como getter)
    public Long getTotalGeral() {
        long fichas = quantidadeFichas == null ? 0 : quantidadeFichas;
        long solicitacoes = quantidadeSolicitacoes == null ? 0 : quantidadeSolicitacoes;
        return fichas + solicitacoes;
    }
}
