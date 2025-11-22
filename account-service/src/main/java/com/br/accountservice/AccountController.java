package com.br.accountservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class AccountController {

    @Value("${message}")
    private String message;

    @GetMapping("/message")
    public String getMessage() {
        return "Configuração recebida: " + message;
    }

    @Value("${database.password}")
    private String dbPassword;

    @GetMapping("/db-password")
    public String getDbPassword() {
        // O valor retornado deve ser a senha EM TEXTO SIMPLES
        return "Status do Ambiente: PROD | Senha Decriptografada: " + dbPassword;
    }

}