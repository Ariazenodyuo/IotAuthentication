package Server;

import java.util.HashSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import Util.ServerConstant;

public class Pushback implements MqttCallback {  
	
	
	public Pushback() {
	}
	  
    public void connectionLost(Throwable cause) {  
        System.out.println("连接断开，可以做重连");  
    }  
    

    public void messageArrived(String topic, MqttMessage message) throws Exception {
    	String mess = new String(message.getPayload());
        DeviceManager.validate(mess.split(ServerConstant.DEM)[0], mess.split(ServerConstant.DEM)[1]);
        System.out.println(mess);
    }

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
	}  
}
