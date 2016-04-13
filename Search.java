import java.util.ArrayList;
import java.io.*;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;


public class Search {

	private static final int MYTHREADS = 30;

	public static final int noFactor = 6;
	public static final String fileName = "result.txt";
	public static final String logFileName = "log.txt";

	private static final int population = 100;
	private static final int[] signs = {1, -1, -1, -1, -1, -1};
	private static final int[] seeds = {6, 8, 19, 29, 23};

	private static final int FITNESS_TRIALS = 5;

	private ArrayList<Strategy> myList;
	private ArrayList<Strategy> myQueueList;
	private int counter;

	public static void main(String[] args) {
		Search s = new Search();
		s.search();
	}

	private void search() {
		counter = 0;
		initVectors();
		int crossOverNo = (int)(population/10.0);
		int mutateNo = (int)(population/10.0);
		while (true) {
			ArrayList<Strategy> crossOverList = crossOver(crossOverNo);
			ArrayList<Strategy> mutateList = mutate(mutateNo);
			myQueueList = new ArrayList<>();
			myQueueList.addAll(crossOverList);
			myQueueList.addAll(mutateList);

			calcalateFitness();

			deleteOverflow();
			updateVectors();
			counter++;
			log(myList.get(0));
		}
	}

	private int random(int s) {
		return (int)(Math.random() * s);
	}

