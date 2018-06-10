package Util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

public class Utility {

	public static final int MINUTES_TO_EXPIRE = 10;
	public static String createJwtEs(String deviceId, String claimName, String value) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
		String privateKeyFile = ClientConstant.KEY_PATH+"/ec_"+deviceId+"_private_pkcs8";
		JwtClaims jwtClaims = new JwtClaims();
		jwtClaims.setIssuedAtToNow();
		jwtClaims.setExpirationTimeMinutesInTheFuture(MINUTES_TO_EXPIRE);
		jwtClaims.setAudience(deviceId);
		jwtClaims.setStringClaim(claimName, value);
		byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("EC");
		
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(jwtClaims.toJson());
		jws.setKey(kf.generatePrivate(spec));
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
		String jwt = jws.getCompactSerialization();
		return deviceId + ServerConstant.DEM + jwt;
	}
	
	public static void authenticateA(String message) throws UnknownHostException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, MalformedClaimException, CertificateException {
		String ClientId = message.split(ClientConstant.DEM)[0];
		Socket socket = new Socket(ClientConstant.DEVICEMANAGER_HOST, ClientConstant.AUTHENTICATION_PORT);
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println(message);
		Thread.sleep(5000);
		String content = input.readLine();
		System.out.println(content);
		input.close();
		out.close();
		socket.close();
		
		if("Public key doesn't exist".equals(content)){
			System.out.println(content);
		} else {
			validate(message.split(ClientConstant.DEM)[0], content);
		}
		
		
		
	}
	
	public static void validate(String deviceId, String jwt) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, MalformedClaimException, CertificateException {
		if( ! new File(ClientConstant.KEY_PATH + "/ec_" + deviceId + "_private_pkcs8").exists())
			System.out.println("Private key doesn't exist");
		else {
			
			FileInputStream key = new FileInputStream(ClientConstant.KEY_PATH + "/server_public.pem");
			
			CertificateFactory factory;
			factory = CertificateFactory.getInstance("X.509");
			JwtConsumer consumer = new JwtConsumerBuilder()
					.setVerificationKey(factory.generateCertificate(key).getPublicKey())
					.setRequireExpirationTime()
					.setExpectedAudience(deviceId)
					.setJwsAlgorithmConstraints(
						new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256))
					.build();
			
			
			try {
				JwtClaims claims = consumer.processToClaims(jwt);
				System.out.println("JWT validation succeeded! " + claims);
			} catch (InvalidJwtException  e) {
				System.out.println("Invalid JWT! " + e);
		        if (e.hasExpired())
		        {
		            System.out.println("JWT expired at " + e.getJwtContext().getJwtClaims().getExpirationTime());
		        }
		        // Or maybe the audience was invalid
		        if (e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID))
		        {
		            System.out.println("JWT had wrong audience: " + e.getJwtContext().getJwtClaims().getAudience());
		        }
		        
			}
			
		}
		
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, JoseException {
		System.out.println(createJwtEs("Henry", "content", "hahaha"));
	}
}
