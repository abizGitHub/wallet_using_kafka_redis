package com.abiz.kafka;

import com.abiz.controller.dto.FinancialDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Producer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${spring.kafka.consumer.topic}")
    private String topic;

    public void send(FinancialDto dto, String requestId) {
        kafkaTemplate.send(topic, requestId, dto);
    }

    public void send(List<FinancialDto> dtoList, String requestId) {
        dtoList.forEach(dto -> kafkaTemplate.send(topic, requestId, dto));
    }
}
