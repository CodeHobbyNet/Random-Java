
package net.codehobby;

//import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import net.codehobby.CommandLineArguments;

/**
 * This is just a basic use of the PseudoRandomNumberGenerator class to show how it works and to test it.
 * 
 * @author Jeff Crone
 */
public class Random {

    /**
     * The starting point for the class. It creates a PseudoRandomNumberGenerator object and uses it to generate pseudo-random data.
     * 
     * @param args The command line arguments. -h for hex output (otherwise binary output), -o "filename" to output to file, -n # to specify how many random sets.
     */
    public static void main(String[] args) {
        PseudoRandomNumberGenerator prng = new PseudoRandomNumberGenerator();
        long numGroups = 10;
        String outputFileName = "output.txt";
        boolean saveToFile = false;
        boolean hex = true;
        Map<String, String> arguments = new HashMap<String, String>();
        CommandLineArguments cla = new CommandLineArguments( args );
        
        //Get the command line parameters
        try {
            arguments = cla.parse();
        } catch (Exception ex) {
            System.err.println( "Error parsing the arguments: " + ex.getMessage() );
        }
        if( arguments.containsKey("n") )
        {//The number of groups argument.
            numGroups = Integer.parseInt( arguments.get("n") );
        }
        
        if( arguments.containsKey("o") )
        {//The filename argument.
            outputFileName = arguments.get( "o" );
            saveToFile = true;
        }
        
        if( arguments.containsKey("h") )
        {//The output as hex argument.
            hex = true;
        }
        else
        {
            hex = false;
        }
        
        if( saveToFile )
        {
            try {
                prng.savePseudoRandomDataToFile(outputFileName, numGroups, hex);
            } catch (Exception ex) {
                System.err.println( "Error saving pesudo-random data to \"" + outputFileName + "\": " + ex.getMessage() );
                ex.printStackTrace();
            }
        }
        else
        {
            for( int i = 0; i < numGroups; i++ )
            {
                try
                {
                    if( hex )
                    {
                        System.out.println( PseudoRandomNumberGenerator.bytesToHex(prng.generate()) );
                    }
                    else
                    {
                        System.out.println( prng.generate() );
                    }
                    System.out.println();
                }
                catch( Exception e )
                {
                    System.out.println( "Error getting random number " + i );
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    
}
