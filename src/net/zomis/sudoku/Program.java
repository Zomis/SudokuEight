package net.zomis.sudoku;
import java.util.Collection;

class Program {

	public static void main(String[] args) {
		solveFail();
		solveClassic();
		solveSmall();
		solveExtraZones();
		solveHyper();
		solveSamurai();
		solveIncompleteClassic();
	}

	private static void solveFail() {
		SudokuBoard board = SudokuFactory.sizeAndBoxes(4, 4, 2, 2)
			.addRow("0003")
			.addRow("0204") // the 2 must be a 1 on this row to be solvable
			.addRow("1000")
			.addRow("4000");
		completeSolve(board);
	}

	private static void solveExtraZones() {
		// http://en.wikipedia.org/wiki/File:Oceans_Hypersudoku18_Puzzle.svg
		SudokuBoard board = SudokuFactory.ClassicWith3x3BoxesAndHyperRegions();
		board.addRow(".......1.");
		board.addRow("..2....34");
		board.addRow("....51...");
		board.addRow(".....65..");
		board.addRow(".7.3...8.");
		board.addRow("..3......");
		board.addRow("....8....");
		board.addRow("58....9..");
		board.addRow("69.......");
		completeSolve(board);
	}

	private static void solveSmall() {
		SudokuBoard board = SudokuFactory.sizeAndBoxes(4, 4, 2, 2);
		board.addRow("0003");
		board.addRow("0004");
		board.addRow("1000");
		board.addRow("4000");
		completeSolve(board);
	}

	private static void solveHyper() {
		// http://en.wikipedia.org/wiki/File:A_nonomino_sudoku.svg
		String[] areas = new String[] { "111233333", "111222333", "144442223",
				"114555522", "444456666", "775555688", "977766668",
				"999777888", "999997888" };
		SudokuBoard board = SudokuFactory.ClassicWithSpecialBoxes(areas);
		board.addRow("3.......4");
		board.addRow("..2.6.1..");
		board.addRow(".1.9.8.2.");
		board.addRow("..5...6..");
		board.addRow(".2.....1.");
		board.addRow("..9...8..");
		board.addRow(".8.3.4.6.");
		board.addRow("..4.1.9..");
		board.addRow("5.......7");
		completeSolve(board);

	}

	private static void solveSamurai() {
		// http://www.freesamuraisudoku.com/1001HardSamuraiSudokus.aspx?puzzle=42
		SudokuBoard board = SudokuFactory.samurai();
		board.addRow("6..8..9..///.....38..");
		board.addRow("...79....///89..2.3..");
		board.addRow("..2..64.5///...1...7.");
		board.addRow(".57.1.2..///..5....3.");
		board.addRow(".....731.///.1.3..2..");
		board.addRow("...3...9.///.7..429.5");
		board.addRow("4..5..1...5....5.....");
		board.addRow("8.1...7...8.2..768...");
		board.addRow(".......8.23...4...6..");
		board.addRow("//////.12.4..9.//////");
		board.addRow("//////......82.//////");
		board.addRow("//////.6.....1.//////");
		board.addRow(".4...1....76...36..9.");
		board.addRow("2.....9..8..5.34...81");
		board.addRow(".5.873......9.8..23..");
		board.addRow("...2....9///.25.4....");
		board.addRow("..3.64...///31.8.....");
		board.addRow("..75.8.12///...6.14..");
		board.addRow(".......2.///.31...9..");
		board.addRow("..17.....///..7......");
		board.addRow(".7.6...84///8...7..5.");
		completeSolve(board);
	}

	private static void solveClassic() {
		SudokuBoard board = SudokuFactory.classicWith3x3Boxes();
		board.addRow("...84...9");
		board.addRow("..1.....5");
		board.addRow("8...2146.");
		board.addRow("7.8....9.");
		board.addRow(".........");
		board.addRow(".5....3.1");
		board.addRow(".2491...7");
		board.addRow("9.....5..");
		board.addRow("3...84...");
		completeSolve(board);
	}

	private static void solveIncompleteClassic() {
		SudokuBoard board = SudokuFactory.classicWith3x3Boxes();
		board.addRow("...84...9");
		board.addRow("..1.....5");
		board.addRow("8...2.46."); // Removed a "1" on this line
		board.addRow("7.8....9.");
		board.addRow(".........");
		board.addRow(".5....3.1");
		board.addRow(".2491...7");
		board.addRow("9.....5..");
		board.addRow("3...84...");
		completeSolve(board);
	}

	private static void completeSolve(SudokuBoard board) {
		System.out.println("Rules:");
		board.outputRules();
		System.out.println("Board:");
		board.Output();
		Collection<SudokuBoard> solutions = board.Solve();
		System.out.println("Base Board Progress:");
		board.Output();
		System.out.println("--");
		System.out.println("--");
		System.out.println("All " + solutions.size() + " solutions:");
		int i = 1;
		for (SudokuBoard solution : solutions) {
			System.out.println("----------------");
			System.out.println("Solution " + i++ + " / " + solutions.size()
					+ ":");
			solution.Output();
		}
	}
}
