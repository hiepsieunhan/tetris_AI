import java.util.Scanner;
import java.io.*;

public class TestAll {
	
	public static int play(double[] w) {
		State s = new State();
		while (!s.hasLost()) {
			s.makeMove(StateHelper.bestMove(s, w));
		}			
		//System.out.println(s.getRowsCleared());
		return s.getRowsCleared();
	}

	public static void test(double[] w, int testCase) {
		int Min = 10000000;
		int Max = 0;
		int sum = 0;
		int sampleSize = 200;
		for (int i = 0; i < sampleSize; i++) {
			int cur = play(w);
			sum += cur;
			Min = Math.min(cur, Min);
			Max = Math.max(cur, Max);
		}
		System.out.println("-------------------- Case " + testCase + "------------------------");
		System.out.println("mean = " + sum / sampleSize);
		System.out.println("min = " + Min);
		System.out.println("max = " + Max);
		System.out.println("-----------------------------------------------");
	}
	
	public static void main(String[] args) {

		int noFactor = Search.noFactor;

		Scanner sc = new Scanner(System.in);
		String fileName = sc.next();
		BufferedReader reader = null;
		try {
			FileReader fr = new FileReader(fileName);
			reader = new BufferedReader(fr);
			String line = null;
			int testCase = 0;
			while ((line = reader.readLine()) != null) {
				String params[] = line.split(" ");
				double[] w = new double[noFactor];
				for (int i = 0; i < noFactor; i++) w[i] = Double.parseDouble(params[i]);
				test(w, ++testCase);
			}
		} catch (IOException ex) {

		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}
}