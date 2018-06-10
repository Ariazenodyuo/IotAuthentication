package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import org.jose4j.base64url.Base64;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import Util.ClientConstant;
import Util.ServerConstant;

public class AS_Thread implements Runnable {
	
private Socket client = null;
	
	public AS_Thread(Socket socket) {
		this.client = socket;
	}

	@Override
	public void run() {
		BufferedReader input = null;
		DataOutputStream dos = null;
		PrintStream out = null;
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = input.readLine();
			String deviceId_dst = message.split(ServerConstant.DEM)[0];
			String deviceId_src = message.split(ServerConstant.DEM)[1];
			String content = message.split(ServerConstant.DEM)[2];
			
			
			//transfer private key files to client
			File file_src = new File("keypairs/"+deviceId_src+"_public.pem");
			File file_dst = new File("keypairs/"+deviceId_dst+"_public.pem");
			dos = new DataOutputStream(client.getOutputStream());
			
			
			out = new PrintStream(client.getOutputStream());
			// key file doesn't exist
			if(!DeviceManager.checkClient(deviceId_src)) {
				// if the device hasn't registered yet, generate keys firstly.
				out.println("Device isn't registered\n");
			} else {
				
				//decryption
				FileInputStream key = new FileInputStream(ServerConstant.KEY_PATH + "/" + deviceId_src + "_public.pem");
				
				CertificateFactory factory;
				factory = CertificateFactory.getInstance("X.509");
				JwtConsumer consumer = new JwtConsumerBuilder()
						.setVerificationKey(factory.generateCertificate(key).getPublicKey())
						.setSkipAllDefaultValidators()
						.setJwsAlgorithmConstraints(
							new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256))
						.build();
				JwtClaims claims = consumer.processToClaims(content);
				claims.setAudience(claims.getAudience());
				claims.setIssuedAt(claims.getIssuedAt());
				for(Map.Entry<String, Object>map : claims.getClaimsMap().entrySet()) {
					claims.setClaim(map.getKey(), map.getValue());
				}
				claims.setExpirationTime(claims.getExpirationTime());
				
				
				//encryption
				
				String privateKeyFile = ServerConstant.KEY_PATH+"/server_private_pkcs8";
				byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
				KeyFactory kf = KeyFactory.getInstance("EC");
				
				JsonWebSignature jws = new JsonWebSignature();
				jws.setPayload(claims.toJson());
				jws.setKey(kf.generatePrivate(spec));
				jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
				String jwt = jws.getCompactSerialization();
				
				
//
//			    byte[] keyBytes = Base64.decode(new String(Files.readAllBytes(Paths.get(ServerConstant.KEY_PATH + "/" + deviceId_dst + "_public.pem"))));
//			    //构造X509EncodedKeySpec对象
//			    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
//			    //指定加密算法
//			    KeyFactory keyFactory = KeyFactory.getInstance("EC");
//			    //取公钥匙对象
//			    PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);
//				
//				
//////				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Files.readAllBytes(Paths.get(ServerConstant.KEY_PATH + "/" + deviceId_dst + "_public.pem")));
////				System.out.println(keySpec.getFormat());
//////				KeyFactory factory2 = KeyFactory.getInstance("X.509");
//				
//				key = new FileInputStream(ServerConstant.KEY_PATH + "/" + deviceId_dst + "_public.pem");
//				X509Certificate certificate = (X509Certificate) factory.generateCertificate(key);
//				JsonWebSignature jws = new JsonWebSignature();
//				jws.setPayload(claims.toJson());
////				jws.setKey(factory.generateCertificate(key).getPublicKey());
////				jws.setX509CertSha256ThumbprintHeaderValue(certificate);
//				jws.setAlgorithmHeaderValue("X.509");
//				jws.setKey(publicKey2);
//				String jwt = jws.getCompactSerialization();
//				dos.write(jwt.getBytes());
				
				out.write(jwt.getBytes());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

	}

}
