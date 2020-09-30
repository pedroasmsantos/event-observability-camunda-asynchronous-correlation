package event.observability.camunda.executors

import com.google.gson.Gson
import com.google.gson.JsonObject
import event.observability.camunda.entities.Order
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.logging.Logger

@Service
class OutputValidationExecutor : JavaDelegate{
    override fun execute(execution: DelegateExecution?) {
        val orderId = execution!!.getVariable("orderId").toString()

        val client = HttpClient.newBuilder().build()

        //val token = getAuthenticationToken(client, orderId)
        val token = DEFAULT_TOKEN
        val isOrderValid = validateOrder(client, orderId, token)

        execution!!.setVariable("orderIsValid", isOrderValid)
    }

    fun getAuthenticationToken(client: HttpClient, orderId: String) : String{
        val parameters: MutableMap<Any, Any> = HashMap()
        parameters["client_id"] = CLIENT_ID
        parameters["client_secret"] = CLIENT_SECRET
        parameters["grant_type"] = GRANT_TYPE
        parameters["scopes"] = SCOPE

        val requestBody = buildFormUrlEncoded(parameters)
        val request = HttpRequest.newBuilder()
                .POST(requestBody)
            .uri(URI.create(AUTHORIZATION_ENDPOINT))
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        LOGGER.info("Performing the request ${request.uri()}")

        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join()

        val jsonObject = Gson().fromJson(response.body(), JsonObject::class.java)

        return jsonObject.get("access_token").asString
    }

    fun buildFormUrlEncoded(data: Map<Any, Any>): BodyPublisher? {
        val builder = StringBuilder()
        for ((key, value) in data) {
            if (builder.length > 0) {
                builder.append("&")
            }
            builder
                    .append(URLEncoder.encode(key.toString(), StandardCharsets.UTF_8))
            builder.append("=")
            builder
                    .append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8))
        }

        LOGGER.info("STR: ${builder.toString()}")
        return BodyPublishers.ofString(builder.toString())
    }

    fun validateOrder(client: HttpClient, orderId: String, accessToken: String) : Boolean{
        val request = HttpRequest.newBuilder()
                .uri(URI.create(ORDERING_API_ENDPOINT + orderId))
                .setHeader("Authorization", "Bearer $accessToken")
                .build()

        LOGGER.info("Performing the request ${request.uri()}")

        var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join()

        if(response.statusCode() == HttpStatus.OK.value()){
            val gson = Gson()
            val order = gson.fromJson(response.body().toString(), Order::class.java)
            LOGGER.info("Order Parsed: $order")

            if(order.status.equals("paid") &&
                    order.orderitems.size > 0 ||
                    order.total > 0){
                LOGGER.info("Order ${order.ordernumber} is valid!")
                return true
            }
        }

        LOGGER.info("Order ${orderId} is invalid!")
        return false
    }

    companion object {
        private const val ORDERING_API_ENDPOINT = "http://localhost:5102/ordering-api/api/v1/Orders/"
        private const val AUTHORIZATION_ENDPOINT = "http://localhost:5105/connect/token"
        private const val DEFAULT_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZCN0FDQzUyMDMwNUJGREI0RjcyNTJEQUVCMjE3N0NDMDkxRkFBRTEiLCJ0eXAiOiJhdCtqd3QiLCJ4NXQiOiJhM3JNVWdNRnY5dFBjbExhNnlGM3pBa2ZxdUUifQ.eyJuYmYiOjE2MDE0MzE4OTksImV4cCI6MTYwMTQzNTQ5OSwiaXNzIjoibnVsbCIsImF1ZCI6Im9yZGVycyIsImNsaWVudF9pZCI6IndvcmtmbG93ZW5naW5lIiwic2NvcGUiOlsib3JkZXJzIl19.jWH-2o7Is2-UCtL-7SUi1RLihLpvcvjazbfauvp2v87gjMEiwGhvBnydO2RvBI73s0HLQGsK9L0r7QindzwkOxm6aUNsMKOD2RxpAZLOB_YMlmYcLjkUKteD4MTP1glaE10zGMP4mLwtO7YBNywA-x4peAakh6g5hFfFKSG1kqLIiJHMOhRxhHj4pHritkBNZ0luqglxnnMLK_-ZhlDWUiyxl8aw3or0pFITnvax9ehd5LX39wYMLQoa9K0x3Ut7Zu5TVBKiU4CXwtwVxdsGcDy9wWfRHCmAs5DHkdXdHe5hnmpDzv5msCHO9f3SMij96BALFcZ2Vt0JyMLVEFlbZw"
        private const val GRANT_TYPE = "client_credentials"
        private const val CLIENT_ID = "workflowengine"
        private const val CLIENT_SECRET = "secret"
        private const val SCOPE = "orders"
        private val LOGGER = Logger.getLogger(OutputValidationExecutor::class.java.name)
    }
}