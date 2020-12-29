package prng;

/**
 * multiply with carry algorithm for pseudo random numbers
 * 
 * @author CLARKM
 *
 */
public class MultiplyWithCarry extends ExtendedRandom {

	/**
	 * 
	 */
	private static final long multiplier = 0xffffda61L;



	/**
	 * provide the next random bits
	 * 
	 * @param bits - requested bits, ranges from 1 to 32
	 */
	public final int nextInt() {
		long s = seedToLong();
		s = (multiplier * (s & 0xffffffffL)) + (s >>> 32);
		setSeed(s);
		return (int)(s >>> 32);
	}

}
