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
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.jose4j.lang.JoseException;

import Client.Client;
import Server.Pushback;
import Util.Utility;


/**
 * 
 * This test is to record the time needed for NUMBER new devices to register and authenticate
 * 
 * Run the program twice because 90% of the time will be consumed on the I/O operation for the first time
 * 
 */

public class Testing6 {

	static final int NUMBER = 100;
	static String username = "admin";
	static String password = "public";
	static MqttTopic topic1;
	static String TOPIC_1 = "Testing_Topic";
	static String client_id = "Server";
	public static void exe(String clientid, MqttConnectOptions options) {
		new Runnable() {
			public void run() {
				try {
					Client client = new Client("tcp://127.0.0.1:11883", clientid);
					client.connect(options);
					MqttMessage message = new MqttMessage();
		        	message.setQos(2);
		            message.setPayload(Utility.createJwtEs(client_id, "content", "This is the message from"+clientid).getBytes());
		            topic1.publish(message);
		            client.disconnect();
		            client.close();
					
				} catch (MqttException | IOException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException | JoseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.run();
	}
	public static void main(String[] args) throws UnknownHostException, MqttException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
		Client server = new Client("tcp://127.0.0.1:11883", "server");
		
		MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            server.setCallback(new Pushback(server, client_id));
            server.connect(options);
            topic1 = server.getTopic(TOPIC_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        server.subscribe(TOPIC_1);
        
        long start = System.currentTimeMillis();
		for(int i = 0; i < 100; i++) {
			exe("client__"+i, options);
		}
		System.out.println("Overall time:"+((System.currentTimeMillis() - start) / 1000.0) + "s");
        
        server.disconnect();
        server.close();
        

	}
	

}
