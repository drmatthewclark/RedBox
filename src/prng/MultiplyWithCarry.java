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
	private static final long serialVersionUID = -8752716801848928524L;
	private static final long multiplier = 0xffffda61L;



	/**
	 * provide the next random bits
	 * 
	 * @param bits - requested bits, ranges from 1 to 32
	 */
	protected final int next(int bits) {

		seed = (multiplier * (seed & 0xffffffffL)) + (seed >>> 32);
		return (int)(seed >>> (64 - bits));
	}

}
