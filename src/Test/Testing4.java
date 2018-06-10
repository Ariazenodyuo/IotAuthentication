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
 * 这个测试的是客户端存在id所对应的私钥，但是客户端不存在对应的公钥的情况
 * 1.将服务器公钥目录下的Server重命名为Server123
 * 2.运行程序
 * 3.将第一步的操作复原
 *
 */

public class Testing4 {

	
	static String username = "admin";
	static String password = "public";
	static MqttTopic topic1;
	static String clientId_1 = "Server";
	static String TOPIC_1 = "Testing_Topic";
	public static void main(String[] args) throws UnknownHostException, MqttException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
		Client client = new Client("tcp://127.0.0.1:11883", clientId_1);
		Client server = new Client("tcp://127.0.0.1:11883", "server");
		
		MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            server.setCallback(new Pushback());
            client.connect(options);
            server.connect(options);
            topic1 = server.getTopic(TOPIC_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        server.subscribe(TOPIC_1);
		
        MqttMessage message = new MqttMessage();
        message.setQos(2);
        message.setPayload(Utility.createJwtEs(clientId_1, "content", "This is the message").getBytes());
        
        topic1.publish(message);        
        client.disconnect();
        server.disconnect();
        client.close();
        server.close();
        

	}


}
