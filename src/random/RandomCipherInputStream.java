package random;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.Arrays;

import RedBox.EncryptionKey;
import prng.MultiRandom;


/**
 * class provides and extensible cryptographically secure stream.  The user supplies the source of
 * random numbers which could be java.util.Random, or other cryptographically secure random sources.
 * 
 * 
 * @author CLARKM
 *
 */
public class RandomCipherInputStream extends FilterInputStream {

	/* random number generator, pre-seeded.  Suggest a secureRandom variation */
	private MultiRandom rand;
	private InputStream is;

	
	/**
	 * create an input stream from an existing stream and a source of random numbers. A 
	 * secureRandom generator is suggested. The same generator, with the same initial seed, is
	 * required to decrypt/encrypt the data.
	 * 
	 * @param is InputStream
	 * @param multiRandom random number source
	 */
	public RandomCipherInputStream(final InputStream is, final EncryptionKey key) {
		this(is, key.getBytes());
	}
	
	/**
	 * create an input stream from an existing stream and a source of random numbers. A 
	 * secureRandom generator is suggested. The same generator, with the same initial seed, is
	 * required to decrypt/encrypt the data.
	 * 
	 * @param is InputStream
	 * @param multiRandom random number source
	 */
	public RandomCipherInputStream(final InputStream is, final byte[] key) {
		
		super(is);
		int nonceSize = Arrays.hashCode(key) & 0xF + 2;
		final byte[] nonce = new byte[nonceSize];
		
		try { is.read(nonce); } catch (Exception e){};
		final byte[] newkey = concatenate(key, nonce);
		this.rand = new MultiRandom(newkey);
		this.is = is;
	}
	

	/**
	 * set the seed for the stream
	 * @param seed byte[] with seed
	 */
	public void setSeed(final byte[] seed) {
		((MultiRandom)rand).setSeed(seed);
	}
	
	
	/**
	 * set the seed for the stream
	 * @param seed long with seed
	 */
	public void setSeed(final long seed) {
		rand.setSeed(seed);
	}
	
	/**
	 * reads a byte from the input stream, possibly blocking to wait for the byte.
	 * 
	 * @return byte read, as an integer.
	 * @throws IOException on error
	 */
	public int read() throws IOException {
		return is.read() ^ rand.nextInt();
	}

	/**
	 * Reads up to byte.length bytes of data from this input stream into an array of bytes. This method blocks until 
	 * some input is available. 
	 * This method simply performs the call read(b, 0, b.length) and returns the result. It is important that it does 
	 * not do in.read(b) instead; certain subclasses of FilterInputStream depend on the implementation strategy actually used. 
	 *  
	 *  @param b byte buffer to fill
	 *  @return number of bytes read
	 *  @throws IOException on error
	 */
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	

	/**
	 * Reads up to len bytes of data from this input stream into an array of bytes. If len is not zero, 
	 * the method blocks until some input is available; otherwise, no bytes are read and 0 is returned. 
	 * This method simply performs in.read(b, off, len) and returns the result. 
	 * 
	 * @param b byte buffer to fill
	 * @param offset int offset into buffer to put the bytes read
	 * @param len number of bytes to read
	 * @return number of bytes actually read
	 * @throws IOException on error
	 * 
	 */
	public int read(final byte[] b,final  int offset, final int len) throws IOException {
		
		final int result = is.read(b, offset, len);
		
		/* encrypt/decrypt the array */
		for (int i = 0; i < result; i++) {
			b[i + offset] ^= rand.nextInt();
		}
		
		return result;
	}
	
	
	/**
	 * concatemate two arrays
	 * 
	 * @param first first array
	 * @param second second array
	 * @return concatenation of the two arrays
	 */
	static final byte[] concatenate(final byte[] first,  final byte[] second) {
		
		final byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
