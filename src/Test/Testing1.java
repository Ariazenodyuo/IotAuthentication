package Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import Client.Client;
import Server.Pushback;
import Util.ServerConstant;
import Util.Utility;


/**
 * 
 *  This test is for the situation that public key of the device which is stored at the server is corrupted
 *
 *	1.Create a new private key and corresponding pkcs8 file under the correct directory(key), use the commands below:
 *	ecparam -genkey -name prime256v1 -noout -out ec_<client id>_private.pem
 * 	 pkcs8 -topk8 -inform PEM -outform DER -in ec_<client_id>_private.pem -nocrypt -out ec_<client_id>_private_pkcs8
 *  2.Copy a existing public key and rename it using the same clint_id as the first step
 *  3.Run the program
 * 
 */

public class Testing1 {

	
	static String username = "admin";
	static String password = "public";
	static MqttTopic topic1;
	static String clientId_1 = "corrupt";
	static String TOPIC_1 = "Testing_Topic";
	public static void main(String[] args) throws UnknownHostException, MqttException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Client client = new Client("tcp://127.0.0.1:11883", "aaa");
		Client server = new Client("tcp://127.0.0.1:11883", clientId_1);
		
		MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            server.setCallback(new Pushback(server,clientId_1));
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
