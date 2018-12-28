package prng;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * CBRNG - counter-based random number generated using AES encryption. Adds a counter to each run
 * and encrypts so that the byte sequence is the output of AES encryption.  From the idea of D.E. Shaw
 * group.
 * 
 *
 *   copyright 2019 Matthew Clark
 
     This file is part of TrueRandom.

    TrueRandom is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TrueRandom is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TrueRandom.  If not, see <https://www.gnu.org/licenses/>.

 * @author CLARKM
 *
 */
public class CBRNG extends ExtendedRandom {


	private static final long serialVersionUID = -2718039215048818753L;
	/*
	 * specification of cipher used for encryption
	 */
	private static final String CIPHERSPEC = "AES/CBC/PKCS5Padding";
	/*
	 * depending on the Java deliverable this could be larger, but for most 
	 * default installations 16 bytes is all the keysize you get. This is the key length for
	 * each round of encryption.  
	 */
	private static final int KEYLEN = 16;
	private Cipher cipher;
	
	/* use the standard Random class to increment counter */
	private long counter = Long.MIN_VALUE;
	
	private byte[] seed = new byte[KEYLEN];
	
	// static IV. this shouldn't be a problem for this application. The values
	// are changed by the setSeed function.
	private static final byte[] initializationVector = 
			{ -32, 31, 0, 54, 59, 120, 3, -17, 7, 9, 67, 45, -117, 53, -9, -107 };
	
	
	CBRNG() {
		
	}

	
	/**
	 * constructor with a seed and initialization vector
	 * 
	 * @param seed
	 * @param initializationVector
	 */
	CBRNG(byte[] seed, byte[] initializationVector) {
		
		try {
			cipher = getCipher(Cipher.ENCRYPT_MODE, seed, initializationVector);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * set seed from long
	 * @param long value for seed
	 * 
	 */
	public void setSeed(long newSeed) {
		setSeed(longToByteArray(newSeed));
	}
	
	@Override
	void setSeed(byte[] newSeed) {
		
		if (seed == null) seed = new byte[KEYLEN];
		System.arraycopy(newSeed, 0, seed, 0, Math.min(seed.length, newSeed.length));
		
		// modify IV to be not equal for each reseed.
		initializationVector[15] = newSeed[0];
		
		try {
			// reinitialize cipher
			cipher = getCipher(Cipher.ENCRYPT_MODE, seed, initializationVector);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(501);
		} 
	}



	@Override
	protected synchronized int next(int bits) {
		
		byte[] result = new byte[8]; 
		
		try {
			// use the seed as part of the encryption
			cipher.update(seed);
			// now add 8 bytes to the seed to create a unique result
			cipher.update(longToByteArray(counter++));
			// encrypt the total byte set.
			result = cipher.doFinal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(10);
		} 
		
		return (int) (bytesToLong(result) >> (64 - bits));
	}

}
