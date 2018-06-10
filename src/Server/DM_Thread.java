package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class DM_Thread implements Runnable {
	
	
	private Socket client = null;
	
	public DM_Thread(Socket socket) {
		this.client = socket;
	}
	@Override
	public void run() {
		BufferedReader input;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String deviceId_str = input.readLine();
			System.out.println(deviceId_str);
			if(!DeviceManager.checkClient(deviceId_str)) {
				// if the device hasn't registered yet, generate keys firstly.
				DeviceManager.generateKeyPairs(deviceId_str);
			}
			
			//transfer private key files to client
			File file = new File("keypairs/"+deviceId_str+"_private.pem");
			dos = new DataOutputStream(client.getOutputStream());
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			
			int buffferSize=1024;  
            byte[]bufArray=new byte[buffferSize];  
            dos.writeUTF("ec_"+deviceId_str+"_private.pem");   
            dos.flush();   
            dos.writeLong((long) file.length());   
            dos.flush();   
            while (true) {   
                int read = 0;   
                if (dis!= null) {   
                  read = dis.read(bufArray);   
                }   
                if (read == -1) {   
                  break;   
                }   
                dos.write(bufArray, 0, read);   
              }   
              dos.flush(); 
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(dis != null)
					dis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(dos != null)
					dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(client != null)
					client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

	}

}
