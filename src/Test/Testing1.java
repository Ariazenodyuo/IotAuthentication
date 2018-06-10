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
 * 这个测试的是客户端和服务器密钥不匹配时的情况
 * 1.先在客户端私钥文件夹下（key）建立新的私钥以及对应的pkcs8文件，使用的命令为：
 * 	 ecparam -genkey -name prime256v1 -noout -out ec_corrupt_private.pem
 * 	 pkcs8 -topk8 -inform PEM -outform DER -in corrupt_private.pem -nocrypt -out ec_corrupt_private_pkcs8
 *  以上两条命令中的ec_corrupt为客户端id，可以使用其他的，只要保证在服务器公钥目录下也又对应文件名的公钥即可
 * 2.在服务器公钥文件夹下（keypair）建立符合格式的公钥，可以直接复制已存在的公钥，并以第一步中的客户端id重命名
 * 3.运行testing1
 *
 */

public class Testing1 {

	
	static String username = "admin";
	static String password = "public";
	static MqttTopic topic1;
	static String clientId_1 = "corrupt";
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
            topic1 = client.getTopic(TOPIC_1);
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
