package com.abiz.domain;

import com.abiz.config.AppConstants;
import com.abiz.domain.enumeration.AccountingStatus;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", doNotUseGetters = true, callSuper = false)
@Entity
@Table(name = AppConstants.TABLE_PREFIX + "address")
public class FinancialEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_status")
    private AccountingStatus status;

    @Column(name = "user")
    private String user;

    @Column(precision = 10, scale = 2, name = "amount")
    private BigDecimal amount;

}
