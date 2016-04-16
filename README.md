# TetrisAI
Create AI for Tetris game by applying machine learning algorithm

- Function of each file:

    + CustomState.java: just a copy of State.java (with added some functions) to help for moving piece in look-ahead since we can not modify State.java.
    + StateHelper.java: contains helper functions for finding best move.
    + StateHelperLA.java: contains helper functions for finding best move with applying look-ahead.
    + Search.java: contains functions for learning (finding best vectors).

- To visualize:
    + java PlayerSkeleton

- To learn:
    + java Search

- Weight parameters:
    + 1st: Landing Height: The height where the piece is put (= the height of the column + (the height of the piece / 2)) 
    + 2nd: Rows eliminated: The number of rows eliminated
    + 3rd: Row Transitions: The total number of row transitions. A row transition occurs when an empty cell is adjacent to a filled cell on the same row and vice versa.
    + 4th: Column Transitions: The total number of column transitions. A column transition occurs when an empty cell is adjacent to a filled cell on the same column and vice versa.
    + 5th: Number of Holes: A hole is an empty cell that has at least one filled cell above it in the same column
    + 6th: Well Sums: A well is a succession of empty cells such that their left cells and right cells are both filled


