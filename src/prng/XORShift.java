package prng;


/**
 * implementation of Marsiglia XOR shift random number generator.  
 * Although it may have the weakness that zero is never a native
 * value, since the result is shifted 32 bits, zero is in the result domain thus correcting this fault.
 * 
 * @author CLARKM
 *
 */
public class XORShift extends ExtendedRandom {

	private static final long serialVersionUID = -3429778336651270452L;
	
	private long[] s;
	private int p;
	private static final int STATE_SIZE = 32;
	
	/**
	 * set the seed 
	 * 
	 * @param newSeed long seed
	 */
	public void setSeed(final long newSeed) {
		
		s = new long[STATE_SIZE];
		seed = newSeed;
		/*
		 * initialize with "standard" LC random numbers
		 */
		for (int i = 0; i < s.length; i++) {
			seed = seed * 0x5DEECE66DL + 0xBL;
			s[i] = seed;
		}
	}
	

	/**
	 * xorshift* from wikipedia!
	 * 
	 * @return next random in sequence
	 */
	private final long xorshift1024star() {
		long s0 = s[p];
		long s1 = s[p = ( p + 1 ) & (STATE_SIZE - 1)];
		s1 ^= s1 << 31; // a
		s1 ^= s1 >> 11; // b
		s0 ^= s0 >> 30; // c
		return ( s[p] = s0 ^ s1 ) * 1181783497276652981L;
	}
	
	/**
	 * generate the next bits
	 * 
	 * @param bits - number of bits to return
	 */
	public final int next(final int bits) {
		  return (int) (xorshift1024star() >>> (64 - bits)) ;
	}

}
