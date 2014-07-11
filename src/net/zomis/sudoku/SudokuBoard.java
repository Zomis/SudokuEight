package net.zomis.sudoku;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.zomis.common.ImmutablePoint;

public class SudokuBoard {
	
	private int	mMaxValue;
	private int	rowAddIndex;
	
	private Set<SudokuRule>	rules	= new HashSet<SudokuRule>();
	

	private SudokuTile[][]	tiles;

	public SudokuBoard(int width, int height) {
		this(width, height, Math.max(width, height));
	}

	public SudokuBoard(int width, int height, int maxValue) {
		mMaxValue = maxValue;
		tiles = new SudokuTile[width][height];
		createTiles();
		// If maxValue is not width or height, then adding line rules would be stupid
		if (mMaxValue == width || mMaxValue == height) {
			IntStream.range(0, getWidth ()).forEach(x -> rules.add(new SudokuRule(getColumn(x), "Row " + x)));
			IntStream.range(0, getHeight()).forEach(y -> rules.add(new SudokuRule(getRow(y), "Col " + y)));
		}
	}


	public SudokuBoard(SudokuBoard copy) {
		mMaxValue = copy.mMaxValue;
		tiles = new SudokuTile[copy.getWidth()][copy.getHeight()];
		createTiles();
		// Copy the tile values
		for (ImmutablePoint pos : SudokuFactory.box(getWidth(), getHeight())) {
			tiles[pos.getX()][pos.getY()] = new SudokuTile(pos.getX(),
					pos.getY(), mMaxValue);
			tiles[pos.getX()][pos.getY()].setValue(copy.tiles[pos.getX()][pos
					.getY()].getValue());
		}

		// Copy the rules
		for (SudokuRule rule : copy.rules) {
			Set<SudokuTile> ruleTiles = new HashSet<SudokuTile>();
			for (SudokuTile tile : rule) {
				ruleTiles.add(tiles[tile.getX()][tile.getY()]);
			}
			rules.add(new SudokuRule(ruleTiles, rule.getDescription()));
		}
	}

	void addBoxesCount(int boxesX, int boxesY) {
		int sizeX = getWidth() / boxesX;
		int sizeY = getHeight() / boxesY;

		Collection<ImmutablePoint> boxes = SudokuFactory.box(sizeX, sizeY);
		for (ImmutablePoint pos : boxes) {
			Collection<SudokuTile> boxTiles = TileBox(pos.getX() * sizeX,
					pos.getY() * sizeY, sizeX, sizeY);
			createRule("Box at (" + pos.getX() + ", " + pos.getY() + ")",
					boxTiles);
		}
	}

	public SudokuBoard addRow(String s) {
		// Method for initializing a board from string
		for (int i = 0; i < s.length(); i++) {
			SudokuTile tile = tiles[i][rowAddIndex];
			if (s.charAt(i) == '/') {
				tile.block();
				continue;
			}
			int value = s.charAt(i) == '.' ? 0 : Character.digit(s.charAt(i),
					10);
			tile.setValue(value);
		}
		rowAddIndex++;
		return this;
	}

	public boolean checkValid() {
		return rules.stream().allMatch(rule -> rule.checkValid());
	}

	public void createRule(String description, Collection<SudokuTile> tiles) {
		if (tiles.stream().anyMatch(tile -> tile.IsBlocked()))
			throw new IllegalArgumentException("Unable to create rule since a tile in it is blocked: " + tiles);
		rules.add(new SudokuRule(tiles, description));
	}
	
	public void createRule(String description, Stream<SudokuTile> tiles) {
		createRule(description, tiles.collect(Collectors.toList()));
	}

	private void createTiles() {
	    SudokuFactory.box(getWidth(), getHeight())
	    	.stream().forEach(pos -> tiles[pos.getX()][pos.getY()] = new SudokuTile(pos.getX(), pos.getY(), mMaxValue));
	}

	private Stream<SudokuTile> getColumn(int col) {
		return IntStream.range(0, getHeight()).mapToObj(i -> tiles[col][i]);
	}
	public int getHeight() {
		return tiles[0].length;
	}

	private Collection<SudokuTile> getRow(int row) {
		return IntStream.range(0, getWidth()).mapToObj(i -> tiles[i][row])
				.collect(Collectors.toList());
	}

