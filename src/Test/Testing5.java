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
 * 这个测试的是10个用户能否注册认证成功
 * 1. 运行程序
 *
 */
    class Testing5 {

	
	static String username = "admin";
	static String password = "public";
	static MqttTopic topic1;
	static String TOPIC_1 = "Testing_Topic";
	static Client[] client_list = new Client[10]; 
	public static void main(String[] args) throws UnknownHostException, MqttException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
		for(int i = 0; i < 10; i++)
			client_list[i] = new Client("tcp://127.0.0.1:11883", "client"+i);
		Client server = new Client("tcp://127.0.0.1:11883", "server");
		
		MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            server.setCallback(new Pushback());
            for(int i = 0; i < 10; i++)
            	client_list[i].connect(options);
            server.connect(options);
            topic1 = server.getTopic(TOPIC_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        server.subscribe(TOPIC_1);
		
        MqttMessage[] message = new MqttMessage[10];
        for(int i = 0; i < 10; i++) {
        	message[i] = new MqttMessage();
        	message[i].setQos(2);
            message[i].setPayload(Utility.createJwtEs("client"+i, "content", "This is the message from client"+i).getBytes());
            topic1.publish(message[i]);
            client_list[i].disconnect();
            client_list[i].close();
        }
        
        
        server.disconnect();
        server.close();
        

	}

}
