package net.zomis.sudoku;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SudokuTile {
	public static final int	CLEARED	= 0;
	private int				_maxValue;
	private int				_value;
	private int				_x;
	private int				_y;
	private Set<Integer>	possibleValues;
	private boolean			_blocked;

	public SudokuTile(int x, int y, int maxValue) {
		_x = x;
		_y = y;
		_blocked = false;
		_maxValue = maxValue;
		possibleValues = new HashSet<Integer>();
		_value = 0;
	}

	public int getValue() {
		return _value;
	}

	public void setValue(int value) {
		if (value > _maxValue)
			throw new IllegalArgumentException(
					"SudokuTile Value cannot be greater than " + _maxValue
							+ ". Was " + value);
		if (value < CLEARED)
			throw new IllegalArgumentException(
					"SudokuTile Value cannot be zero or smaller. Was " + value);
		_value = value;
	}

	public boolean hasValue() {
		return getValue() != CLEARED;
	}

	public String toStringSimple() {
		return String.valueOf(getValue());
	}

	@Override
	public String toString() {
		return String.format("Value {0} at pos {1}, {2}. ", getValue(), _x, _y,
				possibleValues.size());
	}

	void resetPossibles() {
		possibleValues.clear();
		for (int i = 1; i <= _maxValue; i++) {
			if (!hasValue() || getValue() == i)
				possibleValues.add(i);
		}
	}

	public void block() {
		_blocked = true;
	}

	void fix(int value, String reason) {
		System.out.printf("Fixing %d on pos %d, %d: %s", value, _x, _y, reason);
		setValue(value);
		resetPossibles();
	}

	SudokuProgress removePossibles(Collection<Integer> existingNumbers) {
		if (_blocked)
			return SudokuProgress.NO_PROGRESS;
		// Takes the current possible values and removes the ones existing in `existingNumbers`

		possibleValues = new HashSet<Integer>(possibleValues.stream()
				.filter(x -> !existingNumbers.contains(x))
				.collect(Collectors.toList()));
		SudokuProgress result = SudokuProgress.NO_PROGRESS;
		if (possibleValues.size() == 1) {
			fix(possibleValues.iterator().next(), "Only one possibility");
			result = SudokuProgress.PROGRESS;
		}
		return possibleValues.isEmpty() ? SudokuProgress.FAILED : result;
	}

	public boolean isPossibleValue(int i) {
		return possibleValues.contains(i);
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

	/**
	 * A blocked field can not contain a value -- used for creating 'holes' in the map
	 * 
	 * @return True if this field is blocked, false otherwise
	 */
	public boolean IsBlocked() {
		return _blocked;
	}

	public int getPossibleCount() {
		return IsBlocked() ? 1 : possibleValues.size();
	}
}
