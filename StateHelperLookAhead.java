public class StateHelperLookAhead {

	private static int ROWS = State.ROWS;
	private static int COLS = State.COLS;
	private static int N_PIECES = State.N_PIECES;

	private static final int ID_ROW_CLEARED = 0;
	private static final int ID_SUM_HEIGHT = 1;
	private static final int ID_ROUGHNESS = 2;
	private static final int ID_FILLED_SPOT_COUNT = 3;
	private static final int ID_WEIGHTED_FILLED_SPOT_COUNT = 4;
	private static final int ID_MAX_HEIGHT = 5;
	private static final int ID_HEIGHT_DELTA = 6;
	private static final int ID_HOLE = 7;
	private static final int ID_DEPTH_WEIGHTED_HOLE = 8;
	private static final int ID_WEIGHTED_HOLE = 9;
	private static final int ID_DEEPEST_HOLE = 10;
	private static final int ID_WELL_COUNT = 11;
	private static final int ID_HORIZONTAL_ROUGHNESS = 12;
	private static final int ID_VERTICAL_ROUGHNESS = 13;


	public static int bestMove(State state, double[] wValues) {
		int[][] legalMoves = state.legalMoves();
		int bestMove = 0;
		double maxRes = -Double.MAX_VALUE;
		for (int i = 0; i < legalMoves.length; i++) {
			double curExp = 0;
			StateLookAhead curState = new StateLookAhead(state);
			curState.makeMove(i);
			if (curState.hasLost()) {
				continue;
			}
			for (int j = 0; j < N_PIECES; j++) {
				curState.setNextPiece(j);
				curExp += expectedMoveLookAhead_1(curState, wValues);
			}
			if (Double.compare(curExp, maxRes) > 0) {
				maxRes = curExp;
				bestMove = i;
			}
		}
		return bestMove;
	}

	public static double expectedMoveLookAhead_1(StateLookAhead state, double[] wValues) {
		int[][] legalMoves = state.legalMoves();
		double expectedValue = 0;
		for (int i = 0; i < legalMoves.length; i++) {
			double cur = makeMove(state, i, wValues);
			expectedValue += cur;
		}
		return expectedValue;
	}

	public static double makeMove(StateLookAhead state, int move, double[] wValues) {
		int nextPiece = state.getNextPiece();
		int[] lMove = state.legalMoves()[move];
		return makeMove(state, lMove[State.ORIENT], lMove[State.SLOT], wValues);
	}


	/* 	returns false if you lose - true otherwise
		simulate makeMove method
		value return is expected value base on the providec value
	*/
	public static double makeMove(StateLookAhead state, int orient, int slot, double[] wValues) {

		// get from state object

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
			return -1000000000;
		}

		
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
	
		return cal(rowsCleared, field, top, wValues);
	}


	// TODO: implement this method
	public static double cal(int rowsCleared, int[][] field, int[] top, double[] wValues) {
		double res = wValues[ID_ROW_CLEARED] * rowsCleared
				+ wValues[ID_SUM_HEIGHT] * calSumHeight(top)
				+ wValues[ID_ROUGHNESS] * calRoughness(top)
				+ wValues[ID_FILLED_SPOT_COUNT] * calFilledSpotCount(field, top)
				+ wValues[ID_WEIGHTED_FILLED_SPOT_COUNT] * calWeightedFilledSpotCount(field, top)
				+ wValues[ID_MAX_HEIGHT] * calMaxHeight(top)
				+ wValues[ID_HEIGHT_DELTA] * calHeightDelta(top)
				+ wValues[ID_HOLE] * calHole(field, top)
				+ wValues[ID_DEPTH_WEIGHTED_HOLE] * calDepthWeightedHole(field, top)
				+ wValues[ID_WEIGHTED_HOLE] * calWeightedHole(field, top)
				+ wValues[ID_DEEPEST_HOLE] * calDeepestHole(field, top)
				+ wValues[ID_WELL_COUNT] * calWellCount(field, top)
				+ wValues[ID_HORIZONTAL_ROUGHNESS] * calHorizontalRoughness(field, top)
				+ wValues[ID_VERTICAL_ROUGHNESS] * calVerticalRoughness(field, top);
		return res;
	}

	public static int calSumHeight(int[] top) {
		int res = 0;
		for (int i = 0; i < COLS; i++) {
			res += top[i];
		}
		return res;
	}

	public static int calRoughness(int[] top) {
		int res = 0;
		for (int i = 0; i < COLS - 1; i++) {
			res += Math.abs(top[i] - top[i + 1]);
		}
		//System.out.println("count Bump " + res);		
		return res;
	}

	public static int calFilledSpotCount(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = 0; i < top[j]; i++) if (field[i][j] > 0) {
				res++;
			}
		}
		return res;
	}

	public static int calWeightedFilledSpotCount(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = 0; i < top[j]; i++) if (field[i][j] > 0) {
				res += (i + 1);
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

	public static int calHeightDelta(int[] top) {
		int Max = 0, Min = ROWS + 1;
		for (int i = 0; i < COLS; i++) {
			Max = Math.max(Max, top[i]);
			Min = Math.min(Min, top[i]);
		}
		return Max - Min;
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

	public static int calDepthWeightedHole(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			int depth = 0;
			for (int i = top[j] - 1; i >= 0; i--) {
				depth++;
				if (field[i][j] == 0) {
					res += depth;
				}
			}
		}	 
		return res;
	}

	public static int calWeightedHole(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = top[j] - 1; i >= 0; i--) if (field[i][j] == 0) {
				res += i + 1;
			}
		}
		return res;
	}

	public static int calDeepestHole(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = 0; i < top[j]; i++) if (field[i][j] == 0) {
				res = Math.max(top[j] - i, res);
				break;
			}
		}
		return res;
	}

	public static int calWellCount(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = 0; i < top[j] - 2; i++) if (field[i][j] == 0) {
				res++;
			}
		}
		return res;
	}

	public static int calHorizontalRoughness(int[][] field, int[] top) {
		int res = 0;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 1; j < COLS; j++) if (field[i][j] != field[i][j - 1]) {
				res++;
			}
		}
		return res;
	}

	public static int calVerticalRoughness(int[][] field, int[] top) {
		int res = 0;
		for (int j = 0; j < COLS; j++) {
			for (int i = 1; i < top[j]; i++) if (field[i][j] != field[i - 1][j]) {
				res++;
			}
		}
		return res;
	}

}