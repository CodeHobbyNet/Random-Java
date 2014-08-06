package net.codehobby;

import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;

public class PseudoRandomNumberGenerator
{
	private static byte[] initializationVector;
	private static BigInteger counter;
	private static byte[] key;

	public static void setIV( byte[] newInitializationVector )
	{
		initializationVector = newInitializationVector;
	}

	public static void setCounter( BigInteger newCounter )
	{
		counter = newCounter;
	}

	public static void setkey( byte[] newKey )
	{
		key = newKey;
	}

	public static byte[] generate() throws Exception
	{//Generate some pseudo-random bytes.
		//ToDo: Redesign this method to bring it close to GRC's design.

		//byte[] input = "test".getBytes();
		//byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
		
		//BigInteger keyInt = new BigInteger( 1, keyBytes );//Translate the key into a positive two's-compliment integer.

		SecretKeySpec sKey = new SecretKeySpec( key, "AES" );

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");

		cipher.init(Cipher.ENCRYPT_MODE, sKey);

		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
		cipher.doFinal(input, 0, input.length, cipherText, 0);
		System.out.println(new String(cipherText));

		//Set up for the next iteration
		input = cipherText;
		keyInt = keyInt.add(1);
	}
}
