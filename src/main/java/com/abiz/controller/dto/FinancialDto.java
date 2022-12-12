package com.abiz.controller.dto;

import com.abiz.domain.enumeration.AccountingStatus;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FinancialDto implements Serializable {

    private String id;

    @NotNull
    private AccountingStatus status;

    @NotEmpty
    private String user;

    @PositiveOrZero
    @NotNull
    private BigDecimal amount;
}
