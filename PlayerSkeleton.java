
public class PlayerSkeleton {
	double[] wValues = {0.1994, -0.3724, -0.3289, -0.2073, -0.0559, 0.0153, 0.2015, -0.2133, -0.05, -0.1489, -0.0478, -0.2172, -0.4931, -0.5133};

	public int pickMove(State s, int[][] legalMoves) {
		return StateHelper.bestMove(s, wValues);
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
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s.getRowsCleared());
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
