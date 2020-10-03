package event.observability.camunda.configurations

import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaConfig {
    @Bean
    fun processEngineAuthenticationFilter(): FilterRegistrationBean<ProcessEngineAuthenticationFilter>
    {
        val registrationBean = FilterRegistrationBean(ProcessEngineAuthenticationFilter())
        registrationBean.addUrlPatterns("/rest/*")
        registrationBean.addInitParameter("authentication-provider", "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider")
        registrationBean.setName("camunda-auth")
        registrationBean.order = 1
        return registrationBean
    }
}