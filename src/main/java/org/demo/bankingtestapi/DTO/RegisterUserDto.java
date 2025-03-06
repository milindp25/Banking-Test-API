package org.demo.bankingtestapi.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterUserDto {

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email address.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    @NotBlank(message = "National ID is required.")
    private String nationalId;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotNull(message = "Date of birth is required.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull(message = "Zip code is required.")
    private Long zipCode;

    @NotNull(message = "Role is required")
    // Optional: if role is not provided, default to "USER"
    private String roleId;
}
