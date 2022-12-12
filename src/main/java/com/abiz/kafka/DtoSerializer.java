package com.abiz.kafka;

import com.abiz.controller.dto.FinancialDto;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class DtoSerializer extends JsonSerializer<FinancialDto> {
}
