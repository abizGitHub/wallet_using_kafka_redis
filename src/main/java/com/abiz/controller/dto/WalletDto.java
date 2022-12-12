package com.abiz.controller.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WalletDto {

    @NotEmpty
    private String user;

    @PositiveOrZero
    @NotNull
    private BigDecimal credit;
}
