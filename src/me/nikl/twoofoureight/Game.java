package me.nikl.twoofoureight;

import com.sun.org.apache.xml.internal.utils.IntVector;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.commons.lang.enums.Enum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Niklas on 14.04.2017.
 *
 *
 */
public class Game {

    private Main plugin;

    private GameRules rule;
    private boolean playSounds;
    private Language lang;

    private Map<Integer, ItemStack> items;

    private Inventory inventory;

    private int gridSize = 4;

    private Player player;

    private double spawnHigherTile = 0.2;

    private Integer[][] grid = new Integer[gridSize][gridSize];

    private Random random;

    private int position = 11, score = 0;

    private boolean over = false;

    public enum Clicks{
        LEFT, RIGHT, UP, DOWN
    }

    private ItemStack left, right, up, down;

    public Game(GameRules rule, Main plugin, Player player, Map<Integer, ItemStack> items){
        this.plugin = plugin;
        this.rule = rule;
        this.playSounds = plugin.getPlaySounds();
        this.lang = plugin.lang;

        this.player = player;

        this.random = new Random(System.currentTimeMillis());

        this.items = items;

        this.left = new ItemStack(Material.ARROW);
        this.right = new ItemStack(Material.ARROW);
        this.up = new ItemStack(Material.ARROW);
        this.down = new ItemStack(Material.ARROW);

        ItemMeta meta = left.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Left");
        this.left.setItemMeta(meta);

        meta = right.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Right");
        this.right.setItemMeta(meta);

        meta = up.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Up");
        this.up.setItemMeta(meta);

        meta = down.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Down");
        this.down.setItemMeta(meta);

        this.inventory = Bukkit.createInventory(null, 54, lang.GAME_TITLE.replace("%score%", String.valueOf(score)));

        for (int y = 0; y < gridSize;y++){
            for (int x = 0; x < gridSize;x++){
                grid[x][y] = 0;
                if(y == 0){
                    inventory.setItem(position + (y-1)*9 + x, this.up);
                } else if(y == gridSize-1){
                    inventory.setItem(position + (y+1)*9 + x, this.down);
                }
                if(x == 0){
                    inventory.setItem(position + (y)*9 + x - 1, this.left);
                } else if(x == gridSize-1){
                    inventory.setItem(position + (y)*9 + x + 1, this.right);
                }
            }
        }

        spawn();
        build();
        player.openInventory(inventory);

        player.getOpenInventory().getBottomInventory().setItem(13, up);
        player.getOpenInventory().getBottomInventory().setItem(21, left);
        player.getOpenInventory().getBottomInventory().setItem(23, right);
        player.getOpenInventory().getBottomInventory().setItem(31, down);
    }

