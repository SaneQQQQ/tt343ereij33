package com.tt343ereij33.dto;

import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationRequest(@NotBlank String username,
                                  @NotBlank @Email String email,
                                  @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                                  message = "Password validation failed") String password) {
    public static UserEntity toUserEntity(RegistrationRequest registrationRequest) {
        return UserEntity.builder()
                .username(registrationRequest.username())
                .email(registrationRequest.email())
                .password(registrationRequest.password())
                .role(Role.ROLE_USER)
                .build();
    }
}