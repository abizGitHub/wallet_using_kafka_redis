package com.abiz.domain;

import com.abiz.config.AppConstants;
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
public class WalletEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 50)
    private String id;

    @Column(name = "user")
    private String user;

    @Column(precision = 10, scale = 2, name = "credit")
    private BigDecimal credit;

}
