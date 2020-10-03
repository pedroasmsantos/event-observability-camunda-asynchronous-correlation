package event.observability.camunda.controllers

import event.observability.camunda.entities.CorrelationEvent
import event.observability.camunda.services.CorrelationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UncorrelatedMessagesController (val correlationService: CorrelationService) {

    @GetMapping(path = ["uncorrelatedMessages"])
    fun getUncorrelatedMessages(): List<CorrelationEvent>{
        return correlationService.findAllUncorrelatedMessages()
    }

}