package com.abiz.kafka;

import com.abiz.controller.dto.FinancialDto;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class DtoDeserializer extends JsonDeserializer<FinancialDto> {
}
