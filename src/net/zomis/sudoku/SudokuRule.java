package net.zomis.sudoku;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SudokuRule implements Iterable<SudokuTile> {
	
	SudokuRule(Stream<SudokuTile> tiles, String description) {
		this(tiles.collect(Collectors.toSet()), description);
	}
	SudokuRule(Collection<SudokuTile> tiles, String description) {
		_tiles = new HashSet<SudokuTile>(tiles);
		_description = description;
	}

	private final Set<SudokuTile> _tiles;
	private final String _description;

	public boolean checkValid() {
		Stream<SudokuTile> filtered = _tiles.stream().filter(
				tile -> tile.hasValue());
		Map<Integer, Integer> grouped = filtered.collect(Collectors.groupingBy(SudokuTile::getValue, Collectors.summingInt(t -> 1)));
		return grouped.entrySet().stream().allMatch(group -> group.getValue() == 1);
	}

	public boolean checkComplete() {
		return _tiles.stream().allMatch(tile -> tile.hasValue())
				&& checkValid();
	}

	SudokuProgress removePossibles() {
		// Tiles that has a number already
		Stream<SudokuTile> withNumber = _tiles.stream().filter(tile -> tile.hasValue());

		// Tiles without a number
		Stream<SudokuTile> withoutNumber = _tiles.stream().filter(tile -> !tile.hasValue());

		// The existing numbers in this rule
		IntStream values = withNumber.mapToInt(tile -> tile.getValue());
		Set<Integer> existingNumbers = new HashSet<Integer>(values.distinct().boxed().collect(Collectors.toSet()));

		SudokuProgress result = SudokuProgress.NO_PROGRESS;
		for (SudokuTile tile : withoutNumber.collect(Collectors.toList()))
			result = result.combineWith(tile.removePossibles(existingNumbers));
		return result;
	}

	SudokuProgress checkForOnlyOnePossibility() {
		// Check if there is only one number within this rule that can have a specific value
		Set<Integer> existingNumbers = _tiles.stream().mapToInt(tile -> tile.getValue())
				.distinct().boxed().collect(Collectors.toSet());
		SudokuProgress result = SudokuProgress.NO_PROGRESS;

		for (int value = 1; value <= _tiles.size(); value++) {
			if (existingNumbers.contains(value)) {
				continue; // this rule already has the value, skip checking for it
			}
			
			final int val = value;
			Set<SudokuTile> possibles = _tiles
					.stream()
					.filter(tile -> !tile.hasValue()
							&& tile.isPossibleValue(val))
					.collect(Collectors.toSet());
			if (possibles.isEmpty())
				return SudokuProgress.FAILED;
			
			if (possibles.size() == 1) {
				possibles.iterator().next().fix(value, "Only possible in rule " + toString());
				result = SudokuProgress.PROGRESS;
			}
		}
		return result;
	}

	SudokuProgress solve() {
		SudokuProgress result1 = removePossibles();
		SudokuProgress result2 = checkForOnlyOnePossibility();
		return result1.combineWith(result2);
	}

	@Override
	public String toString() {
		return _description;
	}

	public Set<SudokuTile> getTiles() {
		return new HashSet<>(_tiles);
	}
	
	@Override
	public Iterator<SudokuTile> iterator() {
		return _tiles.iterator();
	}

	public String getDescription() {
		return _description;
	}
}
