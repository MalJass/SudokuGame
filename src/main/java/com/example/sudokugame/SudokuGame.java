package com.example.sudokugame;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SudokuGame {

	private static final int GRID_SIZE = 9; // 9x9 grid
	private static final int SUBGRID_SIZE = 3; // 3x3 subgrid
	private static final int EMPTY_CELL = 0;
	private int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
	private JTextField[][] cells = new JTextField[GRID_SIZE][GRID_SIZE];

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SudokuGame().createAndShowGUI());
	}

	public void createAndShowGUI() {
		// Generate a solvable puzzle
		generatePuzzle();

		JFrame frame = new JFrame("Sudoku Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 600);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));

		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				JTextField cell = new JTextField();
				cell.setHorizontalAlignment(JTextField.CENTER);
				if (puzzle[row][col] != EMPTY_CELL) {
					cell.setText(String.valueOf(puzzle[row][col]));
					cell.setEditable(false);
					cell.setBackground(Color.YELLOW);
				}

				// Add borders for 3x3 subgrid separation
				int top = (row % SUBGRID_SIZE == 0) ? 3 : 1;
				int left = (col % SUBGRID_SIZE == 0) ? 3 : 1;
				int bottom = (row == GRID_SIZE - 1) ? 3 : 1;
				int right = (col == GRID_SIZE - 1) ? 3 : 1;

				cell.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));

				cells[row][col] = cell;
				panel.add(cell);
			}
		}

		JButton checkButton = new JButton("Check Solution");
		checkButton.addActionListener(e -> {
			highlightErrors();
			if (isSolutionValid()) {
				JOptionPane.showMessageDialog(frame, "Congratulations! You solved it!");
			} else {
				JOptionPane.showMessageDialog(frame, "Incorrect solution. Try again!");
			}
		});

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> resetPuzzle());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(checkButton);
		buttonPanel.add(resetButton);

		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private void generatePuzzle() {
		int[][] solvedGrid = generateSolvedGrid();
		copyGrid(solvedGrid, puzzle);

		Random random = new Random();
		int cellsToRemove = 40; // Adjust difficulty by changing this number
		while (cellsToRemove > 0) {
			int row = random.nextInt(GRID_SIZE);
			int col = random.nextInt(GRID_SIZE);

			if (puzzle[row][col] != EMPTY_CELL) {
				puzzle[row][col] = EMPTY_CELL;
				cellsToRemove--;
			}
		}
	}

	private int[][] generateSolvedGrid() {
		int[][] grid = new int[GRID_SIZE][GRID_SIZE];
		solveGrid(grid, 0, 0); // Use backtracking to generate a valid solved grid
		return grid;
	}

	private void shuffleArray(int[] array) {
		Random random = new Random();
		for (int i = array.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
	}

	private void shuffleRows(int[][] grid) {
		Random random = new Random();
		for (int i = 0; i < GRID_SIZE; i += SUBGRID_SIZE) {
			for (int j = 0; j < SUBGRID_SIZE; j++) {
				int randomIndex = i + random.nextInt(SUBGRID_SIZE);
				swapRows(grid, i + j, randomIndex);
			}
		}
	}

	private void shuffleColumns(int[][] grid) {
		Random random = new Random();
		for (int i = 0; i < GRID_SIZE; i += SUBGRID_SIZE) {
			for (int j = 0; j < SUBGRID_SIZE; j++) {
				int randomIndex = i + random.nextInt(SUBGRID_SIZE);
				swapColumns(grid, i + j, randomIndex);
			}
		}
	}

	private void swapRows(int[][] grid, int row1, int row2) {
		int[] temp = grid[row1];
		grid[row1] = grid[row2];
		grid[row2] = temp;
	}

	private void swapColumns(int[][] grid, int col1, int col2) {
		for (int i = 0; i < GRID_SIZE; i++) {
			int temp = grid[i][col1];
			grid[i][col1] = grid[i][col2];
			grid[i][col2] = temp;
		}
	}

	private boolean solveGrid(int[][] grid, int row, int col) {
		if (row == GRID_SIZE) return true; // Completed all rows
		if (col == GRID_SIZE) return solveGrid(grid, row + 1, 0); // Move to the next row

		Random random = new Random();
		int[] numbers = random.ints(1, GRID_SIZE + 1).distinct().limit(GRID_SIZE).toArray(); // Generate shuffled numbers

		for (int num : numbers) {
			if (isSafe(grid, row, col, num)) {
				grid[row][col] = num;
				if (solveGrid(grid, row, col + 1)) return true;
				grid[row][col] = EMPTY_CELL; // Backtrack
			}
		}
		return false; // Trigger backtracking
	}

	private boolean isSafe(int[][] grid, int row, int col, int num) {
		for (int i = 0; i < GRID_SIZE; i++) {
			if (grid[row][i] == num || grid[i][col] == num ||
					grid[row / SUBGRID_SIZE * SUBGRID_SIZE + i / SUBGRID_SIZE]
							[col / SUBGRID_SIZE * SUBGRID_SIZE + i % SUBGRID_SIZE] == num) {
				return false;
			}
		}
		return true;
	}

	private boolean isSolutionValid() {
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				String input = cells[row][col].getText().trim();
				if (input.isEmpty() || !input.matches("\\d")) return false;

				int num = Integer.parseInt(input);
				if (num < 1 || num > GRID_SIZE) return false;

				int temp = puzzle[row][col];
				puzzle[row][col] = EMPTY_CELL;

				if (!isSafe(puzzle, row, col, num)) {
					puzzle[row][col] = temp;
					return false;
				}
				puzzle[row][col] = temp;
			}
		}
		return true;
	}

	private void resetPuzzle() {
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if (puzzle[row][col] == EMPTY_CELL) {
					cells[row][col].setText("");
					cells[row][col].setBackground(Color.WHITE); // Clear error highlight
				}
			}
		}
	}

	private void highlightErrors() {
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				JTextField cell = cells[row][col];
				String input = cell.getText().trim();

				if (puzzle[row][col] != EMPTY_CELL) {
					cell.setBackground(Color.YELLOW); // Pre-filled cells remain yellow
					continue;
				}

				if (!input.isEmpty() && input.matches("\\d")) {
					int num = Integer.parseInt(input);
					if (isSafe(puzzle, row, col, num)) {
						cell.setBackground(Color.GREEN); // Correctly filled cells are green
					} else {
						cell.setBackground(Color.RED); // Incorrectly filled cells are red
					}
				} else {
					cell.setBackground(Color.WHITE); // Reset empty or invalid cells to white
				}
			}
		}
	}


	private void copyGrid(int[][] source, int[][] destination) {
		for (int i = 0; i < GRID_SIZE; i++) {
			System.arraycopy(source[i], 0, destination[i], 0, GRID_SIZE);
		}
	}
}
