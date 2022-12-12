package com.abiz.controller.mapper;


import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.model.FinancialModel;
import com.abiz.domain.FinancialEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface FinancialMapper {

    FinancialEntity toEntity(FinancialModel model);

    FinancialModel toModel(FinancialEntity entity);

    FinancialModel toModel(FinancialDto dto);

    List<FinancialDto> toDto(List<FinancialEntity> entities);

    FinancialDto toDto(FinancialEntity entity);

}
