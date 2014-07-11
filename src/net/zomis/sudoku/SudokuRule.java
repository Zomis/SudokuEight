package net.zomis.sudoku;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
		this.tiles = new HashSet<SudokuTile>(tiles);
		this.description = description;
	}

	private final Set<SudokuTile> tiles;
	private final String description;

	public boolean checkValid() {
		Stream<SudokuTile> filtered = tiles.stream().filter(
				tile -> tile.hasValue());
		Map<Integer, Integer> grouped = filtered.collect(Collectors.groupingBy(SudokuTile::getValue, Collectors.summingInt(t -> 1)));
		return grouped.entrySet().stream().allMatch(group -> group.getValue() == 1);
	}

	public boolean checkComplete() {
		return tiles.stream().allMatch(tile -> tile.hasValue())
				&& checkValid();
	}

	SudokuProgress removePossibles() {
		Map<Boolean, List<SudokuTile>> hasValue = tiles.stream().collect(Collectors.partitioningBy(tile -> tile.hasValue()));
		List<SudokuTile> withNumber = hasValue.get(Boolean.TRUE);
		List<SudokuTile> withoutNumber = hasValue.get(Boolean.FALSE);
		
		// The existing numbers in this rule
		IntStream values = withNumber.stream().mapToInt(tile -> tile.getValue());
		Set<Integer> existingNumbers = new HashSet<Integer>(values.distinct().boxed().collect(Collectors.toSet()));

		SudokuProgress result = SudokuProgress.NO_PROGRESS;
		for (SudokuTile tile : withoutNumber)
			result = result.combineWith(tile.removePossibles(existingNumbers));
		return result;
	}

	SudokuProgress checkForOnlyOnePossibility() {
		// Check if there is only one number within this rule that can have a specific value
		Set<Integer> existingNumbers = tiles.stream().mapToInt(tile -> tile.getValue())
				.distinct().boxed().collect(Collectors.toSet());
		SudokuProgress result = SudokuProgress.NO_PROGRESS;

		for (int value = 1; value <= tiles.size(); value++) {
			if (existingNumbers.contains(value)) {
				continue; // this rule already has the value, skip checking for it
			}
			
			final int val = value;
			Set<SudokuTile> possibles = tiles
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
		return description;
	}

	public Set<SudokuTile> getTiles() {
		return new HashSet<>(tiles);
	}
	
	@Override
	public Iterator<SudokuTile> iterator() {
		return tiles.iterator();
	}

	public String getDescription() {
		return description;
	}
}
