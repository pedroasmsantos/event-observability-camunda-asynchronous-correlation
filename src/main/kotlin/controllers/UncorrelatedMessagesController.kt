package event.observability.camunda.controllers

import event.observability.camunda.entities.CorrelationEvent
import event.observability.camunda.listeners.MessageCorrelationListener
import event.observability.camunda.services.CorrelationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
class UncorrelatedMessagesController (val correlationService: CorrelationService) {
    private val LOGGER = Logger.getLogger(UncorrelatedMessagesController::class.java.name)

    @GetMapping("api/uncorrelatedMessages")
    fun getUncorrelatedMessages(
        @RequestParam(required = false, defaultValue = "0") olderThan : Int
    ): List<CorrelationEvent>{
        if(olderThan > 0){
            LOGGER.info("Get uncorrelated messages older than $olderThan minutes")
            return correlationService.findOldestUncorrelatedMessages(olderThan)
        }else{
            LOGGER.info("Get all uncorrelated messages")
            return correlationService.findAllUncorrelatedMessages()
        }
    }

}