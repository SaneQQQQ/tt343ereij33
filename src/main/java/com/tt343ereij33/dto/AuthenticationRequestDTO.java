package com.tt343ereij33.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDTO(@NotBlank String username,
                                       @NotBlank String password) {}