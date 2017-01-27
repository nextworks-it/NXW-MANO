package appCatalogueDummyReceiver;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class dummyRabbitConsumer {

	private static final String EXCHANGE_NAME = "selfnet-app-onboarding";

	public static void main(String[] argv) throws Exception {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		//declare topic exchange (durable false and autodelete true)
		Map<String,Object> args = new HashMap<>();
		
		channel.exchangeDeclare(EXCHANGE_NAME, "topic", false, true, args);
		String queueName = channel.queueDeclare().getQueue();

		//bind to routing keys
		//channel.queueBind(queueName, EXCHANGE_NAME, "*.*");
		//channel.queueBind(queueName, EXCHANGE_NAME, "SENSOR.*");
		//channel.queueBind(queueName, EXCHANGE_NAME, "ACTUATOR.VNF");
		channel.queueBind(queueName, EXCHANGE_NAME, "*.SDN_APP");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}