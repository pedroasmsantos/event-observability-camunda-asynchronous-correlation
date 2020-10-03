package event.observability.camunda.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class OrderStockItem @JsonCreator constructor(
        @JsonProperty("ProductId") val productId: Int,
        @JsonProperty("ProductName") val productName: String,
        @JsonProperty("UnitPrice") val unitPrice: Float,
        @JsonProperty("Units") val quantity: Int
)