package eu.selfnet5g.onboarding;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBusConfig {

	@Value("${appcatalogue.rabbit.exchange:selfnet-exchange}")
	private String exchangeName;
	
	@Bean
	TopicExchange exchange() {
		return new TopicExchange(exchangeName, false, true);
	}		
}
