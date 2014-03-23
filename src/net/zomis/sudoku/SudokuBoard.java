package net.zomis.sudoku;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.zomis.common.ImmutablePoint;

public class SudokuBoard {
	
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

	public SudokuBoard(int width, int height, int maxValue) {
		mMaxValue = maxValue;
		tiles = new SudokuTile[width][height];
		createTiles();
		// If maxValue is not width or height, then adding line rules would be stupid
		if (mMaxValue == width || mMaxValue == height) {
			IntStream.range(0, getWidth ()).forEach(x -> rules.add(new SudokuRule(GetCol(x), "Row " + x)));
			IntStream.range(0, getHeight()).forEach(y -> rules.add(new SudokuRule(GetRow(y), "Col " + y)));
		}
	}

	public SudokuBoard(int width, int height) {
		this(width, height, Math.max(width, height));
	}

	private int	mMaxValue;

	private void createTiles() {
	    SudokuFactory.box(getWidth(), getHeight())
	    	.stream().forEach(pos -> tiles[pos.getX()][pos.getY()] = new SudokuTile(pos.getX(), pos.getY(), mMaxValue));
	}

	Collection<SudokuTile> TileBox(int startX, int startY, int sizeX, int sizeY) {
		Collection<ImmutablePoint> positions = SudokuFactory.box(sizeX, sizeY);
		return positions.stream()
				.map(pos -> tiles[startX + pos.getX()][startY + pos.getY()])
				.collect(Collectors.toList());
	}

	private Collection<SudokuTile> GetRow(int row) {
		return IntStream.range(0, getWidth()).mapToObj(i -> tiles[i][row])
				.collect(Collectors.toList());
	}

	private Stream<SudokuTile> GetCol(int col) {
		return IntStream.range(0, getHeight()).mapToObj(i -> tiles[col][i]);
	}

	private Set<SudokuRule>	rules	= new HashSet<SudokuRule>();
	private SudokuTile[][]	tiles;

	public int getWidth() {
		return tiles.length;
	}

	public int getHeight() {
		return tiles[0].length;
	}

	public void createRule(String description, Stream<SudokuTile> tiles) {
		createRule(description, tiles.collect(Collectors.toList()));
	}
	public void CreateRule(String description, SudokuTile... tiles) {
		rules.add(new SudokuRule(Arrays.asList(tiles), description));
	}

	public void createRule(String description, Collection<SudokuTile> tiles) {
		rules.add(new SudokuRule(tiles, description));
	}

	public boolean CheckValid() {
		return rules.stream().allMatch(rule -> rule.checkValid());
	}

	public Collection<SudokuBoard> Solve() {
		Collection<SudokuBoard> results = new ArrayList<SudokuBoard>();
		ResetSolutions();
		SudokuProgress simplify = SudokuProgress.PROGRESS;
		while (simplify == SudokuProgress.PROGRESS)
			simplify = Simplify();

		if (simplify == SudokuProgress.FAILED)
			return results;

		// Find one of the values with the least number of alternatives, but
		// that still has at least 2 alternatives

		// var query = from rule in rules
		// from tile in rule
		// where tile.PossibleCount > 1
		// orderby tile.PossibleCount ascending
		// select tile;

		Stream<SudokuTile> query = rules
				.stream()
				.flatMap(
						rule -> rule.getTiles().stream()
								.filter(tile -> (tile.getPossibleCount() > 1)))
				.sorted((a, b) -> Integer.compare(a.getPossibleCount(),
						b.getPossibleCount()));

		Optional<SudokuTile> chosen = query.findFirst();
		if (!chosen.isPresent()) {
			// The board has been completed, we're done!
			results.add(this);
			return results;
		}

		System.out.println("SudokuTile: " + chosen.toString());

		for (int value = 1; value <= mMaxValue; value++) {
			// Iterate through all the valid possibles on the chosen square and
			// pick a number for it
			SudokuTile tile = chosen.get();
			if (!tile.isPossibleValue(value))
				continue;
			SudokuBoard copy = new SudokuBoard(this);
			copy.Tile(tile.getX(), tile.getY()).fix(value, "Trial and error");
			for (SudokuBoard innerSolution : copy.Solve())
				results.add(innerSolution);
		}
		return results;
	}

	public void Output() {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				System.out.print(tiles[x][y].toStringSimple());
			}
			System.out.println();
		}
	}

	public SudokuTile Tile(int x, int y) {
		return tiles[x][y];
	}

	private int	_rowAddIndex;

	public SudokuBoard addRow(String s) {
		// Method for initializing a board from string
		for (int i = 0; i < s.length(); i++) {
			SudokuTile tile = tiles[i][_rowAddIndex];
			if (s.charAt(i) == '/') {
				tile.block();
				continue;
			}
			int value = s.charAt(i) == '.' ? 0 : Character.digit(s.charAt(i),
					10);
			tile.setValue(value);
		}
		_rowAddIndex++;
		return this;
	}

	void ResetSolutions() {
		Arrays.stream(tiles).forEach(
				arr -> Arrays.stream(arr)
						.forEach(tile -> tile.resetPossibles()));
	}

	SudokuProgress Simplify() {
		SudokuProgress result = SudokuProgress.NO_PROGRESS;
		boolean valid = CheckValid();
		if (!valid)
			return SudokuProgress.FAILED;

		for (SudokuRule rule : rules)
			result = result.combineWith(rule.solve());

		return result;
	}

	void AddBoxesCount(int boxesX, int boxesY) {
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

	void outputRules() {
//		rules.stream().forEach(System.out::println);
		for (SudokuRule rule : rules) {
			System.out.println(String.join(",", rule.getTiles().toString()) + " - " + rule);
		}
	}
}
