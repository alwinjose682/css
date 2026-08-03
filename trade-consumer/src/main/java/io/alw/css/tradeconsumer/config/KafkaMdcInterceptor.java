package io.alw.css.tradeconsumer.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

@FunctionalInterface
interface KafkaMdcInterceptor<K, V> extends RecordInterceptor<K, V> {

    String recordId(V value);

    @Override
    default ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        int partition = record.partition();
        MDC.put("kafkaPartition", String.valueOf(partition));
        MDC.put("id", recordId(record.value()));

        return record;
    }

    @Override
    default void afterRecord(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        MDC.clear();
    }
}
