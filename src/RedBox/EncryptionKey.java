package RedBox;

import java.util.Arrays;
import prng.DigestRandom;
import java.nio.charset.StandardCharsets;


public class EncryptionKey {

	private byte[] key = null;
	
	/*
	 * depending on the Java deliverable this could be larger, but for most 
	 * default AES installations 16 bytes is all the keysize you get. This is the 
	 * key length for  each round of encryption.  If a longer key is used the 
	 * number of rounds is approximately length/KEYLEN + LENMULTIPLIER*KEYLEN
	 */
	private final int KEYLEN = 16;
	
	/*
	 * key is expanded to be at least this multiple of  key length.
	 * one key is 128 bits.  
	 * 
	 */
	private final int LENMULTIPLIER = 3;
	/*
	 * maximum key length in bytes. In principle there is no maximum size
	 * but in practice a long key takes a very long time to use, and can
	 * cause stack overflows.  This comes into play when a file use used as the key.
	 * 
	 * The full key is used to start the key function in any case.
	 */
	private final int MAX_KEY = 1024*16;


	/**
	 * constructor using a UTF-8 encoded string
	 * 
	 * @param ekey String, which will be encode to UTF-8 bytes
	 */
	public EncryptionKey(final String ekey) {
		this(ekey.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * constructor using an array of bytes
	 * 
	 * @param ekey byte[] with key
	 */
	public EncryptionKey(final byte[] ekey) {
		this.key = stretchKey(ekey);
	}
	
	/**
	 * copy constructor
	 * @param ekey get key
	 */
	public EncryptionKey(EncryptionKey ekey) {
		this(ekey.key);
	}

	/**
	 * the bytes to use as the key, stretched and conditioned from the original
	 * key input.
	 * 
	 * @return a secure key derived from the input key.
	 */
	public byte[] getBytes() {
		return Arrays.copyOf(key, key.length);
	}
	
	/**
	 * return key length
	 * @return length of key, in bytes
	 */
	public int size() {
		return key.length;
	}
	
	/**
	 * adjust the key to pad it to some multiple of KEYLEN. In the process it
	 * mixes up the original key so it makes it less prone to dictionary attack
	 * even if a dictionary word or common phrase is used.
	 * 
	 * A large number of digestion rounds are used to make the creation of 
	 * dictionary tables slower.
	 * 
	 * @return key possibly extended by padding.
	 * 
	 */
	public final byte[] stretchKey(final byte[] ekey) {
	
		/*
		 * determine key length. must be a multiple of KEYLEN
		 *
		 * */
		final int newlen = Math.min(
				ekey.length + (ekey.length % KEYLEN) + LENMULTIPLIER*KEYLEN,
				MAX_KEY);
		
		final byte[] newkey = new byte[newlen];
		final DigestRandom digest = new DigestRandom();

		// the goal in any case is to make this time consuming so that a brute-force
		// attack is slow.
		final int rounds = 0x3FFFFF/newlen  + 13;
		
		// process initial key and use for seed	
		digest.setSeed(ekey);
				
		for (int i = 0; i < rounds; i++) {
			digest.nextBytes(newkey);;
			digest.setSeed(newkey);
			digest.nextBytes(newkey);
		}
		
		digest.nextBytes(newkey);
		return newkey;
	}
	
	/**
	 * return part of key by segment
	 * 
	 * @param key  byte array with key
	 * @param i segment number
	 * @return subset of key
	 */
	public byte[] keySegment(final int i, final int len) {
		 return Arrays.copyOfRange(key, i, i + len);
	}
	
	/**
	 * hashcode for the key
	 * @return integer hashcode
	 */
	public int hashCode() {
		
		return Arrays.hashCode(key);
	}
}
