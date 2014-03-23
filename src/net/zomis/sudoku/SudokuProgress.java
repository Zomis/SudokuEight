package net.zomis.sudoku;

public enum SudokuProgress {
	FAILED, NO_PROGRESS, PROGRESS;

	public SudokuProgress combineWith(SudokuProgress b) {
		if (this == SudokuProgress.FAILED)
			return this;
		if (this == SudokuProgress.NO_PROGRESS)
			return b;
		if (this == SudokuProgress.PROGRESS)
			return b == SudokuProgress.FAILED ? b : this;
		throw new UnsupportedOperationException("Invalid value for a");
	}

}