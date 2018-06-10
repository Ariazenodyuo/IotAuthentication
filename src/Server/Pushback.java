package Server;

import java.util.HashSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import Client.Client;
import Util.ClientConstant;
import Util.ServerConstant;
import Util.Utility;

public class Pushback implements MqttCallback {  
	
	
	private Client client;
	private String client_id;
	public Pushback(Client client, String client_id) {
		this.client = client;
		this.client_id = client_id;
	}
	  
    public void connectionLost(Throwable cause) {  
        System.out.println("连接断开，可以做重连");  
    }  
    

    // A -> B
    // B$A$jwt
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    	String mess = new String(message.getPayload());
    	this.client.authenticateA(this.client_id + ClientConstant.DEM + message);
//        DeviceManager.validate(mess.split(ServerConstant.DEM)[0], mess.split(ServerConstant.DEM)[1]);
    }

   
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
	}  
}
