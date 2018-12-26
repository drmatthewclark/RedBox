package cipher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import RedBox.EncryptionKey;
import prng.SeedGenerator;

/**
 * class to provide strongly encrypted streams.  It allows use of keys of any length
 * with standard encryption to provide more security than the built-in algorithm.
 * 
 * @author mclark
 * @param <ncryptionKey>
 *
 */
public class CipherStream {

	/*
	 * specification of cipher used for encryption
	 */
	private String CIPHERSPEC;

	
	SeedGenerator seeds = new SeedGenerator();

	/*
	 * depending on the Java deliverable this could be larger, but for most 
	 * default installations 16 bytes is all the keysize you get. This is the key length for
	 * each round of encryption.  If a longer key is used the number of rounds is approximately
	 * length/KEYLEN + LENMULTIPLIER*KEYLEN
	 */
	private int KEYLEN;
	
	/*
	 * storage for encryption key
	 */
	private transient EncryptionKey KEY;
	
	/*
	 * encryption buffer size for input/output streams
	 */
	private final int BUFFER_SIZE=4096;
	
	/**
	 * constructor that creates the stream, reads from standard input and writes to standard output
	 * @param <EncryptionKey>
	 * 
	 * @param args specfies encryption/decryption mode and optionally key.
	 * 
	 * @throws IOException on any IO Exception key. 
	 */
	public CipherStream(String CipherSpec, int keylen, final EncryptionKey key) throws IOException {
		KEY = key;
		CIPHERSPEC = CipherSpec;
		KEYLEN = keylen;
	}

	
	/**
	 * encrypt standard input and write encrypted bytes to standard output
	 * 
	 * @param key encryption key, unlimited length
	 * @throws IOException
	 */
	public final void encrypt(final InputStream input, final OutputStream output) throws IOException {

		final byte[] buffer = new byte[BUFFER_SIZE];
		final OutputStream os = getCipherOutputStream(output);
		int bytesRead = 0;

		try {
			while ((bytesRead = input.read(buffer)) > 0) {
				os.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			// close streams
			try { os.close(); } catch (Exception e) {};
			try { output.close(); } catch (Exception e) {};
			try { input.close(); } catch (Exception e) {};

		}
	}
	
	
	/**
	 * read encrypted text from standard input and write decrypted text to standard output
	 * @param key encryption key, unlimited length
	 * 
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public final void decrypt(final InputStream input, final OutputStream output) 
	throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

		final InputStream is = getCipherInputStream(input);

		final byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = 0;

		try {
			while ((bytesRead = is.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// close the streams
			try { output.close(); } catch (Exception e) {};
			try { input.close(); } catch (Exception e) {};

		}
	}
	
	

	
	/**
	 * get the cipher for encryption.  This method sets the various parameters.
	 * The key and initialization vector are given as arguments, 
	 * 
	 * @param opmode
	 * @param keyValue
	 * @return Cipher object ready for encryption
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * 
	 */
	private final Cipher getCipher(final int opmode, final byte[] keyValue, final byte[] initializationVector) 
	throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException  {

		final IvParameterSpec ivp = new IvParameterSpec(initializationVector);
		final Cipher result = Cipher.getInstance(CIPHERSPEC);
		final SecretKey key = new SecretKeySpec(keyValue, 0, KEYLEN, CIPHERSPEC.substring(0, CIPHERSPEC.indexOf("/")));
		result.init(opmode, key, ivp);

		return result;
	}

	

	
	/**
	 * creates a cipher input stream with an unlimited length key. The cipher is recursively generated
	 * in increments of the key length. For AES the key length is 16 bytes, so for example a 32 byte key will
	 * result in a doubly-encrypted stream, length of 64 bytes will result in a 4-level encrypted stream.
	 * 
	 * @param stream encrypted InpuStream to decrypt
	 * @param key encryption key
	 * @return decrypted InputStream
	 * @throws IOException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * 
	 */
	public final InputStream getCipherInputStream(final InputStream stream) 
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException  {

		InputStream cis = stream;
		final byte[] iv = new byte[KEYLEN];

		/*
		 * read the randomly created initialization vector so that it is unique to 
		 * this round.
		 */
		stream.read(iv);
		
		/* make new key array padded to be a multiple of key length */
		cis = new CipherInputStream(cis, getCipher(Cipher.DECRYPT_MODE,
				KEY.keySegment(0,KEYLEN), iv));

		for (int i = KEYLEN; i < KEY.size(); i+= KEYLEN) {
			/*
			 * read a new initialization vector for each round.
			 */
			final byte[] key = KEY.keySegment(i, KEYLEN);			
			stream.read(iv);
	
			cis = new CipherInputStream(cis,
					getCipher(Cipher.DECRYPT_MODE, key, iv));

		}
		
		return cis;
	}
	

	
	/**
	 * creates a cipher output stream with an unlimited length key. The cipher is recursively generated
	 * in increments of the key length. For AES the key length is 16 bytes, so for example a 32 byte key will
	 * result in a doubly-encrypted stream,  length of 64 bytes will result in a 4-level encrypted stream.
	 * 
	 * @param stream OutputStream to encrypt
	 * @param key encryption key
	 * @return encrypted OutputStream
	 * 
	 */
	public final OutputStream getCipherOutputStream(final OutputStream stream)  {
		
		OutputStream cos = stream;

		/*
		 * create a unique initialization vector with random bytes not related to
		 * the data or the keys. This ensures that the same file encrypted with the
		 * same key twice will have two different results.
		 */
		byte[] iv = seeds.generateSeed(KEYLEN);
	
		try {
			// write the unique IV to the encrypted file.
			stream.write(iv);

			cos = new CipherOutputStream(cos, getCipher(Cipher.ENCRYPT_MODE,
					KEY.keySegment(0, KEYLEN), iv));
			
			for (int i = KEYLEN; i < KEY.size(); i+= KEYLEN) {
				/*
				 * each round gets a new initializaiton vector, as well as a new key
				 * while the outer IV is in 'plain bytes' the others are encrypted
				 */
				final byte[] key = KEY.keySegment(i,KEYLEN);
				
				iv = seeds.generateSeed(KEYLEN);
				stream.write(iv);
				
				cos = new CipherOutputStream(cos, 
						getCipher(Cipher.ENCRYPT_MODE, key, iv));

			}
			
		} catch (Exception e) {
			System.err.println("getCipherOutputStream: " + e);
		}
		
		return cos;
	}
}
