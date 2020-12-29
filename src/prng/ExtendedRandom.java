package prng;
/**
 * parent class for Extended random
 */


public abstract class ExtendedRandom  {


	private byte[] seed = null;
	

	/**
	 * set the seed
	 * @param long newSeed new seed
	 * 
	 */
	public void setSeed(long newSeed) {
		seed = longToByteArray(newSeed);
	}
	
	
	/**
	 * default set seed with array input.. Convert array to long.  Methods that allow more
	 * entropy will override this method.
	 * 
	 * @param seed byte[] of key
	 */
	public void setSeed(final byte[] seed) {
		this.seed = new byte[seed.length];
		System.arraycopy(seed, 0, this.seed, 0, seed.length);
	}
	
	public byte[] getSeed() {
		byte[] result = new byte[seed.length];
		System.arraycopy(seed, 0, result, 0, seed.length);
		return result;
	}
	
	public long seedToLong() {
		return bytesToLong(seed);
	}
	
	private int next(int bits) {
		return (int) (nextInt() >>> (48 - bits));
	}
	
	
	abstract public int nextInt();
	
	
	public float nextFloat() {
		return next(24) / (float) (1 << 24);
	}
	
	public double nextDouble() {
		return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	}
	
	
	/**
	 * create a 64 bit long with two 32 bit integers.
	 * @return long value
	 */
	public long nextLong() {
		return nextInt() & nextInt() >> 32;
	}
	
	
	public byte[] nextBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (nextInt() & 0xFF);
		}
		return bytes;
	}
	
	
	private boolean haveNextNextGaussian = false;
	private double  nextNextGaussian = Double.NaN;
	/*
	 * <p>This is described in section 3.4.1 of <em>The Art of Computer
	 * * Programming, Volume 2</em> by Donald Knuth.
	 *
	 * @return the next pseudorandom Gaussian distributed double
	 */
	public synchronized double nextGaussian() {
		if (haveNextNextGaussian) {
			haveNextNextGaussian = false;
			return nextNextGaussian;
		}
		double v1, v2, s;
		do
		{
			v1 = 2 * nextDouble() - 1; // Between -1.0 and 1.0.
			v2 = 2 * nextDouble() - 1; // Between -1.0 and 1.0.
			s = v1 * v1 + v2 * v2;
		}
		while (s >= 1);
		double norm = Math.sqrt(-2 * Math.log(s) / s);
		nextNextGaussian = v2 * norm;
		haveNextNextGaussian = true;
		return v1 * norm;
	}
	
	
	
	/**
	 * return random boolean
	 * @return
	 */
	boolean nextBoolean() {
		// select a random bit to examine to avoid bias
		int bit = 3;
		// return true if that bit is set.
		return (nextInt() & (1 << bit))  != 0;
	}
	
	
	/**
	 * return an array of int values constructed from an array of bytes.
	 * 
	 * @param byte array to convert to array of long values
	 * @return array of long values
	 */
	final static int[] bytesToIntArray(final byte[] bytes) {
				
		final int bytesPerValue = 4;
		// make sure there is enough room if the string length is not a multiple of 8
		final int resultLength = (bytes.length + bytesPerValue)/bytesPerValue;
		
		final int[] result = new int[resultLength];

		int byteCount = 0;

		for (int i = 0; i < resultLength; i++) {
			
			int value = 0;
			
			// pack each 8 bytes from the string into a long
			for (int j = 0; j < bytesPerValue && byteCount < bytes.length; j++) {
				value |= ((int)(bytes[byteCount++]) << (bytesPerValue * j));
			}
			
			result[i] = value;
		}
		
		return result;
	}
	
	/**
	 * return a long based on the bytes.
	 * 
	 * @param bytes bytes to hash to a long value
	 * @return a long value
	 */
	public static final long bytesToLong(byte[] bytes) {
		
		long result = 0;
		
		for(int i = 0; i < Math.min(8, bytes.length); i++) {
			result ^= (long)(bytes[i] & 0xFF ) << ((i*8) % 56);
		}

		return result;
	}
	
	/**
	 * return a long based on the bytes.
	 * 
	 * @param bytes bytes to hash to a long value
	 * @return a long value
	 */
	public static final int bytesToInt(byte[] bytes) {
		
		int result = 0;
		
		for(int i = 0; i < Math.min(4, bytes.length); i++) {
			result ^= (bytes[i] & 0xFF ) << ((i*8) % 24);
		}

		return result;
	}
	
	/**
	 * convert a long to a byte array.
	 * 
	 * @param value long value
	 * @return array of 8 bytes.
	 */
	final static byte[] longToByteArray(final long value) {
		
		final int len = Long.BYTES;
		final byte[] bytes = new byte[len];
		
		for (int i = 0; i < len; i++) {
			bytes[i] = (byte) ((value >> i*8) & 0xFF);
		}
		
		return bytes;
	}
	
}
