package prng;

/**
 * this is the same algorithm used for the normal java.util.Random, however the
 * 48 bit mask has been removed.  The 32 bit result is from the high-end of the 64 bit
 * word, possibly reducing problems with non-randomness of the low bits.
 * 
 * @author CLARKM
 *
 */
public class Random64 extends ExtendedRandom {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3752716801248928524L;
	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;


	/**
	 * provide the next random bits
	 * 
	 * @param bits - requested bits, ranges from 1 to 32
	 */
	protected final int next(int bits) {

		seed = seed * multiplier + addend;
		return (int) (seed >>> (64 - bits));
	}
}
