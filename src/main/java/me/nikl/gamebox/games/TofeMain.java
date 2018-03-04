package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Niklas on 14.04.2017.
 *
 * TofeMain class for the GameBox game 2048
 */
public class TofeMain extends JavaPlugin {
    private GameBox gameBox;
    public static final String TWO_O_FOUR_EIGHT = "twoofoureight";

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GameBox");
        if(plugin == null || !plugin.isEnabled()){
            getLogger().warning(" GameBox was not found! Disabling LogicPuzzles...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        gameBox = (GameBox) plugin;
        new Module(gameBox, TWO_O_FOUR_EIGHT
                , "me.nikl.gamebox.games.twoofoureight.Tofe"
                , this, TWO_O_FOUR_EIGHT, "2048", "tofe");
    }
}
