package com.toannguyen.authify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRequest {
    @NotBlank(message = "Name should be not empty")
    String name;
    @Email(message = "Enter valid email address")
    @NotNull(message = "Email should be not empty")
    String email;
    @Size(min = 6, message = "Password should be atlest 6 characters")
    String password;
}
