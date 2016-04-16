/*
	Required StateHelperLA.java and CustomState.java to compile (see README.txt 
	for more detail)
*/

public class PlayerSkeleton {
	double[] wValues = {0.43972701, -0.32620246, -0.2225565, -0.54566604, -0.40189714, -0.4375248};

	public int pickMove(State s, int[][] legalMoves) {
		return StateHelperLA.bestMove(s, wValues);
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println(s.getRowsCleared());
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

}
