package com.br.sistema.exceptions;

public class UsuarioInativoException extends RuntimeException{

    public UsuarioInativoException(String mensagem){
        super(mensagem);
    }
}
