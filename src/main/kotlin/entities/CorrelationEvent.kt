package event.observability.camunda.entities

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.value.ObjectValue
import java.io.Serializable
import java.util.*

class CorrelationEvent : Serializable{
    private var id = UUID.randomUUID().toString()
    private var correlationId: String? = null
    private var messageName: String? = null
    private var payload: String? = null
    private var createdAt: Date? = null

    fun getCorrelationId(): String? {
        return correlationId
    }

    fun setCorrelationId(correlationId: String?): CorrelationEvent {
        this.correlationId = correlationId
        return this
    }

    fun getId(): String {
        return id
    }

    fun setId(id: String): CorrelationEvent {
        this.id = id
        return this
    }

    fun getMessageName(): String? {
        return messageName
    }

    fun setMessageName(messageName: String?): CorrelationEvent {
        this.messageName = messageName
        return this
    }

    fun setPayload(payload: String?): CorrelationEvent {
        this.payload = payload
        return this
    }

    fun getPayload(): String? {
        return payload
    }

    fun getCreatedAt(): Date? {
        return createdAt
    }

    fun setCreatedAt(created: Date?): CorrelationEvent {
        this.createdAt = created
        return this
    }

    fun createdNow(): CorrelationEvent {
        return setCreatedAt(Date())
    }

    fun asObjectValue(): ObjectValue? {
        return Variables.objectValue(this)
                .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                .create()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that: CorrelationEvent = o as CorrelationEvent
        return getId() == that.getId() && getCorrelationId() == that.getCorrelationId() && getMessageName() == that.getMessageName() &&
                getPayload() == that.getPayload() && getCreatedAt() == that.getCreatedAt()
    }

    override fun hashCode(): Int {
        return Objects.hash(getId(), getCorrelationId(), getMessageName(), getPayload(), getCreatedAt())
    }
}