package org.demo.bankingtestapi.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DepositDto {

    @NotBlank(message = "Account number is required.")
    private String accountNumber;

    @NotNull(message = "Deposit amount is required.")
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero.")
    private BigDecimal amount;
}