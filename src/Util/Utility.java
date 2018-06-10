package Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
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
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, JoseException {
		System.out.println(createJwtEs("Henry", "content", "hahaha"));
	}
}
