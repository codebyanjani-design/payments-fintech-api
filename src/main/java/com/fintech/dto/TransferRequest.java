package com.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    private Long targetWalletId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotBlank
    private String referenceId;
}
