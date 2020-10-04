package event.observability.camunda.listeners

import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.context.Context
import java.util.logging.Level
import java.util.logging.Logger
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.springframework.stereotype.Service

@Service
class UserValidationListener : TaskListener {
    override fun notify(delegateTask: DelegateTask) {
        val taskId = delegateTask.id
        val assignee = delegateTask.assignee
        var recipient = DEFAULT_RECIPIENT

        // Assign recipient address if a specific user is assigned to the task
        if(assignee != null){
            // Get User Profile from User Management
            val identityService = Context.getProcessEngineConfiguration().getIdentityService()
            val user = identityService.createUserQuery().userId(assignee).singleResult()

            if (user != null) {
                // Get Email Address from User Profile
                val userEmail = user.email;

                if (userEmail != null && !userEmail.isEmpty()) {
                    recipient = userEmail
                }
            }
        }

        val properties = System.getProperties()
        properties["mail.smtp.host"] = HOST
        properties["mail.smtp.port"] = PORT
        properties["mail.smtp.ssl.enable"] = "true"
        properties["mail.smtp.auth"] = "true"

        // Get the Session object and provide username and password authentication for sending the email
        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SENDER, SENDER_PASSWORD)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(SENDER))
            message.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
            message.subject = "Task assigned: " + delegateTask.name
            message.setText("You have a task pending validation. Please review the task assigned at $APP_USERTASKS_ENDPOINT?task=$taskId")

            Transport.send(message)

            LOGGER.info("Task Assignment Email successfully sent to the email $recipient.")
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Email could not be sent", e)
        }
    }

    companion object {
        private const val APP_USERTASKS_ENDPOINT = "http://localhost:8080/app/tasklist/default/#/"
        private const val HOST = "smtp.gmail.com"
        private const val PORT = "465"
        private const val SENDER = "eventobservability@gmail.com"
        private const val SENDER_PASSWORD = "EventObservability2020"
        private const val DEFAULT_RECIPIENT = "eventobservability@gmail.com"
        private val LOGGER = Logger.getLogger(UserValidationListener::class.java.name)
    }
}