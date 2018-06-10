package Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.jose4j.lang.JoseException;

import Client.Client;
import Server.Pushback;
import Util.Utility;

public class Server {
	
	public static final String HOST = "tcp://127.0.0.1:11883";
	public static final String TOPIC1 = "TOPIC1";
	public static final String TOPIC2 = "TOPIC2";
	public static final String clientId = "Server";
	
	
	public MqttMessage message;
	public Client client;
	public MqttTopic topic1;
	public MqttTopic topic2;
	public String username = "admin";
	public String password = "public";
	
	public Server() throws MqttSecurityException, MqttException, UnknownHostException, IOException, InterruptedException {
		client = new Client(HOST, clientId);
		connect();
	}
	
	public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            client.setCallback(new Pushback());
            client.connect(options);
            topic1 = client.getTopic(TOPIC1);
            topic2 = client.getTopic(TOPIC2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(MqttTopic topic , MqttMessage message) throws MqttPersistenceException,
            MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
        System.out.println("message is published completely! "
                + token.isComplete());
    }
    

    public static void main(String[] args) throws MqttException, UnknownHostException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException, InterruptedException {
        Server server = new Server();
        server.message = new MqttMessage();
        server.message.setQos(2);
        server.message.setRetained(true);
        server.message.setPayload(Utility.createJwtEs(clientId, "content", "Sendting to 124").getBytes());
//        server.message.setPayload("Sending to 124".getBytes());
        server.client.subscribe(Server.TOPIC1);
        server.publish(server.topic1 , server.message);
        
        
        server.message = new MqttMessage();
        server.message.setQos(2);
        server.message.setRetained(true);
        server.message.setPayload(Utility.createJwtEs(clientId, "content", "Sendting to 125").getBytes());
//        server.message.setPayload("Sending to 125".getBytes());
        server.client.subscribe(Server.TOPIC2);
        server.publish(server.topic2 , server.message);

        System.out.println(server.message.isRetained() + "------ratained state");
//        server.client.close();
    }
}
