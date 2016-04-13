import java.util.Scanner;

public class Test {

	public static int play(double[] w) {
		CustomState s = new CustomState();
		//s.setSeed(29);
		int step = 0;
		while (!s.hasLost()) {
			s.makeMove(StateHelperLA.bestMove(s, w));
			step++;
			if (step % 100000 == 0) System.out.print(s.getRowsCleared() + " ");
		}
		System.out.println();
		return s.getRowsCleared();
	}

	public static void main(String[] args) {

		int noFactor = Search.noFactor;

		double[] w = new double[noFactor];
		Scanner sc = new Scanner(System.in);
		for (int i = 0; i < noFactor; i++) w[i] = sc.nextDouble();
		int Min = 100000000;
		int Max = 0;
		int sum = 0;
		int sampleSize = 10;
		for (int i = 0; i < sampleSize; i++) {
			int cur = play(w);
			System.out.println("turn " + (i + 1) + ": " + cur);
			sum += cur;
			Min = Math.min(cur, Min);
			Max = Math.max(cur, Max);
		}
		System.out.println("-----------------------------------------------");
		System.out.println("mean = " + sum / sampleSize);
		System.out.println("min = " + Min);
		System.out.println("max = " + Max);
		System.out.println("-----------------------------------------------");
	}
}
