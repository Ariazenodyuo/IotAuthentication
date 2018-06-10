package Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.jose4j.jwt.MalformedClaimException;

import Server.Pushback;
import Util.ClientConstant;
import Util.Utility;
public class Client extends MqttClient {

	String clientId = null;
	public Client(String serverURI, String clientId) throws MqttException, UnknownHostException, IOException, InterruptedException {
		
		super(serverURI, clientId);
		this.clientId = clientId;
		this.setCallback(new Pushback(this,clientId));
		File file = new File(ClientConstant.KEY_PATH+"/ec_"+clientId+"_private_pkcs8");
		if(!file.exists())
			register(clientId);
		
	}
	
	
	public void register(String clientId) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = null;
		socket = new Socket(ClientConstant.DEVICEMANAGER_HOST, ClientConstant.DEVICEMANAGER_PORT);
		DataInputStream dis = null;
		PrintStream out = new PrintStream(socket.getOutputStream());
		DataOutputStream fileOut = null;
		out.println(clientId);
		
		dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		Thread.sleep(2000);
		//read filename
		String savePath = ClientConstant.KEY_PATH+"/"+dis.readUTF();
		int bufferSize = 1024;   
        // buffer area   
        byte[] buf = new byte[bufferSize];   
        int passedlen = 0;   
        long len = 0;   
		try{  
	        fileOut = new DataOutputStream(   
	            new BufferedOutputStream(new BufferedOutputStream(   
	                new FileOutputStream(savePath))));   
	        len = dis.readLong();   
	        System.out.println("Length of the file is:" + len + "KB");   
	        System.out.println("Transfer starts");   
	        while (true) {   
	            int read = 0;   
	            if (dis!= null) {   
	              read = dis.read(buf);   
	            }   
	            passedlen += read;   
	            if (read == -1) {   
	              break;   
	            }   
	            System.out.println("Progress:" + (passedlen * 100 / len) + "%");   
	            fileOut.write(buf, 0, read);   
	          }   
	          System.out.println("Complete! File is saved as " + savePath);  
	          fileOut.close();
	          convertToPKCS8(clientId);
	        } catch (Exception e) {   
	          e.printStackTrace();   
	        }  finally {
				dis.close();
				socket.close();
			}
		return;
		
	}
	
	private int convertToPKCS8(String clientId) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		String cmd = ClientConstant.OPENSSL_PATH + " pkcs8 -topk8 -inform PEM -outform DER -in " + ClientConstant.KEY_PATH + "/ec_"+clientId+"_private.pem -nocrypt -out " + ClientConstant.KEY_PATH + "/ec_"+clientId+"_private_pkcs8";
		Process process;
		process = runtime.exec(cmd);
		if(process.waitFor() == 1) 
			throw new Exception(getErrorMessage(process));
		return 1;
	}
	
	private static String getErrorMessage(Process process) {  
        String errMeaage = null;  
        try {  
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));  
            String line = null;  
            StringBuilder sb = new StringBuilder();  
            while ((line = br.readLine()) != null) {  
                sb.append(line + "\n");  
            }  
            errMeaage = sb.toString();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return errMeaage;  
    }  
	
	@Override
	public void connect() throws MqttSecurityException, MqttException {
		super.connect();
	}
	 
	
	public void authenticateA(String message) throws UnknownHostException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, MalformedClaimException, CertificateException {
		
		String ClientId = message.split(ClientConstant.DEM)[0];
		Socket socket = new Socket(ClientConstant.DEVICEMANAGER_HOST, ClientConstant.AUTHENTICATION_PORT);
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println(message);
		String content = input.readLine();
		input.close();
		out.close();
		socket.close();
		
		if("Public key doesn't exist".equals(content)){
			System.out.println(content);
		} else {
			Utility.validate(ClientId, content);
		}
		
		
		
	}
	

}
