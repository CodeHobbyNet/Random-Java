package net.codehobby;

import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;

public class PseudoRandomNumberGenerator
{
	private byte[] initializationVector;
	private BigInteger counter;
	private byte[] key;
        private byte[] cipherText;

	public void setIV( byte[] newInitializationVector )
	{
		initializationVector = newInitializationVector;
	}

	public void setCounter( BigInteger newCounter )
	{
		counter = newCounter;
	}

	public void setKey( byte[] newKey )
	{
		key = newKey;
	}

	public byte[] generate() throws Exception
	{//Generate some pseudo-random bytes.
		byte[] input;
                
		if( cipherText == null )
                {//If cipherText is empty, this is the first time the method has been run. Use the Initialization Vector.
                    input = counter.xor( new BigInteger(initializationVector) ).toByteArray();
                }
                else
                {//cipherText isn't emtpy because this method has run before, so use cipherText.
                    input = counter.xor( new BigInteger(cipherText) ).toByteArray();
                }
                
		SecretKeySpec sKey = new SecretKeySpec( key, 0, 16, "AES" );

		//Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

		cipher.init(Cipher.ENCRYPT_MODE, sKey);

		cipherText = cipher.doFinal(input, 0, input.length);

		//Set up for the next iteration
		counter = counter.add( BigInteger.valueOf(1) );
                
                return cipherText;
	}
        
        public static String bytesToHex( byte[] bites )
        {//Convert an array of bytes to a hex string for printing.
            StringBuilder strBuilder = new StringBuilder();
            
            for( byte bite : bites )
            {//Go through each byte and append it to the string as a couple hex characters.
                strBuilder.append( String.format("%02x", bite&0xff) );
            }
            
            //System.out.println( strBuilder.toString().length() );
            
            return strBuilder.toString();
        }
}
