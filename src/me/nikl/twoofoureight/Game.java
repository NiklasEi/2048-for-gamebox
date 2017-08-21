package me.nikl.twoofoureight;

import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.Sounds;
import me.nikl.gamebox.data.SaveType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Niklas on 14.04.2017.
 *
 *
 */
public class Game extends BukkitRunnable{

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

    private Status status = Status.PLAY;

    @Override
    public void run() {
        plugin.debug("run");
        if(over) return;
        switch (status){
            case PLAY:
                return;
            case LEFT:
                moveOneLeft();
                if(status != Status.LEFT){
                    spawn();
                }
                build();

                break;
            case DOWN:
                moveOneDown();
                if(status != Status.DOWN){
                    spawn();
                }
                build();

                break;

            case RIGHT:
                moveOneRight();
                if(status != Status.RIGHT){
                    spawn();
                }
                build();
                break;

            case UP:
                moveOneUp();
                if(status != Status.UP){
                    spawn();
                }
                build();

                break;
        }
    }

    public enum Clicks{
        LEFT, RIGHT, UP, DOWN
    }

    private enum Status{
        PLAY, LEFT, RIGHT, UP, DOWN
    }

    private Sounds gameOver = Sounds.ANVIL_LAND, combinedSound = Sounds.ITEM_PICKUP, shift = Sounds.WOLF_WALK, no = Sounds.VILLAGER_NO;

    private float volume = 0.5f, pitch= 1f;

    private ItemStack left, right, up, down;

    private Set<Integer> combined = new HashSet<>();

