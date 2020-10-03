package event.observability.camunda.listeners

import event.observability.camunda.services.CorrelationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.springframework.stereotype.Service
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors

@Service
class MessageCorrelationListener(val correlationService: CorrelationService) : JavaDelegate{
    private val LOGGER = Logger.getLogger(MessageCorrelationListener::class.java.name)

    override fun execute(execution: DelegateExecution?) {
        LOGGER.info("MessageCorrelationListener")

        val messageNames = getEventNames(execution as ExecutionEntity)

        if(messageNames != null && messageNames.any()) {
            LOGGER.info("Will attempt to correlate...")
            for(name in messageNames){
                val correlationId = correlationService.canCorrelateUsingPreviousUncorrelatedMessage(execution.getProcessBusinessKey(), name)
                if(correlationId.isPresent){
                    execution.setVariableLocal("UncorrelatedMessagesConsumedEvent", correlationId.get());
                    return
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