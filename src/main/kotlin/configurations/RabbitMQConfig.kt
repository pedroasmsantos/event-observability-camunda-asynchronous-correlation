package event.observability.camunda.configurations

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory

@Configuration
class RabbitMQConfig{
    val exchangeName = "eshop_event_bus"
    val queueName = "camundaqueue"

    @Bean
    fun bindings(): Declarables? {
        return Declarables(
            DirectExchange(exchangeName, false, false),
            Queue(queueName, true),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStartedIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStatusChangedToAwaitingValidationIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStatusChangedToCancelledIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStockConfirmedIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStockRejectedIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStatusChangedToStockConfirmedIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderPaymentSucceededIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderPaymentFailedIntegrationEvent", null),
            Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "OrderStatusChangedToPaidIntegrationEvent", null))
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory?): RabbitTemplate? {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = producerJackson2MessageConverter()
        return rabbitTemplate
    }

    @Bean
    fun producerJackson2MessageConverter(): Jackson2JsonMessageConverter? {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun myHandlerMethodFactory(): DefaultMessageHandlerMethodFactory? {
        val factory = DefaultMessageHandlerMethodFactory()
        factory.setMessageConverter(MappingJackson2MessageConverter())
        return factory
    }

    @Override
    fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        registrar.messageHandlerMethodFactory = myHandlerMethodFactory()
    }
}