package event.observability.camunda.entities

data class Order(
    val ordernumber: String,
    val status: String,
    val orderitems: Array<Any>,
    val total: Double
)