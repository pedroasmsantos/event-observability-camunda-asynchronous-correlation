package event.observability.camunda.executors

import event.observability.camunda.services.CorrelationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class DisableAwaitingUserOnUncorrelatedMessageExecutor(val correlationService: CorrelationService) : JavaDelegate{
    private val LOGGER = Logger.getLogger(DisableAwaitingUserOnUncorrelatedMessageExecutor::class.java.name)

    override fun execute(execution: DelegateExecution?) {
        val uncorrelatedMessageId = execution?.processBusinessKey

        LOGGER.info("Attempting to disable awaiting for user validation flag of uncorrelated message with id $uncorrelatedMessageId")
        correlationService.disableAwaitingUserOnUncorrelatedMessage(uncorrelatedMessageId)
    }
}