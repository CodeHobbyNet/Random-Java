package net.codehobby;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is a pseudo-random number generator. It's mainly meant for use by various other Java applications I intend to 
 * write in the future.
 * <p>
 * The design for the pseudo-random number generator was inspired by the description of the pseudo-random number 
 * generator on GRC's Ultra High Security Password Generator at https://www.grc.com/passwords.htm
 * 
 * @author Jeff Crone
 */
public class PseudoRandomNumberGenerator
{
	private byte[] initializationVector;
	private BigInteger counter;
	private byte[] key;
        private byte[] cipherText;
        private boolean iVSet, counterSet, keySet;

        /**
         * Default constructor. Sets the *Set boolean values to false so this object knows they haven't been set yet.
         */
        public PseudoRandomNumberGenerator()
        {
            iVSet = false;
            counterSet = false;
            keySet = false;
        }

        /**
         * Sets the Initialization Vector. The Initialization Vector will be used once at the beginning to 
         * help initialize the encryption process.
         * 
         * @param newInitializationVector The value to assign to the Initialization Vector. The value should be 16 bytes (128 bits) long and ideally as random as possible.
         */
	public void setIV( byte[] newInitializationVector )
	{
		initializationVector = newInitializationVector;
                iVSet = true;
	}

        /**
         * Sets the counter. The counter is incremented each time random info is generated and is 
         * xor-ed with either the Initialization Vector or the previous ciphertext to feed into the 
         * AES encryption process.
         * 
         * @param newCounter The value to assign to the counter. The value should be 16 bytes (128 bits) long and ideally as random as possible.
         */
	public void setCounter( BigInteger newCounter )
	{
		counter = newCounter;
                counterSet = true;
	}

        /**
         * Sets the encryption key for the AES encryption.
         * 
         * @param newKey The value to assign to the key. The value should be 32 bytes (256 bits) long and ideally as random as possible.
         */
	public void setKey( byte[] newKey )
	{
		key = newKey;
                keySet = true;
	}

        /**
         * This is the meat of the pseudo-random number generator. It's what generates the random bytes.
         * <p>
         * The method sets up the input bytes for the AES encryption by xor-ing the counter with either the 
         * Initialization Vector if this is the first run through or the previous run through's ciphertext if not.
         * The method then sets up the encryption to use AES in CBC (Cipher Block Chaining) mode.
         * After that, the method uses the input bytes with the key to generate 128 bits (16 bytes) of 
         * pseudo-random data in the form of an array of bytes.
         * 
         * @return An array of 16 bytes representing the pseudo-random data generate this time around.
         * @throws Exception The Cipher object can throw either a NoSuchAlgorithmException, an InvalidKeyException, or an IllegalBlcokSizeException. Also throws an IllegalStateException if any of the *Set variables aren't set yet.
         */
	public byte[] generate() throws Exception
	{//Generate some pseudo-random bytes.
		byte[] input;

                if( !iVSet )
                {//If iVSet is false, indicating the Initialization Vector isn't set, throw an exception.
                    throw new IllegalStateException( "The Initialization Vector isn't set." );
                }
                else if( !counterSet )
                {//If counterSet is false, indicating the counter isn't set, throw an exception.
                    throw new IllegalStateException( "The counter isn't set." );
                }
                else if( !keySet )
                {//If keySet false, indicating the key isn't set, throw an exception.
                    throw new IllegalStateException( "The key isn't set." );
                }


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

        /**
         * Returns a string representation of the value of the bites parameter in hex format.
         * 
         * @param bites The binary data to format as a hex string.
         * @return A string representation in hex format of the binary data in bites.
         */
        public static String bytesToHex( byte[] bites )
        {//Convert an array of bytes to a hex string for printing.
            StringBuilder strBuilder = new StringBuilder();
            
            for( byte bite : bites )
            {//Go through each byte and append it to the string as a couple hex characters.
                strBuilder.append( String.format("%02x", bite&0xff) );
            }
            
            return strBuilder.toString();
        }
        
        /**
         * Gets some random data from the web and uses that data to set initializationVector, key and counter. 
         * Currently set to get data from Random.org's API.
         */
        private void getRandomDataFromWeb()
        {
            //Get the API Key from the file APIKey.txt
            String keyFileName = "APIKey.txt";
            String APIKey;
            try {
                BufferedReader keyFileReader = new BufferedReader( new FileReader(keyFileName) );
                APIKey = keyFileReader.readLine();
            } catch (FileNotFoundException ex) {
                System.err.println( keyFileName + " wasn't found." );
            } catch (IOException ex) {
                System.err.println( "Error reading " + keyFileName );
            }
            System.err.println( "Check if there are errors reading the API Key file." );
            
            //Uee the API key to get some random data from Random.org
            //See https://api.random.org/json-rpc/1/request-builder
            System.err.println( "The part to contact Random.org hasn't been writtten yet.");
            
        }
}
