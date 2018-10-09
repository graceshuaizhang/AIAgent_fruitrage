import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class homework {
	public int size;
	public int fruit;
	public float time;
	public char[][] board;
	public State init;
	public long start;
	public int depth;
	
	public homework() {
		readInputFile();
		start = System.currentTimeMillis();
		init = new State();
		for (int r = 0; r < size; r++) {
			for (int c = 0; c < size; c++) {
				init.board[r][c] = board[r][c]; 
			}
		}
		// set max depth according to size
		if (size < 5) {
			depth = 6;
		} else if (size < 7) {
			depth = 4;
		} else if (size < 10) {
			depth = 2;
		} else {
			depth = 1;
		}
	}
	
	public class State {
		public char[][] board;
		public int score;
		public int currentScore;
		public int row;
		public int col;
		public boolean isPlayer;
		public int depth;
		
		/**
		 * Constructor.
		 */
		public State() {
			board = new char[size][size];
			score = 0;
			currentScore = 0;
			isPlayer = true;
			depth = 0;
		}
		
		/**
		 * Copy constructor.
		 * @param state the board state
		 */
		public State(State state) {
			board = new char[size][size];
			for (int r = 0; r < size; r++) {
				for (int c = 0; c < size; c++) {
					board[r][c] = state.board[r][c];
				}
			}
			score = state.score;
			currentScore = 0;
			isPlayer = state.isPlayer;
			depth = state.depth;
		}
		
		/**
		 * Check if this state is finished.
		 * @return true if finished, otherwise return false
		 */
		public boolean isFinish() {
			for (int r = 0; r < size; r++) {
				for (int c = 0; c < size; c++) {
					if (board[r][c] != '*') {
						return false;
					}
				}
			}
			return true;
		}
		
		/**
		 * Return string representation.
		 */
		public String toString() {
			String s = "";
			for (int r = 0; r < size; r++) {
				for (int c = 0; c < size; c++) {
					s += board[r][c];
				}
				s += "\n";
			}
			return s;
		}
	}
	
	/**
	 * Do claim action on the board.
	 * @param nextStep next step
	 * @param allBoard fill board
	 * @param fruit the fruit
	 * @param r the row
	 * @param c the column
	 */
	private void claim(State nextStep, State allBoard, char fruit, int r, int c) {
		nextStep.board[r][c] = '*';
		nextStep.currentScore += 1;
		allBoard.board[r][c] = '*';
		// search up
		if (r > 0 && nextStep.board[r - 1][c] == fruit) {
			claim(nextStep, allBoard, fruit, r - 1, c);
		}
		// search down
		if (r < size - 1 && nextStep.board[r + 1][c] == fruit) {
			claim(nextStep, allBoard, fruit, r + 1, c);
		}
		// search left
		if (c > 0 && nextStep.board[r][c - 1] == fruit) {
			claim(nextStep, allBoard, fruit, r, c - 1);
		}
		// search right
		if (c < size - 1 && nextStep.board[r][c + 1] == fruit) {
			claim(nextStep, allBoard, fruit, r, c + 1);
		}
	}
	
	/**
	 * Do down action on the board.
	 * @param nextStep the state
	 */
	private void down(State nextStep) {
		for (int c = 0; c < size; c++) {
			String col = "";
			for (int r = 0; r < size; r++) {
				col += nextStep.board[r][c];
			}
			col = col.replace("*", "");
			for (int r = 0; r < size - col.length(); r++) {
				nextStep.board[r][c] = '*';
			}
			for (int r = 0; r < col.length(); r++) {
				nextStep.board[r + size - col.length()][c] = col.charAt(r);
			}

		}
	}
	
	/**
	 * Get next states from current state.
	 * @param state
	 * @return
	 */
	private ArrayList<State> next(State state) {
		ArrayList<State> steps = new ArrayList<State>();
		State allBoard = new State(state);
		while (!allBoard.isFinish()) {
			for (int r = 0; r < size; r++) {
				for (int c = 0; c < size; c++) {
					if (allBoard.board[r][c] != '*') {
						// next step
						State nextStep = new State(state);
						nextStep.depth++;
						nextStep.row = r;
						nextStep.col = c;
						
						char fruit = nextStep.board[r][c];
						// do claim
						claim(nextStep, allBoard, fruit, r, c);
						if (nextStep.isPlayer) {
							nextStep.score += nextStep.currentScore * nextStep.currentScore;
							nextStep.isPlayer = false;
						} else {
							nextStep.score -= nextStep.currentScore * nextStep.currentScore;
							nextStep.isPlayer = true;
						}
						// do down
						down(nextStep);
						steps.add(nextStep);
					}
				}
			}
		}
		return steps;
	}
	
	/**
	 * Minimum stage.
	 * @param state the state
	 * @param a the alpha value
	 * @param b the beta value
	 * @return the best state
	 */
	private State min(State state, int a, int b) {
		if (state.isFinish() || state.depth > depth) {
			return state;
		}
		ArrayList<State> steps = next(state);
		State nextState = null;
		State currentState = null;
		for (int i = 0; i < steps.size(); i++) {
			State next = max(steps.get(i), a, b);
			if (nextState == null || next.score < nextState.score) {
				nextState = next;
				currentState = steps.get(i);
			}
			if (next.score <= a) {
				return currentState;
			} else if (next.score < b) {
				b = next.score;
			}
		}
		
		return currentState;
	}
	
	/**
	 * Maximum stage.
	 * @param state the state
	 * @param a the alpha value
	 * @param b the beta value
	 * @return the best state
	 */
	private State max(State state, int a, int b) {
		if (state.isFinish() || state.depth > depth) {
			return state;
		}
		ArrayList<State> steps = next(state);
		State nextState = null;
		State currentState = null;
		for (int i = 0; i < steps.size(); i++) {
			State next = min(steps.get(i), a, b);
			if (nextState == null || next.score > nextState.score) {
				nextState = next;
				currentState = steps.get(i);
			}
			if (next.score >= b) {
				return currentState;
			} else if (next.score > a) {
				a = next.score;
			}
		}
		
		return currentState;
	}
	
	/**
	 * Read information from input file.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void readInputFile() {
		try {
			Scanner scanner = new Scanner(new File("input.txt"));
			size = Integer.valueOf(scanner.nextLine());
			fruit = Integer.valueOf(scanner.nextLine());
			time = Float.valueOf(scanner.nextLine()) * 1000;
			board = new char[size][size];

			for (int r = 0; r < size; r++) {
				String line = scanner.nextLine();
				for (int c = 0; c < size; c++) {
					board[r][c] = line.charAt(c);
				}
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		homework hw = new homework();
		State result = hw.max(hw.init, -Integer.MIN_VALUE, Integer.MAX_VALUE);
		FileWriter fw;
		try {
			fw = new FileWriter("output.txt");
			fw.write((char)('A' + result.col));
			fw.write(String.valueOf(result.row + 1));
			fw.write("\n");
			hw.claim(hw.init, result, hw.init.board[result.row][result.col], result.row, result.col);
			hw.down(hw.init);
			fw.write(hw.init.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
