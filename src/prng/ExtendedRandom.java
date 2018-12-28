package prng;
/**
 * parent class for Extended random
 */

import java.util.Arrays;
import java.util.Random;

public abstract class ExtendedRandom extends Random {


	private static final long serialVersionUID = 4705969117250773525L;
	protected long seed = serialVersionUID;
	

	/**
	 * set the seed
	 * @param long newSeed new seed
	 * 
	 */
	public void setSeed(long newSeed) {
		seed = newSeed;
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
			result |= (long)(bytes[i] & 0xFF ) << ((i*8) % 56);
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
		
		final byte[] bytes = new byte[8];
		
		bytes[0] = (byte) ((value      ) & 0xFF);
		bytes[1] = (byte) ((value >>  8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
		bytes[3] = (byte) ((value >> 24) & 0xFF);
		bytes[4] = (byte) ((value >> 32) & 0xFF);
		bytes[5] = (byte) ((value >> 40) & 0xFF);
		bytes[6] = (byte) ((value >> 48) & 0xFF);
		bytes[7] = (byte) ((value >> 56) & 0xFF);
		
		return bytes;
	}
	
	/**
	 * create a long from a byte array for random methods that use only
	 * a long value seed.
	 * @param seed byte array
	 * @return long value computed from byte array.
	 * 
	 */
	final static long byteArrayToLong(byte[] seed) {
		// use the deep hashcode of the array, and increase the length
		// to create a long hash.
		long hash = Arrays.hashCode(seed);
		// try to overflow to unreversible value, and use odd
		// number to preserve sign.
		long result = hash * hash * hash * hash * hash;
		return result;
	}
	
	/**
	 * default set seed with array input.. Convert array to long.  Methods that allow more
	 * entropy will override this method.
	 * 
	 * @param seed byte[] of key
	 */
	void setSeed(byte[] seed) {

		setSeed(byteArrayToLong(seed));
	}
	
}
