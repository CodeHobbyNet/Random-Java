package net.codehobby;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

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
                
                if( input.length%16 != 0 )
                {//The block size needs to be a multpile of 16 bytes. If it's not, pad out input.
                    byte[] newInput = Arrays.copyOf( input, input.length + (16-input.length%16) );
                    input = newInput;
                }
                
		SecretKeySpec sKey = new SecretKeySpec( key, 0, 16, "AES" );

		//Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

		cipher.init(Cipher.ENCRYPT_MODE, sKey);

                System.out.println( "Input length: " + input.length );
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
        public void getRandomDataFromWeb()
        {
            //Get the API Key from the file APIKey.txt
            String keyFileName = "APIKey.txt";
            String APIKey = "";
            
            try {
                BufferedReader keyFileReader = new BufferedReader( new FileReader(keyFileName) );
                APIKey = keyFileReader.readLine();
            } catch (FileNotFoundException ex) {
                System.err.println( keyFileName + " wasn't found." );
                setDefaultInitValues();
            } catch (IOException ex) {
                System.err.println( "Error reading " + keyFileName );
                setDefaultInitValues();
            }
            //System.err.println( "Check if there are errors reading the API Key file." );
            
            //Uee the API key to get some random data from Random.org
            //See https://api.random.org/json-rpc/1/request-builder
            //See the method around line 2068 of RandomOrgClient.java of the project at https://github.com/RandomOrg/JSON-RPC-Java
            //System.err.println( "The part to contact Random.org hasn't been finished yet.");
            
            JsonObject jsonData = new JsonObject();
            JsonObject jsonResponse = new JsonObject();
            JsonObject params = new JsonObject();
            
            System.err.println( "Add a check to make sure it's not going over Random.org's request limit" );
            
            //Create the JSON data to send to Random.org.
            //System.err.println( "Still working on creating the JSON data to send to Random.org.");
            jsonData.addProperty( "jsonrpc", "2.0" );
            jsonData.addProperty( "method", "generateBlobs" );
            params.addProperty( "apiKey", APIKey );
            params.addProperty( "n", 4 );
            params.addProperty( "size", 128 );
            params.addProperty( "format", "hex" );
            jsonData.add( "params", params );
            jsonData.addProperty( "id", UUID.randomUUID().toString() );
            System.out.println( jsonData.toString() );
            
            try
            {
                jsonResponse = fetchFromWeb( jsonData );
            } catch( MalformedURLException ex ) {
                System.err.println( "The URL was malformed." );
                System.err.println( "Error Message: " + ex.getMessage() );
                ex.printStackTrace();
                setDefaultInitValues();
            } catch( SocketTimeoutException e ) {
                System.err.println( "The connection to Random.org timed out." );
                System.err.println( "Error Message: " + e.getMessage() );
                e.printStackTrace();
                setDefaultInitValues();
            } catch (ProtocolException ex) {
                System.err.println( "The protocol (probably POST protocol) isn't supported." );
                System.err.println( "Error Message: " + ex.getMessage() );
                ex.printStackTrace();
                setDefaultInitValues();
            } catch (IOException ex) {
                System.err.println( "Input/Output exception." );
                System.err.println( "Error Message: " + ex.getMessage() );
                ex.printStackTrace();
                setDefaultInitValues();
            } catch (Exception ex) {
                System.err.println( "Exception: " + ex.getMessage() );
                ex.printStackTrace();
                setDefaultInitValues();
            }
            
            //Take the data from the response and put it in the initialization data.
            System.out.println( jsonResponse.toString() );
            if( jsonResponse.has("error") )
            {
                System.err.println( "Error number " + jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("code").getAsString() + " was returned with message:" );
                System.err.println( jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("message").getAsString() );
            }
            else
            {
                JsonArray randomBlobs = jsonResponse.getAsJsonObject("result").getAsJsonObject("random").getAsJsonArray("data");
                for( int i = 0; i < randomBlobs.size(); i++ )
                {
                    System.out.println( randomBlobs.get(i).getAsString() );
                }
                System.err.println( "Add something to add the returned values to the initialization values.");
            }
            
        }
        
        /**
         * Takes a request for Random.org as a JSON object, sends it to Random.org and returns the output as another JSON object.
         * @param jsonRequest The request as a JSON object.
         * @return The output from Random.org as a JSON object.
         * @throws MalformedURLException
         * @throws ProtocolException
         * @throws IOException
         * @throws Exception 
         */
        private JsonObject fetchFromWeb( JsonObject jsonRequest ) throws MalformedURLException, ProtocolException, IOException, Exception
        {
            String URLText = "https://api.random.org/json-rpc/1/invoke";
            HttpsURLConnection connection = (HttpsURLConnection) new URL( URLText ).openConnection();//Open the connection.
            connection.setConnectTimeout(5000);//Set the timeout to 5 seconds.
            
            //Set the headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            
            //Send the request with the JSON and get the response
            connection.setDoOutput( true );
            DataOutputStream outputStream = new DataOutputStream( connection.getOutputStream() );
            outputStream.writeBytes( jsonRequest.toString() );
            outputStream.flush();
            outputStream.close();
            int responseCode = connection.getResponseCode();
            
            if( responseCode == HttpsURLConnection.HTTP_OK )
            {//If the response code is ok, get the json data returned by Random.org.
                BufferedReader inputReader = new BufferedReader( new InputStreamReader(connection.getInputStream()) );//Establish the input reader.
                String inputLine;
                StringBuffer inputBuffer = new StringBuffer();
                
                while( (inputLine = inputReader.readLine()) != null )
                {//Take each line from the reader and append them to the buffer until there are no more lines from the reader.
                    inputBuffer.append( inputLine );
                }
                
                //Close the reader.
                inputReader.close();
                
                //Parse the buffer into a json object and return it.
                return new JsonParser().parse( inputBuffer.toString() ).getAsJsonObject();
            }
            else
            {
                System.err.println( "Error " + responseCode + ": " + connection.getResponseMessage() );
                throw new Exception( "Error " + responseCode + ": " + connection.getResponseMessage() );
            }
        }
        
        /**
         * Sets some default initialization values. 
         * Using this method isn't ideal since the values returned will be less random, but it's better than 
         * returning no pseudo-random values.
         */
        private void setDefaultInitValues()
        {
            BigInteger initializationVector = new BigInteger( "1A024F91E8150033B974CD817BA67EB4", 16 );
            BigInteger counter = new BigInteger( "7486667286DEEB44A3C7C89658C73B25", 16 );
            BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D08", 16 );
            //BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D0837", 16 );

            setIV( initializationVector.toByteArray() );
            setCounter( counter );
            setKey( key.toByteArray() );
        }
}
