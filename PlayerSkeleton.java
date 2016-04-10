
public class PlayerSkeleton {
	double[] wValues = {0.1871, -0.3838, -0.1993, -0.092, -0.1759, -0.3865 ,-0.1519 ,-0.4035, -0.1729, -0.1662, -0.1233, -0.2532, -0.344 ,-0.3843};

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
