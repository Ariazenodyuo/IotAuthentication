package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.experimental.theories.Theories;

import Util.ClientConstant;
import Util.ServerConstant;
public class DeviceManager {
	
	
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(ServerConstant.DEVICEMANAGER_PORT);
		boolean f = true;
		while(f) {
			System.out.println("DeviceManager is working...");
			Socket client = server.accept();
			System.out.println("Connected to"+client.getInetAddress());
			new Thread(new DM_Thread(client)).run();
		}
		
		server.close();
		
	}
	
	public static boolean validate(String deviceId, String jwt) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, MalformedClaimException, CertificateException {
		if( ! new File(ServerConstant.KEY_PATH + "/" + deviceId + "_public.pem").exists())
			System.out.println("Public key doesn't exist");
		else {
			FileInputStream key = new FileInputStream(ServerConstant.KEY_PATH + "/" + deviceId + "_public.pem");
			CertificateFactory factory = CertificateFactory.getInstance("X.509");  
			JwtConsumer consumer = new JwtConsumerBuilder()
					.setVerificationKey(factory.generateCertificate(key).getPublicKey())
					.setExpectedAudience(deviceId)
					.setRequireExpirationTime()
					.setJwsAlgorithmConstraints(
						new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256))
					.build();
			
			try {
				JwtClaims claims = consumer.processToClaims(jwt);
				System.out.println("JWT validation succeeded! " + claims);
				return true;
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
		return false;
		
	}
		
	
	/**
	 * check if the device has already registered
	 * @param deviceId
	 * @return
	 */
	public static boolean checkClient(String deviceId) {
		File file1 = new File(ServerConstant.KEY_PATH + "/"+deviceId+"_public.pem");
		File file2 = new File(ServerConstant.KEY_PATH + "/"+deviceId+"_private.pem");
		return file1.exists() && file2.exists();
	}
	
	
	/**
	 * generate a pair of keys in directory "keypairs/", start with deviceId as filename
	 * @param deviceId
	 * @throws Exception
	 */
	public static int generateKeyPairs(String deviceId) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		String cmd_privateKey = ServerConstant.OPENSSL_PATH + " ecparam -genkey -name prime256v1 -noout -out "+ServerConstant.KEY_PATH+"/"+deviceId+"_private.pem";
		String cmd_publicKey = ServerConstant.OPENSSL_PATH + " req -x509 -new -key "+ ServerConstant.KEY_PATH +"/"+deviceId+"_private.pem -days 1000000 -out "+ ServerConstant.KEY_PATH +"/"+deviceId+"_public.pem -subj \"/CN=unused\"";
		Process process;
		
		int pcode = 0;
		process = runtime.exec(cmd_privateKey);
		pcode = process.waitFor();
		if(pcode == 1) {
			throw new Exception(getErrorMessage(process));
		}
		
		process = runtime.exec(cmd_publicKey);
		pcode = process.waitFor();
		if(pcode == 1) {
			throw new Exception(getErrorMessage(process));
		}
		
		
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
}

