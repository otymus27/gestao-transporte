package com.br.sistema.entities.Login;


// ✅ Novo campo: senhaProvisoria
public record LoginResponse(String accessToken, Long expiresIn, boolean senhaProvisoria) {
}
