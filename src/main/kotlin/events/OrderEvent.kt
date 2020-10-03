package event.observability.camunda.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class OrderEvent @JsonCreator constructor(
    @JsonProperty("OrderId") val id: String,
    @JsonProperty("OrderStatus") val status: String?,
    @JsonProperty("OrderItems") val products: Array<OrderStockItem>?
)