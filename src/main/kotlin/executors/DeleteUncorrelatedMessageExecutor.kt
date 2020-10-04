package event.observability.camunda.executors

import event.observability.camunda.services.CorrelationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class DeleteUncorrelatedMessageExecutor(val correlationService: CorrelationService) : JavaDelegate{
    private val LOGGER = Logger.getLogger(DeleteUncorrelatedMessageExecutor::class.java.name)

    override fun execute(execution: DelegateExecution?) {
        val uncorrelatedMessageId = execution?.processBusinessKey

        LOGGER.info("Attempting to delete uncorrelated message with id $uncorrelatedMessageId")
        correlationService.deleteUncorrelatedMessage(uncorrelatedMessageId)
    }
}