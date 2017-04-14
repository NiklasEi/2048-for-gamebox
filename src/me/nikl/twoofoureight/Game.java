package me.nikl.twoofoureight;

import com.sun.org.apache.xml.internal.utils.IntVector;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Niklas on 14.04.2017.
 */
public class Game {

    private GameRules rule;
    private boolean playSounds;
    private Language lang;

    private Map<Integer, ItemStack> items;

    private Inventory inventory;

    private Player player;


    public Game(GameRules rule, Main plugin, Player player, Map<Integer, ItemStack> items){
        this.rule = rule;
        this.playSounds = plugin.getPlaySounds();
        this.lang = plugin.lang;

        this.player = player;

        this.items = items;

        this.inventory = Bukkit.createInventory(null, 54, lang.GAME_TITLE);

        for(ItemStack item : items.values()){
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }




}