	private void log(Strategy s) {

		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(logFileName, true), "utf-8"));

		    Date date = new Date();
		    writer.write(date.toString() + "\n");
		    writer.write("turn " + counter + "\n");
		    writer.write((s.getFitness() / FITNESS_TRIALS) + "\n");
		    double[] w = s.getW();
		    for (int i = 0; i < w.length; i++) {
		    	writer.write(String.valueOf((int)(w[i] * 100000000)/100000000.0) + " ");
		    }
		    writer.write("\n");
		    writer.write("--------------------------\n");
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}

	private void calcalateFitness() {
		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
		int dividedSize = (int)Math.ceil(myQueueList.size() * 1.0 / MYTHREADS);
		int cur = 0;
		for (int i = 0; i < MYTHREADS; i++) {
			MyCalFitness curThread = new MyCalFitness(i + 1);
			for (int j = 0; j < dividedSize; j++) {
				if (cur >= myQueueList.size()) break;
				curThread.addStrategy(myQueueList.get(cur));
				cur++;
			}
			executor.execute(curThread);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		for (Strategy s : myQueueList) {
			myList.add(s);
		}
	}

	private static int fitness(double[] wValues) {
		int res = 0, score;
		for (int i = 0; i < FITNESS_TRIALS; i++) {
			CustomState s = new CustomState();
			s.setSeed(seeds[i]);
			while (!s.hasLost()) {
				s.makeMove(StateHelperLA.bestMove(s, wValues));
			}
			int point = s.getRowsCleared();
			res += point;
		}
		return res;
	}

	private double[] modify(double[] wValues, int probMofidy) {
		double[] w = new double[noFactor];
		for (int i = 0; i < noFactor; i++) {
			w[i] = wValues[i];
		}
		if (random(100) >= probMofidy) return w;
		int position = random(wValues.length);
		double maxDelta = 0.2 / ((int)(counter / 200) + 1);
		double delta = Math.random() * maxDelta * 2 - maxDelta;
		if (random(2) == 0) delta = -delta;
		w[position] += delta;
		return w;
	}

	private double[] combine(Strategy s1, Strategy s2) {
		double[] w = new double[noFactor];
		double[] wS1 = s1.getW();
		double[] wS2 = s2.getW();
		int fitnessS1 = s1.getFitness();
		int fitnessS2 = s2.getFitness();
		for (int i = 0; i < noFactor; i++) {
			w[i] = wS1[i] * fitnessS1 + wS2[i] * fitnessS2;
		}
		return normalize(w);
	}

	private double[] normalize(double[] wValues) {
		int n = wValues.length;
		double[] w = new double[n];
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += wValues[i] * wValues[i];
		}
		sum = Math.sqrt(sum);
		for (int i = 0; i < n; i++) {
			w[i] = wValues[i] / sum;
		}
		return w;
	}

	private double[] randomW() {
		double[] w = new double[noFactor];
		for (int i = 0; i < noFactor; i++) {
			w[i] = (random(10) + 1) * signs[i];
		}
		return normalize(w);
	}

	private ArrayList<Strategy> randomSelect(int s) {
		ArrayList<Strategy> res = new ArrayList<>();
		int[] ids = new int[myList.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = i;
		}
		for (int i = 0; i < ids.length; i++) {
			int j = random(ids.length);
			int temp = ids[i];
			ids[i] = ids[j];
			ids[j] = temp;
		}
		for (int i = 0; i < Math.min(ids.length, s); i++) {
			res.add(myList.get(ids[i]));
		}
		Collections.sort(res);
		return res;
	}

	private ArrayList<Strategy> crossOver(int s) {
		int sampleSize = (int)(population/10.0);
		ArrayList<Strategy> res = new ArrayList<>();
		while (--s >= 0) {
			//double[] newW = combine(randomList.get(0), randomList.get(1));
			double[] newW;
			int firstId = random(population);
			int secondId = random(population);
			if (secondId == firstId) {
				if (firstId == 0) secondId++;
				else secondId--;
			}
			newW = combine(myList.get(firstId), myList.get(secondId));
			newW = normalize(modify(normalize(newW), 10));
			int fitness = fitness(newW);
			res.add(new Strategy(newW, fitness));
		}
		return res;
	}

	private ArrayList<Strategy> mutate(int s) {
		ArrayList<Strategy> randomList = randomSelect(s);
		ArrayList<Strategy> mutatedList = new ArrayList<>();
		for (int i = 0; i < s; i++) {
			double[] w = normalize(modify(randomList.get(i).getW(), 100));
			int fitness = fitness(w);
			mutatedList.add(new Strategy(w, fitness));
		}
		return mutatedList;
	}

	private void deleteOverflow() {
		Collections.sort(myList);
		for (int i = myList.size() - 1; i >= population; i--) {
			myList.remove(i);
		}
	}

	private void initVectors() {
		myList = new ArrayList<>();
		myQueueList = new ArrayList<>();
		BufferedReader reader = null;
		try {
			FileReader fr = new FileReader(fileName);
			reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String params[] = line.split(" ");
				double[] w = new double[noFactor];
				for (int i = 0; i < noFactor; i++) w[i] = Double.parseDouble(params[i]);
				w = normalize(w);
				if (params.length == noFactor) {
					myQueueList.add(new Strategy(w, -1));
				} else {
					myList.add(new Strategy(w, Integer.parseInt(params[noFactor])));
				}

			}
		} catch (IOException ex) {

		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}

		// create to have enough population
		while (myQueueList.size() < population) {
			double[] w = randomW();
			myQueueList.add(new Strategy(w, -1));
		}
		calcalateFitness();
	}

	private void updateVectors() {
		Collections.sort(myList);
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(fileName), "utf-8"));

		    for (Strategy s : myList) {
		    	double[] w = s.getW();
		    	for (int i = 0; i < w.length; i++) {
		    		writer.write(String.valueOf((int)(w[i] * 100000000)/100000000.0) + " ");
		    	}
		    	writer.write(s.getFitness() + "\n");
		    }

		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}

	class MyCalFitness implements Runnable {
		ArrayList<Strategy> list;
		int index;
		public MyCalFitness(int index) {
			list = new ArrayList<>();
			this.index = index;
		}

		public void addStrategy(Strategy s) {
			list.add(s);
		}

		@Override
		public void run() {
			try {
				for (Strategy s : list) {
					int fitness = Search.fitness(s.getW());
					System.out.println(index + " " + fitness);
					s.setFitness(fitness);
				}
			} catch (Exception e) {
			}
		}
	}

}

class Strategy implements Comparable<Strategy> {
	private int fitness;
	private double[] w;
	public Strategy(double[] w, int fitness) {
		this.w = w;
		this.fitness = fitness;
	}

	public void setFitness(int fitness) {
		this.fitness = fitness;
	}

	public void setW(double[] w) {
		this.w = w;
	}

	public int getFitness() {
		return this.fitness;
	}

	public double[] getW() {
		return this.w;
	}

	public int compareTo(Strategy other) {
		// descending order.
		return other.fitness - this.fitness;
	}
}
