package event.observability.camunda.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import event.observability.camunda.entities.CorrelationEvent
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.Timestamp

class UncorrelatedMessagesRepository(val jdbcTemplate: JdbcTemplate) {
    private val tableName = "UNCORRELATED_MESSAGES"

    fun checkIfTableExists() {
        jdbcTemplate.execute(getValidateUncorrelatedMessagesQuery())
    }

    fun findUncorrelatedMessage(correlationId: String?, messageName: String?): CorrelationEvent? {
        try {
            val queryParameters = arrayOf(correlationId, messageName)
            return jdbcTemplate.queryForObject(getFindUncorrelatedMessageQuery(), queryParameters, extractData())
        } catch (e: EmptyResultDataAccessException) {
            return null;
        }
    }

    fun createUncorrelatedMessagesTable() {
        jdbcTemplate.execute(getCreateUncorrelatedMessagesTableQuery())
    }

    fun insertUncorrelatedMessage(event: CorrelationEvent): Int {
        return jdbcTemplate.update(getInsertUncorrelatedMessageQuery(),
                event.getId(),
                event.getCorrelationId(),
                event.getMessageName(),
                Timestamp.from(event.getCreatedAt()?.toInstant()),
                ObjectMapper().writeValueAsString(event.getPayload()))
    }

    fun deleteUncorrelatedMessage(correlationId: String?, messageName: String?){
        jdbcTemplate.update(getDeleteUncorrelatedMessagesByCorrelationIdAndMessageNameStatement(), correlationId, messageName)
    }

    fun deleteUncorrelatedMessage(messageId: String?){
        jdbcTemplate.update(getDeleteUncorrelatedMessagesByIdStatement(), messageId)
    }

    private fun getValidateUncorrelatedMessagesQuery(): String {
        return "SELECT 1 FROM $tableName"
    }

    private fun getFindUncorrelatedMessageQuery(): String {
        return "SELECT id, correlationId, messageName, createdAt, payload " +
                "FROM $tableName " +
                "WHERE correlationId=? AND messageName=? " +
                "ORDER BY createdAt ASC"
    }


    private fun getCreateUncorrelatedMessagesTableQuery(): String {
        return "CREATE TABLE $tableName ( " +
                "id VARCHAR(36), " +
                "correlationId VARCHAR(255), " +
                "messageName VARCHAR(100), " +
                "createdAt TIMESTAMP, " +
                "payload VARCHAR(2500) " +
                ")"
    }

    private fun getInsertUncorrelatedMessageQuery() : String{
        return "INSERT INTO $tableName " +
                "(id, correlationId, messageName, createdAt, payload) " +
                "VALUES(?, ?, ?, ?, ?)"
    }

    private fun getDeleteUncorrelatedMessagesByCorrelationIdAndMessageNameStatement(): String{
        return "DELETE FROM $tableName WHERE correlationId=? AND messageName=?"
    }

    private fun getDeleteUncorrelatedMessagesByIdStatement(): String{
        return "DELETE FROM $tableName WHERE id=?"
    }

    private fun extractData(): RowMapper<CorrelationEvent>{
        return RowMapper{ resultSet: ResultSet, rowIndex: Int ->
                CorrelationEvent()
                        .setId(resultSet.getString("id"))
                        .setCorrelationId(resultSet.getString("correlationId"))
                        .setMessageName(resultSet.getString("messageName"))
                        .setCreatedAt(resultSet.getTimestamp("createdAt"))
                        .setPayload(resultSet.getString("payload"))

        }
    }

}