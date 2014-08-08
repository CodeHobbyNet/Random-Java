
package net.codehobby;

import java.math.BigInteger;

/**
 *
 * @author Jeff Crone
 */
public class Random {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BigInteger initializationVector = new BigInteger( "1A024F91E8150033B974CD817BA67EB4", 16 );
        BigInteger counter = new BigInteger( "7486667286DEEB44A3C7C89658C73B25", 16 );
        BigInteger key = new BigInteger( "D373838825F7123B81E45C52EF8DA2BEB5582B44EC0231AD99EE598A894D0837", 16 );
        
        PseudoRandomNumberGenerator.setIV( initializationVector.toByteArray() );
        PseudoRandomNumberGenerator.setCounter( counter );
        PseudoRandomNumberGenerator.setKey( key.toByteArray() );
        
        for( int i = 0; i < 100; i++ )
        {
            try
            {
                System.out.println( PseudoRandomNumberGenerator.generate() );
            }
            catch( Exception e )
            {
                System.out.println( "Error getting random number " + i );
                e.printStackTrace();
            }
        }
    }
    
}
