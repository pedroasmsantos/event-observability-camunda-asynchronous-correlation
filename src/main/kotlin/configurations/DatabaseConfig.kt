package event.observability.camunda.configurations

import event.observability.camunda.repositories.UncorrelatedMessagesRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.core.JdbcTemplate
import java.util.logging.Logger
import javax.annotation.PostConstruct

@Configuration
class DatabaseConfig(val jdbcTemplate: JdbcTemplate) {
    private val LOGGER = Logger.getLogger(DatabaseConfig::class.java.name)

    @Bean
    fun getUncorrelatedMessagesRepository(): UncorrelatedMessagesRepository {
        return UncorrelatedMessagesRepository(jdbcTemplate)
    }

    @PostConstruct
    fun initializeDatabase()
    {
        val repository = getUncorrelatedMessagesRepository()
        try {
            repository.checkIfTableExists()
        } catch (e: BadSqlGrammarException) {
            LOGGER.info("Unable to find table... Will attempt to create it...")
            repository.createUncorrelatedMessagesTable()
        }
    }

}