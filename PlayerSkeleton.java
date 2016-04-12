
public class PlayerSkeleton {
	// 0.23958196 -0.31542331 -0.22554692 -0.65526497 -0.55367212 -0.23730191
	// 3.4181268101392694 -4.500158825082766 -3.2178882868487753 -9.348695305445199 -7.899265427351652 -3.3855972247263626
	double[] wValues = {3.4181268101392694, -4.500158825082766, -3.2178882868487753, -9.348695305445199, -7.899265427351652, -3.3855972247263626};

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
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s.getRowsCleared());
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

}
