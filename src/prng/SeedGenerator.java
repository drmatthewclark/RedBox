package prng;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;


public class SeedGenerator  {

	private static DigestRandom random = null;
	private static SecureRandom srand = null;	
	
	/**
	 * generate a key using fairly random input. This is faster than the random device as it
	 * doesn't run out of entropy
	 * 
	 * @param size number of bytes for key
	 * @return random bytes
	 */
	public final byte[] generateSeed(final int size) {
		
		// keep using the same source to insure a differnt value every time
		// this is called.
		if (random == null) {
			random = new DigestRandom();
		}

		/*
 		* generate entropy from hardware as well. This is limited and
 		* will block if we try to read a lot from it.
 		*/	
		if (srand == null) {
			srand = new SecureRandom();
			random.setSeed(srand.generateSeed(16));
		}
	
		random.setSeed(getEntropy());
		final byte[] result = new byte[size];
		random.nextBytes(result);
		return result;
	}
	
	
	/**
	 * get some random entropy items from the environment.
	 * 
	 * @return random bytes for entropy.
	 */
	private final byte[] getEntropy() {

		final StringBuilder result = new StringBuilder();
		result.append(System.nanoTime());
		/*
		 * use free space, which should change often
		 */
		final File[] root = File.listRoots();

		for (File file : root) {
			result.append(file.getUsableSpace());
			if (random.nextBoolean()) {
				result.append(file.getAbsolutePath());
				result.append(random.nextLong());
				result.append(file.lastModified());
			}
		}

		result.append(this.toString());

		final ThreadMXBean threadMXBean = 
				(ThreadMXBean) ManagementFactory.getThreadMXBean();

		final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

		for (final Method beanMethod : runtimeMXBean.getClass().getMethods()) {
			if (beanMethod.getAnnotatedParameterTypes().length == 0) {
				try {
					beanMethod.setAccessible(true);
					final Object item = beanMethod.invoke(runtimeMXBean, (Object[])null);
					result.append(item.toString());
				} catch (Exception e) {}
			}
		}

		for (final Method beanMethod : threadMXBean.getClass().getMethods()) {
			if (beanMethod.getAnnotatedParameterTypes().length == 0) {
				try {
					beanMethod.setAccessible(true);
					final Object item  = beanMethod.invoke(threadMXBean, (Object[])null);
					result.append(item.toString());
				} catch (Exception e) {}
			}
		}

		return result.toString().getBytes(StandardCharsets.UTF_8);
	}
}
