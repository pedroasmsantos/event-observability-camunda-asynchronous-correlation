package event.observability.camunda.listeners

import event.observability.camunda.configurations.DatabaseConfig
import event.observability.camunda.services.CorrelationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.springframework.stereotype.Service
import java.util.logging.Logger
import java.util.stream.Collectors

@Service
class MessageDeletionListener(val correlationService: CorrelationService) : JavaDelegate {
    private val LOGGER = Logger.getLogger(MessageDeletionListener::class.java.name)

    override fun execute(execution: DelegateExecution?) {
        LOGGER.info("MessageDeletionListener")

        val consumedEvent = execution?.getVariableLocal("UncorrelatedMessagesConsumedEvent")
        if(consumedEvent != null){
            val uncorrelatedMessageId = consumedEvent as String
            correlationService.removeUncorrelatedMessage(uncorrelatedMessageId)
        }else{
            val messageNames = getEventNames(execution as ExecutionEntity)

            if(messageNames != null && messageNames.any()){
                LOGGER.info("Will attempt to remove...")
                for(name in messageNames){
                    LOGGER.info("Going to remove message $name with correlaton id ${execution.processBusinessKey}")
                    correlationService.removeUncorrelatedMessage(execution.getProcessBusinessKey(), name)
                }
            }
        }
    }

    private fun getEventNames(execution: ExecutionEntity): List<String>{
        return execution.eventSubscriptions.stream()
                .filter { ev -> ev.isSubscriptionForEventType(EventType.MESSAGE) }
                .map(EventSubscriptionEntity::getEventName)
                .collect(Collectors.toList())
    }
}