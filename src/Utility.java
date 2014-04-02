import java.util.Random;

public class Utility {
	public static Integer[] generateRandomNumber(int limit, int range) {
		// note a single Random object is reused here
		Integer[] integerArray = new Integer[limit];
		Random randomGenerator = new Random();
		for (int i = 0; i < limit; ++i) {
			integerArray[i] = randomGenerator.nextInt(range);
			// System.out.println(integerArray[i]);
		}
		return integerArray;

	}

	// checks whether an int is prime or not.
	public static boolean isPrime(Integer n) {
		// check if n is a multiple of 2
		if (n % 2 == 0)
			return false;
		// if not, then just check the odds
		for (int i = 3; i * i <= n; i += 2) {
			if (n % i == 0)
				return false;
		}
		return true;
	}
}
