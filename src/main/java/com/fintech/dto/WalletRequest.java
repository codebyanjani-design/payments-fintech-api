package com.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletRequest {
    @NotBlank
    private String userId;
    
    @PositiveOrZero
    private BigDecimal initialBalance;
}
