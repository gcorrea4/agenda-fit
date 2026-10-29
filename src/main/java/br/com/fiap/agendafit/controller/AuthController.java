package br.com.fiap.agendafit.controller;

import br.com.fiap.agendafit.dto.ErrorResponse;
import br.com.fiap.agendafit.dto.LoginRequest;
import br.com.fiap.agendafit.dto.RegisterRequest;
import br.com.fiap.agendafit.dto.TokenResponse;
import br.com.fiap.agendafit.dto.UserResponse;
import br.com.fiap.agendafit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro de alunos e login com JWT")
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um aluno (role USER), com a senha gravada em BCrypt")
    @ApiResponse(responseCode = "201", description = "Usuário criado (sem a senha)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return UserResponse.of(service.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Confere e-mail e senha e devolve o token JWT (válido por 1 hora)")
    @ApiResponse(responseCode = "200", description = "Token e tempo de expiração em segundos")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }
}