    public Game(GameRules rule, Main plugin, Player player, Map<Integer, ItemStack> items, boolean playSounds, boolean topNav, boolean surroundGrid, ItemStack surroundItemStack){
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

        String title= lang.GAME_TITLE.replace("%score%", String.valueOf(score));
        if(GameBoxSettings.checkInventoryLength && title.length() > 32){
            title = "Title is too long!";
        }
        this.inventory = Bukkit.createInventory(null, 54, title);

        if(surroundGrid){
            for(int i = 0; i<inventory.getSize(); i++){
                inventory.setItem(i, surroundItemStack);
            }
        }


        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                grid[x][y] = 0;
                inventory.setItem(y*9 + x + position, null);
                if(topNav) {
                    if (y == 0) {
                        inventory.setItem(position + (y - 1) * 9 + x, this.up);
                    } else if (y == gridSize - 1) {
                        inventory.setItem(position + (y + 1) * 9 + x, this.down);
                    }
                    if (x == 0) {
                        inventory.setItem(position + (y) * 9 + x - 1, this.left);
                    } else if (x == gridSize - 1) {
                        inventory.setItem(position + (y) * 9 + x + 1, this.right);
                    }
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

        this.runTaskTimer(plugin, 0, 3);
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
        this.cancel();
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

    private boolean moveLeft(boolean set){
        for (int y = 0; y < gridSize;y++){
            for (int x = 1; x < gridSize;x++) {
                if((grid[x][y] != 0 && grid[x-1][y] == 0)
                        || (grid[x][y] != 0 && grid[x][y] == grid[x-1][y] && !combined.contains(x+y*gridSize) && !combined.contains(x-1+y*gridSize))){
                    if(set)this.status = Status.LEFT;
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOneLeft(){
        boolean playCombined = false;
        for (int y = 0; y < gridSize;y++){
            for (int x = 1; x < gridSize;x++) {
                if(grid[x][y] != 0 && grid[x-1][y] == 0){
                    grid[x-1][y] = grid[x][y];
                    grid[x][y] = 0;
                } else if (grid[x][y] != 0 && grid[x][y] == grid[x-1][y] && !combined.contains(x+y*gridSize) && !combined.contains(x-1+y*gridSize)){
                    grid[x][y] = 0;
                    grid[x-1][y] ++;
                    combined.add(x-1 + y*gridSize);
                    playCombined = true;
                }
            }
        }
        if(playSounds){
            if(playCombined) {
                player.playSound(player.getLocation(), combinedSound.bukkitSound(), volume, pitch);
            } else {
                player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
            }
        }
        if(!moveLeft(false)){
            this.status = Status.PLAY;
            combined.clear();
        }
    }

    private boolean moveRight(boolean set){
        for (int y = 0; y < gridSize;y++){
            for (int x = gridSize - 2; x >= 0 ;x--){
                if((grid[x][y] != 0 && grid[x+1][y] == 0) || (grid[x][y] != 0 && grid[x][y] == grid[x+1][y] && !combined.contains(x+y*gridSize) && !combined.contains(x+1+y*gridSize))){
                    if(set)this.status = Status.RIGHT;
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOneRight(){
        boolean playCombined = false;
        for (int y = 0; y < gridSize;y++){
            for (int x = gridSize - 2; x >= 0 ;x--){
                if(grid[x][y] != 0 && grid[x+1][y] == 0){
                    grid[x+1][y] = grid[x][y];
                    grid[x][y] = 0;
                } else if (grid[x][y] != 0 && grid[x][y] == grid[x+1][y] && !combined.contains(x+y*gridSize) && !combined.contains(x+1+y*gridSize)){
                    grid[x][y] = 0;
                    grid[x+1][y] ++;
                    combined.add(x+1 + y*gridSize);
                    playCombined = true;
                }
            }
        }
        if(playSounds){
            if(playCombined) {
                player.playSound(player.getLocation(), combinedSound.bukkitSound(), volume, pitch);
            } else {
                player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
            }
        }
        if(!moveRight(false)){
            this.status = Status.PLAY;
            combined.clear();
        }
    }

    private boolean moveUp(boolean set){
        for (int x = 0; x < gridSize;x++){
            for (int y = 1; y < gridSize;y++){
                if((grid[x][y] != 0 && grid[x][y-1] == 0) || (grid[x][y] != 0 && grid[x][y] == grid[x][y-1] && !combined.contains(x+y*gridSize) && !combined.contains(x+(y-1)*gridSize))){
                    if(set) this.status = Status.UP;
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOneUp(){
        boolean playCombined = false;
        for (int x = 0; x < gridSize;x++){
            for (int y = 1; y < gridSize;y++){
                if(grid[x][y] != 0 && grid[x][y-1] == 0){
                    grid[x][y-1] = grid[x][y];
                    grid[x][y] = 0;
                } else if (grid[x][y] != 0 && grid[x][y] == grid[x][y-1] && !combined.contains(x+y*gridSize) && !combined.contains(x+(y-1)*gridSize)){
                    grid[x][y] = 0;
                    grid[x][y-1] ++;
                    combined.add(x + (y-1)*gridSize);
                    playCombined = true;
                }
            }
        }
        if(playSounds){
            if(playCombined) {
                player.playSound(player.getLocation(), combinedSound.bukkitSound(), volume, pitch);
            } else {
                player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
            }
        }
        if(!moveUp(false)){
            this.status = Status.PLAY;
            combined.clear();
        }
    }


    private boolean moveDown(boolean set){
        for (int x = 0; x < gridSize;x++){
            for (int y = gridSize - 2; y >=0;y--){
                if(grid[x][y] != 0 && grid[x][y+1] == 0){
                    if(set)this.status = Status.DOWN;
                    return true;
                } else if (grid[x][y] != 0 && grid[x][y] == grid[x][y+1] && !combined.contains(x+y*gridSize) && !combined.contains(x+(y+1)*gridSize)){
                    plugin.debug("continue because of   x: " + x + "   y: " + y);
                    if(set)this.status = Status.DOWN;
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOneDown(){
        boolean playCombined = false;
        for (int x = 0; x < gridSize;x++){
            for (int y = gridSize - 2; y >=0;y--){
                if(grid[x][y] != 0 && grid[x][y+1] == 0){
                    grid[x][y+1] = grid[x][y];
                    grid[x][y] = 0;
                } else if (grid[x][y] != 0 && grid[x][y] == grid[x][y+1] && !combined.contains(x+y*gridSize) && !combined.contains(x+(y+1)*gridSize)){
                    grid[x][y] = 0;
                    grid[x][y+1] ++;
                    combined.add(x + (y+1)*gridSize);
                    playCombined = true;
                }
            }
        }
        if(playSounds){
            if(playCombined) {
                player.playSound(player.getLocation(), combinedSound.bukkitSound(), volume, pitch);
            } else {
                player.playSound(player.getLocation(), shift.bukkitSound(), volume, pitch);
            }
        }
        if(!moveDown(false)){
            this.status = Status.PLAY;
            combined.clear();
        }
    }


    public void onClick(InventoryClickEvent event){
        if(event.getCurrentItem() == null) return;
        if(over) return;
        if(status != Status.PLAY) return;
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

                if(!moveLeft(true) && playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);

                break;
            case DOWN:
                plugin.debug("moving down");
                if(!moveDown(true) && playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                break;

            case RIGHT:
                plugin.debug("moving right");
                if(!moveRight(true) && playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                break;

            case UP:
                plugin.debug("moving up");
                if(!moveUp(true) && playSounds)player.playSound(player.getLocation(), no.bukkitSound(), volume, pitch);
                break;
        }
    }

}
