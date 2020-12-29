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
	
	
	CBRNG() {
		
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
		int keylen = Math.min(keyValue.length, KEYLEN);
		final SecretKey key = new SecretKeySpec(keyValue, 0, keylen, CIPHERSPEC.substring(0, CIPHERSPEC.indexOf("/")));
		result.init(opmode, key, ivp);

		return result;
	}



	@Override
	public int nextInt() {
		
		byte[] result = new byte[8]; 
		
		try {
			if (cipher == null) {
				byte[] iv = new byte[16];
				System.arraycopy(getSeed(), 0, iv, 0, Math.min(getSeed().length, 16));
				cipher = getCipher(Cipher.ENCRYPT_MODE, iv, iv);
			}
			// use the seed as part of the encryption
			cipher.update(getSeed());
			// now add 8 bytes to the seed to create a unique result
			cipher.update(longToByteArray(counter++));
			// encrypt the total byte set.
			result = cipher.doFinal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(10);
		} 
		
		return bytesToInt(result);
	}

}
