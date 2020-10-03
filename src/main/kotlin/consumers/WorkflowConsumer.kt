package event.observability.camunda.consumers

import event.observability.camunda.events.OrderEvent
import event.observability.camunda.services.CorrelationService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class WorkflowConsumer(val correlationService: CorrelationService){
    private val LOGGER = Logger.getLogger(WorkflowConsumer::class.java.name)

    @RabbitListener(queues = ["camundaqueue"])
    fun receivedMessage(event: OrderEvent, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) routingKey: String) {
        LOGGER.info("Received the message $event due to the binding $routingKey")
        correlationService.correlateMessage(event.OrderId, routingKey, event)
    }
}