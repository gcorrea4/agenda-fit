package br.com.fiap.agendafit.dto;

// expiresIn em segundos
public record TokenResponse(String token, long expiresIn) {}
