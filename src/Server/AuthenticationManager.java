package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Util.ServerConstant;

public class AuthenticationManager {
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(ServerConstant.AUTHENTICATION_PORT);
		boolean f = true;
		while(f) {
			System.out.println("AuthenticationManager is working...");
			Socket client = server.accept();
			System.out.println("Connected to"+client.getInetAddress());
			new Thread(new AS_Thread(client)).run();
		}
		
		server.close();
		
	}
}
