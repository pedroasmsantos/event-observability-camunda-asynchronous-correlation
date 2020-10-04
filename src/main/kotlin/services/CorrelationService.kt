package event.observability.camunda.services

import com.google.gson.Gson
import event.observability.camunda.entities.CorrelationEvent
import event.observability.camunda.events.OrderEvent
import event.observability.camunda.events.OrderStockItem
import event.observability.camunda.repositories.UncorrelatedMessagesRepository
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.logging.Logger

@Service
class CorrelationService(val runtimeService: RuntimeService,
                         val repository: UncorrelatedMessagesRepository,
                         val scheduler: TaskScheduler){
    private val LOGGER = Logger.getLogger(CorrelationService::class.java.name)

    fun canCorrelateUsingPreviousUncorrelatedMessage(correlationId: String?, messageName: String?) : Optional<String>{
        LOGGER.info("Attempting to correlate....")
        val uncorrelatedMessage = repository.findUncorrelatedMessage(correlationId, messageName)

        if(uncorrelatedMessage != null){
            LOGGER.info("Scheduling message correlation....")
            scheduler.schedule(
                {
                    LOGGER.info("Uncorrelated Message found... Will attempt to apply correlation...")
                    applyOrderCorrelation(uncorrelatedMessage.getMessageName(), uncorrelatedMessage.getCorrelationId(),
                            uncorrelatedMessage.getCorrelationId(), uncorrelatedMessage.getPayload(), arrayOf<OrderStockItem>())


                },
                Instant.now().plusSeconds(2))

            return Optional.of(uncorrelatedMessage.getId())
        }

        return Optional.empty()
    }

    fun correlateMessage(correlationId: String,
                         messageName: String,
                         event: OrderEvent) {
        LOGGER.info("CorrelationService#consumeMessage: Attempt to correlate message correlationId $correlationId - messageName $messageName. ")

        val result = applyOrderCorrelation(messageName, correlationId, event.id, event.status, event.products)

        if (result != null && result.any()){
            LOGGER.info("Event $event correlated successfully.")
        }else{
            LOGGER.info("Event $event couldn't be correlated.")
            val payload = Gson().toJson(event)
            val correlationEvent = CorrelationEvent()
                    .setMessageName(messageName)
                    .setCorrelationId(correlationId)
                    .setPayload(payload)
                    .createdNow()

            repository.insertUncorrelatedMessage(correlationEvent)
        }
    }

    fun deleteUncorrelatedMessage(messageId: String?) {
        LOGGER.info("Attempting to remove uncorrelated message record because message was already correlated")
        repository.deleteUncorrelatedMessage(messageId)
    }

    fun findAllUncorrelatedMessages(): List<CorrelationEvent>{
        LOGGER.info("Getting all uncorrelated messages")
        return repository.findAllUncorrelatedMessages()
    }

    fun findOldestUncorrelatedMessages(olderThanInMinutes: Int): List<CorrelationEvent>{
        LOGGER.info("Getting oldest uncorrelated messages")
        return repository.findOldestUncorrelatedMessages(olderThanInMinutes)
    }

    fun disableAwaitingUserOnUncorrelatedMessage(messageId: String?){
        repository.updateAwaitingUserOnUncorrelatedMessage(false, messageId)
    }

    fun enableAwaitingUserOnUncorrelatedMessage(messageId: String){
        repository.updateAwaitingUserOnUncorrelatedMessage(true, messageId)
    }

    fun applyGenericCorrelation(messageName: String?, correlationId: String?,
                                payload: String? = "No payload provided") : List<MessageCorrelationResult> {
        return runtimeService.createMessageCorrelation(messageName)
                .processInstanceBusinessKey(correlationId)
                .setVariable("payload", payload)
                .correlateAllWithResult()
    }

    private fun applyOrderCorrelation(messageName: String?, correlationId: String?,
                                 orderId: String?, orderStatus: String?, orderItems: Array<OrderStockItem>?) : List<MessageCorrelationResult> {
        return runtimeService.createMessageCorrelation(messageName)
                .processInstanceBusinessKey(correlationId)
                .setVariable("orderId", orderId)
                .setVariable("orderStatus", orderStatus)
                .setVariable("orderItems", orderItems)
                .correlateAllWithResult()
    }
}