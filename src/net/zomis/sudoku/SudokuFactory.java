package net.zomis.sudoku;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.zomis.common.ImmutablePoint;

public class SudokuFactory
{
	private static final int DEFAULT_SIZE = 9;
	private static final int SAMURAI_AREAS = 7;
	private static final int BOX_SIZE = 3;
	private static final int HYPER_MARGIN = 1;

	public static Collection<ImmutablePoint> box(int sizeX, int sizeY)
	{
		Collection<ImmutablePoint> points = new ArrayList<>(sizeX * sizeY);
		IntStream.range(0, sizeX).forEach(x -> IntStream.range(0, sizeY)
				.forEach(y -> points.add(new ImmutablePoint(x, y))));
		return points;
	}

	public static SudokuBoard samurai()
	{
		SudokuBoard board = new SudokuBoard(SAMURAI_AREAS*BOX_SIZE, SAMURAI_AREAS*BOX_SIZE, DEFAULT_SIZE);
		// Removed the empty areas where there are no tiles
		Collection<Collection<SudokuTile>> queriesForBlocked = new ArrayList<Collection<SudokuTile>>();

		queriesForBlocked.add(box(BOX_SIZE, BOX_SIZE*2).stream().map(pos -> board.Tile(pos.getX() + DEFAULT_SIZE, pos.getY()                            )).collect(Collectors.toList()));
		queriesForBlocked.add(box(BOX_SIZE, BOX_SIZE*2).stream().map(pos -> board.Tile(pos.getX() + DEFAULT_SIZE, pos.getY() + DEFAULT_SIZE * 2 - BOX_SIZE)).collect(Collectors.toList()));
		queriesForBlocked.add(box(BOX_SIZE*2, BOX_SIZE).stream().map(pos -> board.Tile(pos.getX()                            , pos.getY() + DEFAULT_SIZE)).collect(Collectors.toList()));
		queriesForBlocked.add(box(BOX_SIZE*2, BOX_SIZE).stream().map(pos -> board.Tile(pos.getX() + DEFAULT_SIZE * 2 - BOX_SIZE, pos.getY() + DEFAULT_SIZE)).collect(Collectors.toList()));

		queriesForBlocked.forEach(area -> area.forEach(tile -> tile.block()));

		// Select the tiles in the 3 x 3 area (area.X, area.Y) and create rules for them
		for (ImmutablePoint area : box(SAMURAI_AREAS, SAMURAI_AREAS)) {
			List<SudokuTile> tilesInArea = box(BOX_SIZE, BOX_SIZE).stream().map(pos -> board.Tile(area.getX() * BOX_SIZE + pos.getX(), area.getY() * BOX_SIZE + pos.getY())).collect(Collectors.toList());

			if (tilesInArea.iterator().next().IsBlocked())
				continue;
			board.createRule("Area " + area.getX() + ", " + area.getY(), tilesInArea);
		}

		for (int x = 0; x < board.getWidth(); x++) {
			final int posSetI = x;
			board.createRule("Column Upper " + x, box(1, DEFAULT_SIZE).stream().map(pos -> board.Tile(posSetI, pos.getY())));
			board.createRule("Column Lower " + x, box(1, DEFAULT_SIZE).stream().map(pos -> board.Tile(posSetI, pos.getY() + DEFAULT_SIZE + BOX_SIZE)));

			board.createRule("Row Left "  + x, box(DEFAULT_SIZE, 1).stream().map(pos -> board.Tile(pos.getX(), posSetI)));
			board.createRule("Row Right " + x, box(DEFAULT_SIZE, 1).stream().map(pos -> board.Tile(pos.getX() + DEFAULT_SIZE + BOX_SIZE, posSetI)));

			if (x >= BOX_SIZE*2 && x < BOX_SIZE*2 + DEFAULT_SIZE) {
				// Create rules for the middle sudoku
				board.createRule("Column Middle " + x, box(1, 9).stream().map(pos -> board.Tile(posSetI, pos.getY() + BOX_SIZE*2)));
				board.createRule("Row Middle "    + x, box(9, 1).stream().map(pos -> board.Tile(pos.getX() + BOX_SIZE*2, posSetI)));
			}
		}
		return board;
	}

	public static SudokuBoard sizeAndBoxes(int width, int height, int boxCountX, int boxCountY) {
		SudokuBoard board = new SudokuBoard(width, height);
		board.AddBoxesCount(boxCountX, boxCountY);
		return board;
	}

	public static SudokuBoard classicWith3x3Boxes() {
		return sizeAndBoxes(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE / BOX_SIZE, DEFAULT_SIZE / BOX_SIZE);
	}

	public static SudokuBoard ClassicWith3x3BoxesAndHyperRegions()
	{
		SudokuBoard board = classicWith3x3Boxes();
		final int HyperSecond = HYPER_MARGIN + BOX_SIZE + HYPER_MARGIN;
		// Create the four extra hyper regions

		board.createRule("HyperA", box(3, 3).stream().map(pos -> board.Tile(pos.getX() + HYPER_MARGIN, pos.getY() + HYPER_MARGIN)));
		board.createRule("HyperB", box(3, 3).stream().map(pos -> board.Tile(pos.getX() + HyperSecond, pos.getY() + HYPER_MARGIN)));
		board.createRule("HyperC", box(3, 3).stream().map(pos -> board.Tile(pos.getX() + HYPER_MARGIN, pos.getY() + HyperSecond)));
		board.createRule("HyperD", box(3, 3).stream().map(pos -> board.Tile(pos.getX() + HyperSecond, pos.getY() + HyperSecond)));
		return board;
	}

	public static SudokuBoard ClassicWithSpecialBoxes(String[] areas) {
		int sizeX = areas[0].length();
		int sizeY = areas.length;
		SudokuBoard board = new SudokuBoard(sizeX, sizeY);
		String joinedString = String.join("", areas);
		IntStream grouped = joinedString.chars().distinct();

		// Loop through all the unique characters

		// Select the rule tiles based on the index of the character
		for (int ch : grouped.boxed().collect(Collectors.toList())) {
			Stream<SudokuTile> ruleTiles = IntStream.range(0, joinedString.length())
					.filter(i -> joinedString.charAt(i) == ch) // filter out any non-matching characters
					.mapToObj(x -> board.Tile(x % sizeX, x / sizeY));
			board.createRule("Area " + (char) ch, ruleTiles);
		}

		return board;
	}
}
