package net.codehobby;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        private boolean iVSet, counterSet, encryptionKeySet;
        private String keyFileName;
        private String APIKey;

        /**
         * Default constructor. Sets the *Set boolean values to false so this object knows they haven't been set yet.
         */
        public PseudoRandomNumberGenerator()
        {
            iVSet = false;
            counterSet = false;
            encryptionKeySet = false;
            keyFileName = "APIKey.txt";
            APIKey = "";
            
            fetchAPIKeyFromFile();
            
            getRandomDataFromWeb();
        }
        
        /**
         * Constructor that sets the initial values.
         * @param newInitializationVector The new InitializationVector.
         * @param newCounter The new Counter.
         * @param newKey The new Key.
         */
        public PseudoRandomNumberGenerator( byte[] newInitializationVector, BigInteger newCounter, byte[] newKey )
        {
            iVSet = false;
            counterSet = false;
            encryptionKeySet = false;
            keyFileName = "APIKey.txt";
            APIKey = "";
            
            setIV( newInitializationVector );
            setCounter( newCounter.toByteArray() );
            setEncryptionKey( newKey );
            
            fetchAPIKeyFromFile();
        }

        /**
         * Sets the Initialization Vector. The Initialization Vector will be used once at the beginning to 
         * help initialize the encryption process.
         * 
         * @param newInitializationVector The value to assign to the Initialization Vector. The value should be 16 bytes (128 bits) long and ideally as random as possible.
         */
	public void setIV( byte[] newInitializationVector )
	{
            if( newInitializationVector.length == 16 )
            {//If the length is right, 16 bytes (128 bits), go ahead and save the value.
		initializationVector = newInitializationVector;
                iVSet = true;
            }
            else
            {//If the lenght isn't right, throw an error.
                throw new IllegalArgumentException( "The key argument needs to be 16 bytes (128 bits)." );
            }
	}

        /**
         * Sets the counter. The counter is incremented each time random info is generated and is 
         * xor-ed with either the Initialization Vector or the previous ciphertext to feed into the 
         * AES encryption process.
         * 
         * @param newCounter The value to assign to the counter. The value should be 16 bytes (128 bits) long and ideally as random as possible.
         */
	public void setCounter( byte[] newCounter )
	{
            if( newCounter.length == 16 )
            {//If the length is right, 16 bytes (128 bits), go ahead and save the value.
		counter = new BigInteger( newCounter );
                counterSet = true;
            }
            else
            {//If the lenght isn't right, throw an error.
                throw new IllegalArgumentException( "The counter argument needs to be 16 bytes (128 bits)." );
            }
	}

        /**
         * Sets the encryption key for the AES encryption.
         * 
         * @param newKey The value to assign to the key. The value should be 32 bytes (256 bits) long and ideally as random as possible.
         */
	public void setEncryptionKey( byte[] newKey )
	{
            if( newKey.length == 32 )
            {//If the length is right, 32 bytes (256 bits), go ahead and save the value.
		key = newKey;
                encryptionKeySet = true;
            }
            else
            {//If the lenght isn't right, throw an error.
                throw new IllegalArgumentException( "The key argument needs to be 32 bytes (256 bits)." );
            }
	}
        
        /**
         * Sets newKeyFileName to the filename the program will look to for the Random.org API key. The file should be a text file containing only the key.
         * @param newKeyFileName The filename containing the key.
         */
        public void setAPIKeyFileName( String newKeyFileName )
        {
            keyFileName = newKeyFileName;
        }
        
        /**
         * Sets the Random.org API key to newKey.
         * @param newKey The value of the Random.org API key.
         */
        public void setAPIKey( String newKey )
        {
            APIKey = newKey;
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
                else if( !encryptionKeySet )
                {//If encryptionKeySet false, indicating the key isn't set, throw an exception.
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

                //System.out.println( "Input length: " + input.length );
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
         * Interprets hexValue as a string of a hexadecimal number and returns an array of bytes equivalent to that number.
         * @param hexValue A string with a hexadecimal number inside.
         * @return An array of bytes corresponding to the value of the number in hexValue.
         */
        public static byte[] hexToBytes( String hexValue )
        {
            //List<Byte> bites = new ArrayList<Byte>();
            
            byte[] bites = new byte[hexValue.length()/2];
            for( int i = 0; i < hexValue.length(); i+=2 )
            {//Go through each 2 characters and add them as bytes to bites.
                bites[i/2] = Byte.parseByte(hexValue.substring(i, i+1), 16);
            }
            
            return bites;
            //return new BigInteger( hexValue, 16 ).toByteArray();
        }
        
        /**
         * Gets some random data from the web and uses that data to set initializationVector, key and counter. 
         * Currently set to get data from Random.org's API.
         */
        public void getRandomDataFromWeb()
        {
            //Get the API Key from the file APIKey.txt
            int numBitsPerBlob = 128;//The number of bits to request per BLOB from Random.org.
            int numBlobs = 4;//The number of BLOBs to request from Random.org.
            
            
            //Uee the API key to get some random data from Random.org
            //See https://api.random.org/json-rpc/1/request-builder
            //See the method around line 2068 of RandomOrgClient.java of the project at https://github.com/RandomOrg/JSON-RPC-Java
            //System.err.println( "The part to contact Random.org hasn't been finished yet.");
            
            JsonObject jsonData = new JsonObject();
            JsonObject jsonResponse = new JsonObject();
            JsonObject params = new JsonObject();
            
            //System.err.println( "Add a check to make sure it's not going over Random.org's request limit" );
            
            //Create the JSON data to send to Random.org.
            jsonData.addProperty( "jsonrpc", "2.0" );
            jsonData.addProperty( "method", "generateBlobs" );
            params.addProperty( "apiKey", APIKey );
            params.addProperty( "n", numBlobs );
            params.addProperty( "size", numBitsPerBlob );
            params.addProperty( "format", "hex" );
            jsonData.add( "params", params );
            jsonData.addProperty( "id", UUID.randomUUID().toString() );
            //System.out.println( jsonData.toString() );
            
            try
            {
                if( fetchUsageFromWeb(numBitsPerBlob*numBlobs, APIKey) )
                {//If the request is authorized by Random.org, go ahead and make it.
                    jsonResponse = fetchFromWeb( jsonData );
                }
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
            //System.out.println( jsonResponse.toString() );
            if( jsonResponse.has("error") )
            {
                System.err.println( "In getRandomDataFromWeb(), error number " + jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("code").getAsString() + " was returned with message:" );
                System.err.println( jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("message").getAsString() );
            }
            else
            {
                JsonArray randomBlobs = jsonResponse.getAsJsonObject("result").getAsJsonObject("random").getAsJsonArray("data");
                setIV( hexToBytes(randomBlobs.get(0).getAsString()) );//Assign the IV to the first blob.
                setCounter( hexToBytes(randomBlobs.get(1).getAsString()) );//Assign the counter to the second blob.
                setEncryptionKey( hexToBytes(randomBlobs.get(2).getAsString() + randomBlobs.get(3).getAsString()) );//Assign the key to the third and fourth blobs since it needs 256 bits.
                /*
                for( int i = 0; i < randomBlobs.size(); i++ )
                {
                    System.out.println( randomBlobs.get(i).getAsString() );
                }
                */
                //System.err.println( "Add something to add the returned values to the initialization values.");
            }
            
        }
        
        /**
         * Gets the Random.org API key from the file pointed to by keyFileName.
         */
        public void fetchAPIKeyFromFile()
        {
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
        }
        
        /**
         * Gets the Random.org API key from the file pointed to by tempKeyFileName. The file should be a text file with only the key in it.
         * @param tempKeyFileName The filename of the file containing the Random.org API key.
         */
        public void fetchAPIKeyFromFile( String tempKeyFileName )
        {
            try {
                BufferedReader keyFileReader = new BufferedReader( new FileReader(tempKeyFileName) );
                APIKey = keyFileReader.readLine();
            } catch (FileNotFoundException ex) {
                System.err.println( keyFileName + " wasn't found." );
                setDefaultInitValues();
            } catch (IOException ex) {
                System.err.println( "Error reading " + keyFileName );
                setDefaultInitValues();
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
            BigInteger newInitializationVector = new BigInteger( "1A024F91E8150033B974CD817BA67EB4", 16 );
            BigInteger newCounter = new BigInteger( "7486667286DEEB44A3C7C89658C73B25", 16 );
            BigInteger newKey = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D08", 16 );
            //BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D0837", 16 );

            setIV( newInitializationVector.toByteArray() );
            setCounter( newCounter.toByteArray() );
            setEncryptionKey( newKey.toByteArray() );
        }
        
        /**
         * Checks the usage statistics from Random.org and returns whether it'll allow another request of initial values.
         * @param numBits The number of bits that are planned to be requested from Random.org.
         * @return True if Random.org should allow for the request, ,false if it shouldn't.
         */
        private boolean fetchUsageFromWeb( int numBits, String APIKey ) throws IOException, ProtocolException, Exception
        {
            JsonObject jsonData = new JsonObject();
            JsonObject jsonResponse = new JsonObject();
            JsonObject params = new JsonObject();
            
            //Create the JSON data to send to Random.org.
            jsonData.addProperty( "jsonrpc", "2.0" );
            jsonData.addProperty( "method", "getUsage" );
            params.addProperty( "apiKey", APIKey );
            jsonData.add( "params", params );
            jsonData.addProperty( "id", UUID.randomUUID().toString() );
            //System.out.println( jsonData.toString() );
            
            jsonResponse = fetchFromWeb( jsonData );
            
            //Take the data from the response and put it in the initialization data.
            //System.out.println( jsonResponse.toString() );
            if( jsonResponse.has("error") )
            {
                System.err.println( "In fetchUsageFromWeb(...), JSON Error number " + jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("code").getAsString() + " was returned with message:" );
                System.err.println( jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("message").getAsString() );
                throw new Exception( "JSON Error number " + jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("code").getAsString() + " was returned with message:" + jsonResponse.getAsJsonObject("error").getAsJsonPrimitive("message").getAsString() );
            }
            else
            {
                String status = jsonResponse.getAsJsonObject("result").getAsJsonPrimitive("status").getAsString();
                if( status.contentEquals("paused") )
                {//If the API Key is paused by Random.org, return false.
                    System.err.println( "The API Key is paused." );
                    return false;
                }
                else
                {
                    long requestsLeft = jsonResponse.getAsJsonObject("result").getAsJsonPrimitive("requestsLeft").getAsLong();
                    if( requestsLeft < 1 )
                    {//If there aren't any more requests left, return false.
                        System.err.println( "There are no requests left. The requestsLeft field returned by Random.org is " + requestsLeft );
                        return false;
                    }
                    else
                    {
                        long bitsLeft = jsonResponse.getAsJsonObject("result").getAsJsonPrimitive("requestsLeft").getAsLong();
                        if( bitsLeft < numBits )
                        {//If there aren't any more requests left, return false.
                            System.err.println( "There aren't enough bits left to request. Random.org says it'll only allow a request of up to " + bitsLeft + " bits.");
                            return false;
                        }
                        else
                        {//There is nothing blocking the request per Random.org. Return true.
                            return true;
                        }
                    }
                }
            }
        }
        
        /**
         * Saves some pseudo-random data to a file either as hex or bytes.
         * @param filename The filename of the file to save data to.
         * @param numGroups The number of outputs of generate to save.
         * @param asHex
         * @throws Exception If an exception is generated during the generation of the pseudo-random data.
         */
        public void savePseudoRandomDataToFile( String filename, long numGroups, boolean asHex ) throws Exception
        {
            DataOutputStream binaryOutput = null;
            BufferedWriter textWriter = null;
            try {
                binaryOutput = new DataOutputStream( new FileOutputStream(filename, true) );//Create the file output stream. Will append to the file.
                textWriter = new BufferedWriter( new FileWriter(filename, true) );
                
                int i = 0;
                while( i < numGroups )
                {//Iterate until it gets to the number of groups as a parameter.
                    byte[] group = generate();//Generate the pseudo-random data
                    if( asHex )
                    {
                        textWriter.write( bytesToHex(group) + "\n" );//Write the random data as hex to the file.
                    }
                    else
                    {
                        System.out.println( "Writing as binary." );
                        for( byte bite : group )
                        {
                            binaryOutput.writeByte(bite);
                        }
                    }
                    
                    i++;
                }
                
                
                
            } catch (FileNotFoundException ex) {
                System.err.println( "File not found error for file \"" + filename + "\": ");
                System.err.println( ex.getMessage() );
                ex.printStackTrace();
            } catch (IOException ex) {
                System.err.println( "IO Exception for file \"" + filename + "\": ");
                System.err.println( ex.getMessage() );
                ex.printStackTrace();
            }
            finally
            {
                //binaryOutput.close();//Close the file.
                textWriter.close();//Close the file.
            }
        }
}
