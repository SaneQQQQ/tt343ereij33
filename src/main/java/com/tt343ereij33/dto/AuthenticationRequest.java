package com.tt343ereij33.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(@NotBlank String username,
                                    @NotBlank String password) {}