package prng;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * A digest-based random number generator that allows use of any digest method, and 
 * any seed length.
 * 
 * @author crypto
 *
 */
public class DigestRandom extends ExtendedRandom {
	
	private MessageDigest digest;
	/* default digest to generate random numbers */
	private final static String DEFAULT_DIGEST = "SHA-512";
	/* internal state used for generating numbers */
	private transient byte[] state;
	private static int stateMultiplier = 5;
	private transient int index;

	
	/**
	 * constructor for digest
	 */
	public DigestRandom() {
		this(DEFAULT_DIGEST);
	}
	
	/**
	 * constructor with specified digest name
	 * 
	 * @param digestName name of digest recognized by java
	 */
	DigestRandom(String digestName)  {
		try {
			digest = MessageDigest.getInstance(digestName);
			state = new byte[digest.getDigestLength() * stateMultiplier];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	/**
	 * update the state used to generate random numbers
	 */
	void updateState() {
		
		final byte[] result = new byte[state.length];
		final int length = digest.getDigestLength();
		
		for (int i = 0; i < stateMultiplier; i++)  {
			// for each loop this digests the digest.
			System.arraycopy(digest.digest(state), 0, result, length*i, length);
		}
		index = 0;
		state = result;

	}
	
	/**
	 * set the seed bytes. This augments the entropy of the system and does not reset it.
	 * 
	 * @param byte[] seed bytes to use as a seed
	 */
	public void setSeed(final byte[] seed) {
		if (digest == null) return; // when called before initialization
		digest.update(seed);
		updateState();
	}
	
	/**
	 * set the seed bytes. This augments the entropy of the system and does not reset it.
	 * 
	 * @param long seed bytes to use as a seed
	 */
	public void setSeed(long seed) {
		setSeed(String.valueOf(seed).getBytes(StandardCharsets.UTF_8));
	}
	
	
	/**
	 * provides the next bytes from the random stream. 
	 * 
	 * @param bytes byte array to be filled with bytes
	 */
	private final int nextByte() {
		
		// get more bytes if necessary
		if (index >= state.length ) {
			updateState();
		}
		
		return (int)(state[index++]);
	}
	
	/**
	 * required method to return a random number of bits
	 * 
	 * @param bits number of bits between 0 and 32
	 * @return random integer composed of desired number of bits
	 */
	public final int nextInt() {
		
		final int val = 
				  (nextByte() ) 
				| (nextByte() << 8)   
				| (nextByte() << 16) 
				| (nextByte() << 24) ;
		
		return val;
	}

}
