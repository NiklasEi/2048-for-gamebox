package me.nikl.twoofoureight;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.data.Statistics;
import me.nikl.gamebox.game.IGameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        String displayName;
        List<String> lore;
        for(String key : tiles.getKeys(false)){
            if(!tiles.isString(key + ".materialData")){
                Bukkit.getLogger().log(Level.SEVERE, "Configuration error in the tile: " + key);
                continue;
            }
            ItemStack item = plugin.getItemStack(tiles.getString(key + ".materialData"));

            ItemMeta meta = item.getItemMeta();

            if(tiles.isString(key + ".displayName")){
                displayName = chatColor(tiles.getString(key + ".displayName"));
                meta.setDisplayName(displayName);
            }

            if(tiles.isList(key + ".lore")){
                lore = new ArrayList<>(tiles.getStringList(key + ".lore"));
                for(int i = 0; i < lore.size();i++){
                    lore.set(i, chatColor(lore.get(i)));
                }
                meta.setLore(lore);
            }

            item.setItemMeta(meta);

            plugin.debug(" load item Nr. " + counter + "   it is: " + item.toString());

            items.put(counter, item);
            counter ++;
        }
    }

    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    @Override
    public boolean onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        // ToDo
        games.get(inventoryClickEvent.getWhoClicked().getUniqueId()).onClick(inventoryClickEvent);
        return true;
    }

    @Override
    public boolean onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        games.remove(inventoryCloseEvent.getPlayer().getUniqueId());
        return true;
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }

    @Override
    public int startGame(Player[] players, boolean b, String... strings) {
        if(strings.length != 1){
            Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }
        GameRules rule = gameTypes.get(strings[0]);
        if(rule == null){
            Bukkit.getLogger().log(Level.WARNING, " unknown argument to start a game: " + Arrays.asList(strings));
            return GameBox.GAME_NOT_STARTED_ERROR;
        }
        if(!pay(players, rule.getCost())){
            return GameBox.GAME_NOT_ENOUGH_MONEY;
        }

        games.put(players[0].getUniqueId(), new Game(rule, plugin, players[0], items));
        return GameBox.GAME_STARTED;
    }

    @Override
    public void removeFromGame(UUID uuid) {
        games.remove(uuid);
    }

    public void setGameTypes(Map<String, GameRules> gameTypes) {
        this.gameTypes = gameTypes;
    }



    private boolean pay(Player[] player, double cost) {
        if (plugin.isEconEnabled() && !player[0].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player[0].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
            if (Main.econ.getBalance(player[0]) >= cost) {
                Main.econ.withdrawPlayer(player[0], cost);
                player[0].sendMessage(chatColor(lang.PREFIX + plugin.lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
                return true;
            } else {
                player[0].sendMessage(chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
                return false;
            }
        } else {
            return true;
        }
    }
}
