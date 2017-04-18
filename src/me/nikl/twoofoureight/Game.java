package me.nikl.twoofoureight;

import com.sun.org.apache.xml.internal.utils.IntVector;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.data.SaveType;
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

import java.util.*;

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

    private Sounds gameOver = Sounds.ANVIL_LAND, combined = Sounds.ITEM_PICKUP, shift = Sounds.WOLF_WALK, no = Sounds.VILLAGER_NO;

    private float volume = 0.5f, pitch= 1f;

    private ItemStack left, right, up, down;

    public Game(GameRules rule, Main plugin, Player player, Map<Integer, ItemStack> items, boolean playSounds){
        this.plugin = plugin;
        this.rule = rule;
        this.playSounds = plugin.getPlaySounds() && playSounds;
        this.lang = plugin.lang;

        this.player = player;

        this.random = new Random(System.currentTimeMillis());

        this.items = new HashMap<>(items);

        this.left = items.get(0).clone();
        this.right = items.get(0).clone();
        this.up = items.get(0).clone();
        this.down = items.get(0).clone();

        this.items.remove(0);

        ItemMeta meta = left.getItemMeta();
        meta.setDisplayName(lang.GAME_BUTTON_LEFT);
        this.left.setItemMeta(meta);

        meta = right.getItemMeta();
        meta.setDisplayName(lang.GAME_BUTTON_RIGHT);
        this.right.setItemMeta(meta);

        meta = up.getItemMeta();
        meta.setDisplayName(lang.GAME_BUTTON_UP);
        this.up.setItemMeta(meta);

        meta = down.getItemMeta();
        meta.setDisplayName(lang.GAME_BUTTON_DOWN);
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
        spawn();
        player.openInventory(inventory);

        player.getOpenInventory().getBottomInventory().setItem(13, up);
        player.getOpenInventory().getBottomInventory().setItem(21, left);
        player.getOpenInventory().getBottomInventory().setItem(23, right);
        player.getOpenInventory().getBottomInventory().setItem(31, down);

        build();
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
            onGameEnd();
            over = true;
            if(playSounds)player.playSound(player.getLocation(), gameOver.bukkitSound(), volume, pitch);
            plugin.debug("game is over");
        }
    }


    private int getKey(int score){
        int distance = -1;
        for(int key : rule.getMoneyRewards().keySet()) {
            if((score - key) >= 0 && (distance < 0 || distance > (score - key))){
                distance = score - key;
            }
        }
        if(distance > -1)
            return score - distance;
        return -1;
    }

    void onGameEnd(){
        if(player == null || over) return;
        int key = getKey(score);
        if(Main.debug) Bukkit.getConsoleSender().sendMessage("Key in onGameEnd: " + key);
        if(Main.debug)Bukkit.getConsoleSender().sendMessage("pay: " + rule.getMoneyRewards().get(key)+ "     token: " + rule.getTokenRewards().get(key));

        // score intervals could be empty or not configured
        if(key < 0){
            player.sendMessage(lang.PREFIX + lang.GAME_OVER_NO_PAY.replaceAll("%score%", score +""));
            return;
        }

        if(plugin.isEconEnabled() && !player.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && rule.getMoneyRewards().get(key) > 0.0){
            Main.econ.depositPlayer(player, rule.getMoneyRewards().get(key));
            player.sendMessage(lang.PREFIX + lang.GAME_WON_MONEY.replace("%reward%", rule.getMoneyRewards().get(key)+"").replace("%score%", score+""));
        } else {
            player.sendMessage(lang.PREFIX + lang.GAME_OVER_NO_PAY.replace("%score%", score+""));
        }
        if(rule.isSaveStats()){
            plugin.getGameManager().getStatistics().addStatistics(player.getUniqueId(), Main.gameID, rule.getKey(), (double) score, SaveType.SCORE);
        }
        if(rule.getTokenRewards().get(key) > 0){
            plugin.gameBox.wonTokens(player.getUniqueId(), rule.getTokenRewards().get(key), Main.gameID);
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
        boolean changed =  false, played = false;
        for (int y = 0; y < gridSize;y++){
            for (int x = 0; x < gridSize - 1;x++){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.LEFT, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.LEFT, x, y, true);
                        if(playSounds)player.playSound(player.getLocation(), combined.bukkitSound(), volume, pitch);
                        played = true;
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
                        if(!played && playSounds)player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
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
        boolean changed =  false, played = false;
        for (int y = 0; y < gridSize;y++){
            for (int x = gridSize - 1; x >= 0 ;x--){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.RIGHT, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.RIGHT, x, y, true);
                        if(playSounds)player.playSound(player.getLocation(), combined.bukkitSound(), volume, pitch);
                        played = true;
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
                        if(!played && playSounds)player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
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
        boolean changed =  false, played = false;
        for (int x = 0; x < gridSize;x++){
            for (int y = 0; y < gridSize - 1;y++){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.UP, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.UP, x, y, true);
                        if(playSounds)player.playSound(player.getLocation(), combined.bukkitSound(), volume, pitch);
                        played = true;
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
                        if(!played && playSounds)player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
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
        boolean changed =  false, played = false;
        for (int x = 0; x < gridSize;x++){
            for (int y = gridSize - 1; y >=0;y--){
                if(grid[x][y] == 0) continue;
                if(grid[x][y] == getNextTile(Clicks.DOWN, x, y, false)){
                    toMerge.add(x+gridSize*y);
                    if(set) {
                        grid[x][y]++;
                        getNextTile(Clicks.DOWN, x, y, true);
                        if(playSounds)player.playSound(player.getLocation(), combined.bukkitSound(), volume, pitch);
                        played = true;
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
                        if(!played && playSounds)player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
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
                } else {
                    if(playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                }
                break;
            case DOWN:
                plugin.debug("moving down");
                if(moveDown()){
                    spawn();
                    build();
                } else {
                    if(playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                }
                break;

            case RIGHT:
                plugin.debug("moving right");
                if(moveRight()){
                    spawn();
                    build();
                } else {
                    if(playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                }
                break;

            case UP:
                plugin.debug("moving up");
                if(moveUp()){
                    spawn();
                    build();
                } else {
                    if(playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                }
                break;
        }
    }

}
