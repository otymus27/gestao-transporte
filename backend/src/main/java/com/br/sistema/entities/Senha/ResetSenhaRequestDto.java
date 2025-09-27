package com.br.sistema.entities.Senha;

public record ResetSenhaRequestDto(
        String username,
        String senhaProvisoria,
        String novaSenha
) {
}