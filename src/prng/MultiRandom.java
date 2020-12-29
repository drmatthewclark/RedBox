package prng;



import java.security.SecureRandom;
import java.util.Arrays;


/**
 * Multirandom uses a variable number of random number generators, and for each call for a random number one of the generators
 * is randomly selected to generate the number. This allows the keyspace to be unlimited bits; a separate random number generator is
 * created for each long value in the initialization array. Each generator then maintains its own separate state.  
 * 
 * The method may be cryptographically secure. Since the last generated random number is used to select the generator for the next number, 
 * in order to compute the next number one has to know the state of each generator, as well as the entire stream of past numbers to know
 * which generator will be used for the next value. This is because internal state used to select the next generator is XOR'd with the last random
 * number every time one is generated.
 * 
 * If a string is used to initialize the random number generator, a separate generator is created for each 8 bytes of 
 * the string. The string byte array is converted to a array of long values. Thus the variable amount of state held increases
 * the entropy of the random numbers generated as longer keys are used.
 * 
 * 
 * @author Matthew Clark
 *
 */
public class MultiRandom extends ExtendedRandom {

	
	/*
	 * mask for absolute value of an integer
	 */
	final int signMask = 0x7FFFFFFF;

	/*
	 * array of random number generators.  
	 */
	private ExtendedRandom[] sources = null;
	
	/*

	
	/*
	 * default classes to use to create random numbers.  It includes the very popular MersenneTwister, and extension of the Java algorithm to
	 * use 64 instead of 48 bits, and the very quick XORShift algorithm.  The use of several algorithms may increase the quality
	 * of the result, although it also works fine if all of the random number generators are the same class since they should have
	 * different initialization values.
	 * 
	 */
	@SuppressWarnings("unchecked")

	private static final Class<? extends ExtendedRandom>[] defaultRandomGeneratorClasses 
		= new Class[] {
				Random64.class, 
				MersenneTwister.class, 
				XORShift.class, 
				DigestRandom.class,
				MultiplyWithCarry.class,
				BlumBlumShub.class,
				CBRNG.class
				};

	/*
	 * internal state. used to select the next random number generator to use
	 */
	private transient int state = -1;
	
	private ExtendedRandom selectSource() {
		
		int index = (state % sources.length);
		ExtendedRandom source = sources[index];
		state ^= source.nextInt() & signMask;
		return sources[index];
		
	}

	/**
	 * default constructor. Uses DEFAULT_SOURCES random number generators.
	 */
	public MultiRandom() {
		
		/*
		 * initialize random generators with current time and other semi-random initialization values.
		 * Leverage secureRandom's reading of the system source of entropy as part of the initialization.
		 */
		
		this(new SecureRandom().generateSeed(32), 
				defaultRandomGeneratorClasses);

	}
	
	
	/**
	 * Master constructor takes key and set of classes to use to generate random numbers
	 * 
	 * @param randoms
	 */

	public MultiRandom(byte[] seeds, Class<? extends ExtendedRandom>... randoms) {
		
		sources = new ExtendedRandom[randoms.length];
		for (int i = 0; i < randoms.length; i++) {
			try {
				sources[i] = (ExtendedRandom) randoms[i].newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.exit(33);
			} catch (IllegalAccessException e) {
				e.printStackTrace();

			}
		}

		setSeed(seeds);
	}
	
	

	
	/**
	 * generate sequence based on the string given as argument. It uses unlimited bits of the string by generating
	 * a different random number generator seeded by a long created by every 8 bytes of the string.
	 * 
	 * @param key byte array used as key.
	 */
	public MultiRandom(final byte[] key) {
		this(key, defaultRandomGeneratorClasses);
	}
	

	/**
	 * Random compatible constructor
	 * 
	 * @param seed long value for seed
	 */
	public MultiRandom(long seed) {
		
		this(longToByteArray(seed), defaultRandomGeneratorClasses);
	}
	


	/**
	 * set the seed for the random sources.  This may either reset the state or
	 * add to the state depending on the random source.
	 * 
	 * @param seed byte[] array with bytes for seed
	 */
	public void setSeed(byte[] seed) {
		
		state ^= Arrays.hashCode(seed);
		
		for (ExtendedRandom source : sources) {
			source.setSeed(seed);
		}
	}
	
	
	/**
	 * seed seed with a single long
	 * @param seed set the seed
	 * 
	 */
	public void setSeed(final long seed) {
		
		/*
		 * select one of the sources to generate
		 * seeds for the other sources
		 */
		final ExtendedRandom rand = selectSource();
		
		/*
		 * set the seeds
		 */
		for (ExtendedRandom source : sources) {
			source.setSeed(rand.nextLong());
		}
		super.setSeed(seed);
	}
	
	/**
	 * provides an integer from one of the random sources, chosen using
	 * the state variable which is pseudorandom itself
	 */
	public final int nextInt() {
		
		return selectSource().nextInt();

	}
	
}
