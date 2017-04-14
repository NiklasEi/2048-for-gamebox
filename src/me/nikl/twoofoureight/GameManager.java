package me.nikl.twoofoureight;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.game.IGameManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by Niklas on 14.04.2017.
 *
 * 2048s GameManager
 */

public class GameManager implements IGameManager {
    private Main plugin;

    private Map<UUID, Game> games = new HashMap<>();
    private Language lang;

    private Statistics statistics;

    private Map<Integer, ItemStack> items = new HashMap<>();


    private Map<String,GameRules> gameTypes;


    public GameManager(Main plugin){
        this.plugin = plugin;
        this.lang = plugin.lang;

        this.statistics = plugin.gameBox.getStatistics();

        loadTiles();
    }

    private void loadTiles() {
        if(!plugin.getConfig().isConfigurationSection("tiles")){
            Bukkit.getLogger().log(Level.SEVERE, "Configuration error.. cannot find any tiles");
            return;
        }

        ConfigurationSection tiles = plugin.getConfig().getConfigurationSection("tiles");

        int counter = 1;
        for(String key : tiles.getKeys(false)){
            if(!tiles.isString(key + ".materialData")){
                Bukkit.getLogger().log(Level.SEVERE, "Configuration error in the tile: " + key);
                continue;
            }
            ItemStack item = plugin.getItemStack(tiles.getString(key + ".materialData"));

            items.put(counter, item);
        }
    }


    @Override
    public boolean onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        // ToDo
        return false;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        // ToDo
        return false;
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }

    @Override
    public int startGame(Player[] players, boolean b, String... strings) {
        games.put(players[0].getUniqueId(), new Game(gameTypes.values().iterator().next(), plugin, players[0], items));
        return GameBox.GAME_STARTED;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        // ToDo
    }

    public void setGameTypes(Map<String, GameRules> gameTypes) {
        this.gameTypes = gameTypes;
    }


}