    private void build() {
        for (int y = 0; y < gridSize;y++){
            for (int x = 0; x < gridSize;x++){
                inventory.setItem(position + y*9 + x, items.get(grid[x][y]));
            }
        }
        if(!over){
            plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE.replace("%score%", String.valueOf(score)));
        } else {
            plugin.getNms().updateInventoryTitle(player, lang.GAME_TITLE_LOST.replace("%score%", String.valueOf(score)));
        }
    }

    private void spawn(){
        if(getFreeSlots() == 0){
            return;
        }
        int x = random.nextInt(gridSize),y = random.nextInt(gridSize);
        while (grid[x][y] != 0){
            x = random.nextInt(gridSize);
            y = random.nextInt(gridSize);
        }
        if(random.nextDouble() > spawnHigherTile){
            grid[x][y] = 1;
            score += 2;
        } else {
            grid[x][y] = 2;
            score += 4;
        }

        if(getFreeSlots() == 0 && !moveLeft(false) && !moveUp(false)){
            over = true;
            plugin.debug("game is lost");
        }
    }

    private int getFreeSlots() {
        int toReturn = 0;
        for(int x = 0; x < gridSize; x++){
            for(int y = 0; y < gridSize; y++){
                if(grid[x][y] == 0){
                    toReturn ++;
                }
            }
        }
        return toReturn;
    }

    private boolean moveLeft(){
        return moveLeft(true);
    }

    private boolean moveLeft(boolean set){
        List<Integer> toMerge = new ArrayList<>();
        boolean changed =  false;
        for (int y = 0; y < gridSize;y++){
            for (int x = 0; x < gridSize - 1;x++){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.LEFT, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.LEFT, x, y, true);
                    }
                    changed = true;
                    x++;
                }
            }
        }

        y:
        for (int y = 0; y < gridSize;y++) {
            for (int x = 0; x < gridSize;x++) {
                if(grid[x][y] == 0){
                    if(set){
                        grid[x][y] = getNextTile(Clicks.LEFT, x, y, true);
                    } else {
                        if(getNextTile(Clicks.LEFT, x, y, false) != 0){
                            return true;
                        }
                    }
                    if(grid[x][y] == 0){
                        continue y;
                    } else {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }


    private boolean moveRight(){
        return moveRight(true);
    }

    private boolean moveRight(boolean set){
        List<Integer> toMerge = new ArrayList<>();
        boolean changed =  false;
        for (int y = 0; y < gridSize;y++){
            for (int x = gridSize - 1; x >= 0 ;x--){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.RIGHT, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.RIGHT, x, y, true);
                    }
                    changed = true;
                    x++;
                }
            }
        }

        y:
        for (int y = 0; y < gridSize;y++) {
            for (int x = gridSize - 1; x >= 0 ;x--) {
                if(grid[x][y] == 0){
                    if(set){
                        grid[x][y] = getNextTile(Clicks.RIGHT, x, y, true);
                    } else {
                        if(getNextTile(Clicks.RIGHT, x, y, false) != 0){
                            return true;
                        }
                    }
                    if(grid[x][y] == 0){
                        continue y;
                    } else {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }


    private boolean moveUp() {
        return moveUp(true);
    }

    private boolean moveUp(boolean set){
        List<Integer> toMerge = new ArrayList<>();
        boolean changed =  false;
        for (int x = 0; x < gridSize;x++){
            for (int y = 0; y < gridSize - 1;y++){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.UP, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.UP, x, y, true);
                    }
                    changed = true;
                    y++;
                }
            }
        }

        x:
        for (int x = 0; x < gridSize;x++) {
            for (int y = 0; y < gridSize;y++) {
                if(grid[x][y] == 0){
                    if(set){
                        grid[x][y] = getNextTile(Clicks.UP, x, y, true);
                    } else {
                        if(getNextTile(Clicks.UP, x, y, false) != 0){
                            return true;
                        }
                    }
                    if(grid[x][y] == 0){
                        continue x;
                    } else {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }



    private boolean moveDown() {
        return moveDown(true);
    }

    private boolean moveDown(boolean set){
        List<Integer> toMerge = new ArrayList<>();
        boolean changed =  false;
        for (int x = 0; x < gridSize;x++){
            for (int y = gridSize - 1; y >=0;y--){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.DOWN, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.DOWN, x, y, true);
                    }
                    changed = true;
                    y++;
                }
            }
        }

        x:
        for (int x = 0; x < gridSize;x++) {
            for (int y = gridSize - 1; y >=0;y--) {
                if(grid[x][y] == 0){
                    if(set){
                        grid[x][y] = getNextTile(Clicks.DOWN, x, y, true);
                    } else {
                        if(getNextTile(Clicks.DOWN, x, y, false) != 0){
                            return true;
                        }
                    }
                    if(grid[x][y] == 0){
                        continue x;
                    } else {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private Integer getNextTile(Clicks click, int x, int y, boolean clear) {
        int toReturn;
        switch (click){
            case LEFT:
                for(int newX = x + 1; newX < gridSize; newX++){
                    if(grid[newX][y] != 0){
                        toReturn = grid[newX][y];
                        if(clear)grid[newX][y] = 0;
                        return toReturn;
                    }
                }
                break;
            case DOWN:
                for(int newY = y - 1; newY >= 0; newY--){
                    if(grid[x][newY] != 0){
                        toReturn = grid[x][newY];
                        if(clear)grid[x][newY] = 0;
                        return toReturn;
                    }
                }
                break;

            case RIGHT:
                for(int newX = x - 1; newX >= 0; newX--){
                    if(grid[newX][y] != 0){
                        toReturn = grid[newX][y];
                        if(clear)grid[newX][y] = 0;
                        return toReturn;
                    }
                }
                break;

            case UP:
                for(int newY = y + 1; newY < gridSize; newY++){
                    if(grid[x][newY] != 0){
                        toReturn = grid[x][newY];
                        if(clear)grid[x][newY] = 0;
                        return toReturn;
                    }
                }
                break;
        }
        return 0;
    }

    public void onClick(InventoryClickEvent event){
        if(event.getCurrentItem() == null) return;
        if(over) return;
        ItemStack item = event.getCurrentItem();
        if(item.isSimilar(this.left)){
            onClick(Clicks.LEFT);
            return;
        } else if(item.isSimilar(this.right)){
            onClick(Clicks.RIGHT);
            return;
        } else if(item.isSimilar(this.up)){
            onClick(Clicks.UP);
            return;
        } else if(item.isSimilar(this.down)){
            onClick(Clicks.DOWN);
            return;
        }
    }

    public void onClick(Clicks click){
        plugin.debug("clicked");
        switch (click){
            case LEFT:
                plugin.debug("moving left");
                if(moveLeft()){
                    spawn();
                    build();
                }
                break;
            case DOWN:
                plugin.debug("moving down");
                if(moveDown()){
                    spawn();
                    build();
                }
                break;

            case RIGHT:
                plugin.debug("moving right");
                if(moveRight()){
                    spawn();
                    build();
                }
                break;

            case UP:
                plugin.debug("moving up");
                if(moveUp()){
                    spawn();
                    build();
                }
                break;
        }
    }

}
