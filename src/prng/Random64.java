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


	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;



	public int nextInt() {
		long s = seedToLong();
		s *= multiplier + addend;
		setSeed(s);
		return (int)s;

	}
}
