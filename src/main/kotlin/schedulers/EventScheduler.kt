package event.observability.camunda.schedulers

import event.observability.camunda.services.CorrelationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class EventScheduler(val correlationService: CorrelationService){

    @Scheduled(cron = "0 0/$olderThanInMinutes * 1/1 * ?")
    fun triggerProcessForOldestUncorrelatedMessages(){
        LOGGER.info("Scheduler activated... Will try to fetch uncorrelated messages older than $olderThanInMinutes minutes and trigger a BPMN instance.")
        val messages = correlationService.findOldestUncorrelatedMessages(olderThanInMinutes)
        for(message in messages) {
            if (!message.getAwaitingUser()){
                correlationService.applyGenericCorrelation(uncorrelatedMessageName, message.getId(), message.getPayload())
                correlationService.enableAwaitingUserOnUncorrelatedMessage(message.getId())
            }
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger(EventScheduler::class.java.name)
        private const val uncorrelatedMessageName = "UncorrelatedMessage"
        private const val olderThanInMinutes = 30
    }

}