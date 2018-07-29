package me.nikl.gamebox.games.twoofoureight;

import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.game.exceptions.GameStartException;
import me.nikl.gamebox.game.manager.EasyManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.ItemStackUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Niklas on 14.04.2017.
 *
 * 2048s GameManager
 */

public class GameManager extends EasyManager {
    private Tofe tofe;
    private Map<UUID, Game> games = new HashMap<>();
    private Language lang;
    private DataBase statistics;
    private Map<Integer, ItemStack> items = new HashMap<>();
    private Map<String,GameRules> gameTypes = new HashMap<>();
    private boolean topNav, surroundGrid;
    private ItemStack surroundItemStack;

    public GameManager(Tofe tofe){
        this.tofe = tofe;
        this.lang = (Language) tofe.getGameLang();
        this.statistics = tofe.getGameBox().getDataBase();
        loadTiles();
        this.topNav = tofe.getConfig().getBoolean("rules.topNavigation", false);
        this.surroundGrid = tofe.getConfig().getBoolean("rules.surroundTheGrid.enable", true);
        surroundItemStack = ItemStackUtility.loadItem(tofe.getConfig().getConfigurationSection("rules.surroundTheGrid"));
    }

    private void loadTiles() {
        if(!tofe.getConfig().isConfigurationSection("tiles")){
            Bukkit.getLogger().log(Level.SEVERE, "Configuration error.. cannot find any tiles");
            return;
        }
        // in items 0 the navigation item is saved
        ItemStack nav = ItemStackUtility.getItemStack(tofe.getConfig().getString("buttons.navigation.materialData", "ARROW"));
        if(nav == null){
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Wrong configured navigation button");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "   Please set \"buttons.navigation.materialData\" to a valid value");
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + "   using default nav button now");
            nav = new ItemStack(Material.ARROW, 1);
        } else if(tofe.getConfig().getBoolean("buttons.navigation.glow")){
            nav = NmsFactory.getNmsUtility().addGlow(nav);
        }
        items.put(0, nav);
        ConfigurationSection tiles = tofe.getConfig().getConfigurationSection("tiles");
        int counter = 1;
        String displayName;
        List<String> lore;
        for(String key : tiles.getKeys(false)){
            if(!tiles.isString(key + ".materialData")){
                Bukkit.getLogger().log(Level.SEVERE, "Configuration error in the tile: " + key);
                continue;
            }
            ItemStack item = ItemStackUtility.getItemStack(tiles.getString(key + ".materialData"));
            if (item == null) {
                tofe.warn("Tile '" + key + "' cannot be loaded.");
                continue;
            }
            if(tiles.getBoolean(key + ".glow")){
                item = NmsFactory.getNmsUtility().addGlow(item);
            }
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
            tofe.debug(" load item Nr. " + counter + "   it is: " + item.toString());
            items.put(counter, item);
            counter ++;
        }
    }

    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    @Override
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        tofe.debug(" click in manager    rawslot: " + inventoryClickEvent.getRawSlot());
        games.get(inventoryClickEvent.getWhoClicked().getUniqueId()).onClick(inventoryClickEvent);
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        removeFromGame(inventoryCloseEvent.getPlayer().getUniqueId());
    }

    @Override
    public boolean isInGame(UUID uuid) {
        return games.containsKey(uuid);
    }

    @Override
    public void startGame(Player[] players, boolean playSounds, String... strings) throws GameStartException {
        if (strings.length != 1) {
            Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        GameRules rule = gameTypes.get(strings[0]);
        if (rule == null) {
            Bukkit.getLogger().log(Level.WARNING, " unknown argument to start a game: " + Arrays.asList(strings));
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        if (!pay(players, rule.getCost())) {
            throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
        }

        games.put(players[0].getUniqueId(), new Game(rule, tofe, players[0], items, playSounds, topNav, surroundGrid, surroundItemStack));
    }

    @Override
    public void removeFromGame(UUID uuid) {
        games.get(uuid).onGameEnd();
        games.remove(uuid);
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        double cost = buttonSec.getDouble("cost", 0.);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        gameTypes.put(buttonID, new GameRules(tofe, buttonID, cost, saveStats));
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameTypes;
    }


    private boolean pay(Player[] player, double cost) {
        return tofe.payIfNecessary(player[0], cost);
    }
}
