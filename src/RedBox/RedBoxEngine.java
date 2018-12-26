package RedBox;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import cipher.CipherStream;
import random.RandomCipherOutputStream;
import random.RandomCipherInputStream;




public class RedBoxEngine {
	
	/*
	 * name of environment variable used for encryption symmetric key
	 */
	private final static String KeyEnvironmentVariable = "EKEY";

	/*
	 * error message for incorrect  invocation
	 */
	final static String errorMessage = "usage: -e <password> to encrypt,  -d <password> to decrypt. "
			+	"\nReads from standard input and writes to standard output\n"
			+   "set environment variables EKEY for the key\n";

	
	public enum mode { ENCRYPT, DECRYPT };
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		
		EncryptionKey KEY = null;
		
		final mode command = args[0].startsWith("-e") ? mode.ENCRYPT : mode.DECRYPT;
		
		/*
		 * get KEY from environment, if set.
		 */
		if (args.length > 1) {
			
			KEY = new EncryptionKey(args[1]); // this will make more encoding independent
			
		} else if (System.getenv(KeyEnvironmentVariable) != null) {
			
			KEY = new EncryptionKey(System.getenv(KeyEnvironmentVariable).getBytes());
		
		} else if (args.length == 0 || (KEY == null && args.length < 2)) {
			
			System.out.println(errorMessage);
			System.exit(1);
		}
		
		
		new RedBoxEngine().go(command, KEY, System.in, System.out);
	}
	

	/**
	 * perform the encryption
	 * 
	 * @param args first parameter is the option -e or -d to encrypt or decrypt. 
	 * 	The second parameter is the encryption key.
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	void go(final mode command, final EncryptionKey KEY,
			final InputStream in, final OutputStream out) 
					throws UnsupportedEncodingException, InvalidKeyException,
					NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException  {

		try {
			final EncryptionKey aesKey = new EncryptionKey(KEY);
			final CipherStream aesCipher = new CipherStream("AES/CFB/NoPadding", 16, aesKey);
			final EncryptionKey blowfishKey = new EncryptionKey(aesKey);
			final CipherStream blowfishCipher = new CipherStream("Blowfish/CFB/NoPadding", 8, blowfishKey);
			final EncryptionKey randomKey = new EncryptionKey(blowfishKey);

			if (command.equals(mode.ENCRYPT)) {

				final RandomCipherOutputStream xorOutputStream = new RandomCipherOutputStream(out, randomKey);
				final OutputStream bf = blowfishCipher.getCipherOutputStream(xorOutputStream);
				aesCipher.encrypt(in, bf);

				xorOutputStream.close();
				bf.close();

			} else if (command.equals(mode.DECRYPT)) {

				final RandomCipherInputStream xorInputStream = new RandomCipherInputStream(in, randomKey);
				final InputStream bf = blowfishCipher.getCipherInputStream(xorInputStream);
				aesCipher.decrypt(bf, out);

				xorInputStream.close();
				bf.close();
			}

			in.close();
			out.close();

		} catch (java.io.IOException io) {
			io.printStackTrace();
		} 
	}
}