	public int getWidth() {
		return tiles.length;
	}

	public void output() {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				System.out.print(tiles[x][y].toStringSimple());
			}
			System.out.println();
		}
	}

	void outputRules() {
//		rules.stream().forEach(System.out::println);
		for (SudokuRule rule : rules) {
			System.out.println(String.join(",", rule.getTiles().toString()) + " - " + rule);
		}
	}

	void resetSolutions() {
		Arrays.stream(tiles).forEach(
				arr -> Arrays.stream(arr)
						.forEach(tile -> tile.resetPossibles()));
	}

	SudokuProgress simplify() {
		SudokuProgress result = SudokuProgress.NO_PROGRESS;
		if (!checkValid())
			return SudokuProgress.FAILED;

		for (SudokuRule rule : rules)
			result = result.combineWith(rule.solve());

		return result;
	}

	public Collection<SudokuBoard> solve() {
		resetSolutions();
		SudokuProgress simplify = SudokuProgress.PROGRESS;
		while (simplify == SudokuProgress.PROGRESS)
			simplify = simplify();

		if (simplify == SudokuProgress.FAILED)
			return new ArrayList<>();

		// Find one of the values with the least number of alternatives,
		// but that still has at least 2 alternatives

		Stream<SudokuTile> query = rules
				.stream()
				.flatMap(
						rule -> rule.getTiles().stream()
								.filter(tile -> (tile.getPossibleCount() > 1)))
				.sorted((tileA, tileB) -> Integer.compare(tileA.getPossibleCount(),
						tileB.getPossibleCount()));

		Optional<SudokuTile> chosen = query.findFirst();
		if (!chosen.isPresent()) {
			// The board has been completed, we're done!
			Collection<SudokuBoard> results = new ArrayList<SudokuBoard>();
			results.add(this);
			return results;
		}

//		System.out.println("SudokuTile: " + chosen.toString());

		SudokuTile tile = chosen.get();
		
		Collection<SudokuBoard> results = new ArrayList<>();
		
		results = IntStream
				.rangeClosed(1, mMaxValue)
//				.parallel()
				.filter(value -> tile.isPossibleValue(value))
				.mapToObj(value -> fixTile(tile, value))
				.flatMap(board -> board.solve().stream())
				.collect(Collectors.toList());
		
		
//		for (int value = 1; value <= mMaxValue; value++) {
//			// Iterate through all the valid possibles on the chosen square and pick a number for it
//			if (!tile.isPossibleValue(value))
//				continue;
//			SudokuBoard newBoard = fixTile(tile, value);
//			results.addAll(newBoard.solve());
//		}
		return results;
	}

	private SudokuBoard fixTile(SudokuTile tile, int value) {
		SudokuBoard newBoard = new SudokuBoard(this);
		newBoard.tile(tile.getX(), tile.getY()).fix(value, "Trial and error");
		return newBoard;
	}

	public SudokuTile tile(int x, int y) {
		return tiles[x][y];
	}

	Collection<SudokuTile> TileBox(int startX, int startY, int sizeX, int sizeY) {
		Collection<ImmutablePoint> positions = SudokuFactory.box(sizeX, sizeY);
		return positions.stream()
				.map(pos -> tiles[startX + pos.getX()][startY + pos.getY()])
				.collect(Collectors.toList());
	}

	public boolean isComplete() {
		return this.rules.stream().allMatch(rule -> rule.checkComplete());
	}

	public boolean isRulesValid() {
		Predicate<SudokuTile> tileInRule = tile -> rules.stream().anyMatch(rule -> rule.getTiles().contains(tile));
		return Arrays.stream(tiles).flatMap(arr -> Arrays.stream(arr)).filter(tile -> !tile.IsBlocked())
				.allMatch(tile -> tileInRule.test(tile));
	}

	public Set<SudokuRule> getRules() {
		return new HashSet<>(this.rules);
	}

	public void highlightRule(SudokuRule rule) {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (rule.getTiles().contains(tile(x, y)))
					System.out.print(" ");
				else System.out.print(tiles[x][y].toStringSimple());
			}
			System.out.println();
		}
	}
}
