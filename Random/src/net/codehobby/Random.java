
package net.codehobby;

import java.math.BigInteger;

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
        BigInteger initializationVector = new BigInteger( "1A024F91E8150033B974CD817BA67EB4", 16 );
        BigInteger counter = new BigInteger( "7486667286DEEB44A3C7C89658C73B25", 16 );
        BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D08", 16 );
        //BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D0837", 16 );
        PseudoRandomNumberGenerator prng = new PseudoRandomNumberGenerator();
        
        prng.setIV( initializationVector.toByteArray() );
        prng.setCounter( counter );
        prng.setKey( key.toByteArray() );
        
        for( int i = 0; i < 100; i++ )
        {
            try
            {
                System.out.println( PseudoRandomNumberGenerator.bytesToHex(prng.generate()) );
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
