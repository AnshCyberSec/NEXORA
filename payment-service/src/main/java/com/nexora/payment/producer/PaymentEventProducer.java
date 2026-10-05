package com.nexora.payment.producer;

import com.nexora.payment.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String , PaymentEvent> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    private static final String LOG_PREFIX = "[PAYMENT-PRODUCER]";

    public void publishPaymentEvent(PaymentEvent event){
        log.info("{} Publishing payment event: paymentId={}, status={}",
                LOG_PREFIX, event.getPaymentId(), event.getStatus());

        try{
            kafkaTemplate.send(TOPIC, event.getPaymentId(),event);
            log.info("{} Payment event published successfully: {}", LOG_PREFIX, event.getPaymentId());
        }catch (Exception e){
            log.error("{} Failed to publish payment event: {}",LOG_PREFIX, event.getPaymentId(),e);
        }
    }
}
