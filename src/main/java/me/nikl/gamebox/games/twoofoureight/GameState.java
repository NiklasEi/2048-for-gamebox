package me.nikl.gamebox.games.twoofoureight;

/**
 * @author Niklas Eicker
 */
public class GameState {
    private Integer[][] grid;
    private int score;
    private int gridSize;

    public GameState(int gridSize) {
        this.score = 0;
        this.gridSize = gridSize;
    }

    public void set(int score, Integer[][] grid) {
        this.score = score;
        this.grid = new Integer[gridSize][gridSize];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid.length; x++) {
                this.grid[x][y] = new Integer(grid[x][y]);
            }
        }
    }

    public int getScore() {
        return this.score;
    }

    public Integer[][] getGrid() {
        return this.grid;
    }
}
