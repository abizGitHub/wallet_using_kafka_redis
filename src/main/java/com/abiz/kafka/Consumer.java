package com.abiz.kafka;

import com.abiz.controller.dto.FinancialDto;
import com.abiz.service.IWalletConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Consumer {

    private final IWalletConsumer walletConsumer;

    public Consumer(IWalletConsumer walletConsumer) {
        this.walletConsumer = walletConsumer;
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "${spring.kafka.consumer.topic}",
            partitions = "#{'${spring.kafka.consumer.partitions}'}"))
    public void listen(ConsumerRecord<String, FinancialDto> record) {
        Optional<FinancialDto> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            FinancialDto dto = kafkaMessage.get();
            System.out.println("received:" + dto);
            walletConsumer.apply(record.key(), dto);
        }
    }
}
