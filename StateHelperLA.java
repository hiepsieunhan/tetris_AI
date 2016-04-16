public class StateHelperLA	 {

	private static int ROWS = State.ROWS;
	private static int COLS = State.COLS;

	private static final int ID_ROW_CLEARED = 0;
	private static final int ID_LANDING_HEIGHT = 1;
	private static final int ID_HORIZONTAL_ROUGHNESS = 2;
	private static final int ID_VERTICAL_ROUGHNESS = 3;
	private static final int ID_HOLE = 4;
	private static final int ID_WELL_COUNT = 5;

	public static int bestMove(State state, double[] wValues) {
		return bestMove(new CustomState(state), wValues);
	}

	public static int bestMove(CustomState state, double[] wValues) {
		int maxHeight = calMaxHeight(state.getTop());
		if (maxHeight <= 15) {
			return bestMoveNormal(state, wValues);
		}

		int legalMoves = state.legalMoves().length;
		int bestMove = 0;
		double maxRes = -Double.MAX_VALUE;
		for (int i = 0; i < legalMoves; i++) {
			CustomState cState = new CustomState(state);
			int rowsCleared = cState.makeMove(i);
			if (cState.hasLost()) continue;
			double cur = sumMove(cState, wValues) + rowsCleared * wValues[ID_ROW_CLEARED];
			if (Double.compare(cur, maxRes) > 0) {
				maxRes = cur;
				bestMove = i;
			}
		}
		return bestMove;
	}

	public static double sumMove(CustomState state, double[] wValues) {
		double res = 0;
		for (int i = 0; i < 7; i++) {
			state.setNextPiece(i);
			res += makeMove(state, bestMoveNormal(state, wValues), wValues);
		}
		return res/7;
	}

	public static int bestMoveNormal(State state, double[] wValues) {
		CustomState cState = new CustomState(state);
		return bestMoveNormal(cState, wValues);
	}

	public static int bestMoveNormal(CustomState state, double[] wValues) {
		int legalMoves = state.legalMoves().length;
		int bestMove = 0;
		double maxRes = -Double.MAX_VALUE;
		for (int i = 0; i < legalMoves; i++) {
			double cur = makeMove(state, i, wValues);
			if (Double.compare(cur, maxRes) > 0) {
				maxRes = cur;
				bestMove = i;
			}
		}
		return bestMove;
	}

	public static double makeMove(CustomState state, int move, double[] wValues) {
		int nextPiece = state.getNextPiece();
		int[] lMove = state.legalMoves()[move];
		return makeMove(state, lMove[State.ORIENT], lMove[State.SLOT], wValues);
	}


	public static double makeMove(CustomState state, int orient, int slot, double[] wValues) {

		// get from state object

		int landingHeight = 0;

		int[] oriTop = state.getTop();
		int[][] oriField = state.getField();
		int[] top = new int[COLS];
		int[][] field = new int[ROWS][COLS];
		// copy content of top[]
		for (int i = 0; i < COLS; i++) {
			top[i] = oriTop[i];
		}
		// copy content of field[][]
		for (int i = 0; i < ROWS; i++)
			for (int j = 0; j < COLS; j++) {
				field[i][j] = oriField[i][j];
			}

		int[][][] pBottom = state.getpBottom();
		int[][][] pTop = state.getpTop();
		int[][] pWidth = state.getpWidth();
		int[][] pHeight = state.getpHeight();
		int nextPiece = state.getNextPiece();

		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			return -1000000000000L;
		}

		landingHeight = height + (pHeight[nextPiece][orient] - 1) / 2;


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = 1;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}

		return cal(rowsCleared, landingHeight, field, top, wValues);
	}


	// TODO: implement this method
	public static double cal(int rowsCleared, int landingHeight, int[][] field, int[] top, double[] wValues) {
		double res = wValues[ID_ROW_CLEARED] * rowsCleared
							 + wValues[ID_LANDING_HEIGHT] * landingHeight
							 + wValues[ID_HOLE] * calHole(field, top)
							 + wValues[ID_HORIZONTAL_ROUGHNESS] * calHorizontalRoughness(field, top)
							 + wValues[ID_VERTICAL_ROUGHNESS] * calVerticalRoughness(field, top)
							 + wValues[ID_WELL_COUNT] * calWellCount(field, top);
		return res;
	}

	public static int calHole(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			int ok = 0;
			for (int i = top[j] - 1; i >= 0; i--) if (field[i][j] == 0) {
				res += ok;
			} else {
				ok = 1;
			}
		}
		return res;
	}

	public static int calHorizontalRoughness(int[][] field, int[] top) {
		int res = 0;
		for (int i = 0; i < ROWS; i++) {
			int lastCell = 1;
			for (int j = 0; j < COLS; j++) {
				int cur = field[i][j] > 0 ? 1 : 0;
				if (cur != lastCell) {
					res++;
				}
				lastCell = cur;
			}
			if (lastCell == 0) res++;
		}
		return res;
	}

	public static int calVerticalRoughness(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			int lastCell = 1;
			for (int i = 0; i <= top[j]; i++) {
				int cur = field[i][j] > 0 ? 1 : 0;
				if (cur != lastCell) {
					res++;
				}
				lastCell = cur;
			}
		}
		return res;
	}

	public static int calWellCount(int[][] field, int[] top) {
		int res = 0;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) if (field[i][j] == 0) {
				int leftCell = j > 0 ? field[i][j - 1] : 1;
				int rightCell = j < COLS - 1 ? field[i][j + 1] : 1;
				if (leftCell > 0 && rightCell > 0) {
					res++;
				}
			}
		}
		return res;
	}

	public static int calMaxHeight(int[] top) {
		int res = 0;
		for (int i = 0; i < COLS; i++) {
			res = Math.max(res, top[i]);
		}
		return res;
	}

}

