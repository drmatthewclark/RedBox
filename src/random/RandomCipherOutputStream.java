package random;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import RedBox.EncryptionKey;
import prng.MultiRandom;
import prng.SeedGenerator;

/**
 * class provides and extensible cryptographically secure stream.  The user supplies the source of
 * random numbers which could be java.util.Random, or other cryptographically secure random sources.
 * 
 * The PRNG is continuously modified by the actual data being encrypted so as to avoid common stream
 * cipher attacks.
 * 
 * @author CLARKM
 *
 */
public class RandomCipherOutputStream extends FilterOutputStream {

	/* random number generator, pre-seeded.  Suggest a secureRandom variation */
	private MultiRandom rand;
	private OutputStream os;
	private SeedGenerator seeds = new SeedGenerator();

	/**
	 * create an input stream from an existing stream and a source of random numbers. A 
	 * secureRandom generator is suggested. The same generator, with the same initial seed, is
	 * required to decrypt/encrypt the data.
	 * 
	 * @param is InputStream
	 * @param multiRandom random number source
	 */
	public RandomCipherOutputStream(final OutputStream os, EncryptionKey key) {
		this(os, key.getBytes());
	}
	
	
	public RandomCipherOutputStream(final OutputStream os, final byte[] key) {
		
		super(os);
		this.os = os;
		int nonceSize = Arrays.hashCode(key) & 0xF + 2;
		
		final byte[] nonce =  seeds.generateSeed(nonceSize);
		try { os.write(nonce); } catch (Exception e) {}
		final byte[] newkey = RandomCipherInputStream.concatenate(key, nonce);
	
		this.rand = new MultiRandom(newkey);
	}
	
	/**
	 * set the seed for the stream
	 * 
	 * @param seed byte[] seed
	 */
	public void setSeed(final long seed) {
		rand.setSeed(seed);
	}
	
	/**
	 * set the seed for the stream
	 * 
	 * @param seed long seed
	 */
	public void setSeed(final byte[] seed) {
		rand.setSeed(seed);
	}
	
	/**
	 * write an encrypted byte 
	 * 
	 * @param b byte to encrypt
	 * @throw IOException on error
	 */
	public final void write(final int b) throws IOException {
		os.write(b ^ rand.nextInt());
	}

	
	/**
	 * write an encrypted/decrypted byte array. This method copies the array so as not to alter the bytes in the
	 * argument.
	 * 
	 * @param b byte array to write
	 * @throws IOException on error
	 */
	public final void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	
	/**
	 * write an encrypted/decrypted byte array. This method copies the array so as not to alter the bytes in the
	 * argument.
	 * 
	 * @param b byte array to write
	 * @param offset offset of array to process
	 * @param len number of bytes to process
	 * @throws IOException on error
	 */
	public final void write(final byte[] b, final int offset, final int len) throws IOException {
		
		final byte[] copy = Arrays.copyOfRange(b, offset, offset + len);

		for (int i = 0; i < copy.length; i++) {
			copy[i] ^= rand.nextInt();
		}
		
		os.write(copy);
	}
}
