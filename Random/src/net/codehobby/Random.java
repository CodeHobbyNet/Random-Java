
package net.codehobby;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is just a basic use of the PseudoRandomNumberGenerator class to show how it works and to test it.
 * 
 * @author Jeff Crone
 */
public class Random {

    /**
     * The starting point for the class. It creates a PseudoRandomNumberGenerator object and uses it to generate pseudo-random data.
     * 
     * @param args The command line arguments. So far they're not used.
     */
    public static void main(String[] args) {
        /*
        BigInteger initializationVector = new BigInteger( "1A024F91E8150033B974CD817BA67EB4", 16 );
        BigInteger counter = new BigInteger( "7486667286DEEB44A3C7C89658C73B25", 16 );
        BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D08", 16 );
        //BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D0837", 16 );
        */
        PseudoRandomNumberGenerator prng = new PseudoRandomNumberGenerator();
        long numGroups = 10;
        String outputFileName = "output.txt";
        boolean hex = true;
        
        //Get the command line parameters
        if( args.length > 0 )
        {
            numGroups = Long.parseLong( args[0] );
            
            if( args.length > 1 )
            {
                outputFileName = args[1];
                
                if( args.length > 2 )
                {
                    if( args[2].equalsIgnoreCase("true") )
                    {
                        hex = true;
                    }
                    else if( args[2].equalsIgnoreCase("false") )
                    {
                        hex = false;
                    }
                    else
                    {
                        System.err.println( "The third argument should be true or false." );
                    }
                }
                
                if( args.length > 3 )
                {
                    System.err.println( "There should be 3 arguments, the number of groups to generate, the filename, and whether to save it as hex." );
                }
            }
        }
        /*
        prng.setIV( initializationVector.toByteArray() );
        prng.setCounter( counter );
        prng.setKey( key.toByteArray() );
        */
        
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
        
        try {
            prng.savePseudoRandomDataToFile(outputFileName, numGroups, hex);
        } catch (Exception ex) {
            System.err.println( "Error saving pesudo-random data to \"" + outputFileName + "\": " + ex.getMessage() );
            ex.printStackTrace();
        }
    }
    
}
