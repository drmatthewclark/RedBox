package prng;

/**
 * Blum Blum Shub algorithm to generate random number sequences
 * 
 * x(n+1) = x(n)^2 mod M  where M is a product of two large prime numbers
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
public class BlumBlumShub extends ExtendedRandom {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3752716805248928524L;
	// The 9,999,979,999th prime is 252,097,271,807.
	// this does not use all 64 bits so the next method will use the lower 32 bits.
	private static final long M = 252097271807L;
	

	BlumBlumShub() {
		
	}
	/**
	 * provide the next random bits
	 * 
	 * @param bits - requested bits, ranges from 1 to 32
	 */
	protected synchronized final int next(int bits) {

		seed = (seed * seed) % M;
		return (int) (seed >>> (32 - bits));
	}

}
