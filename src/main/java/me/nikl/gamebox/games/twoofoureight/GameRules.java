package me.nikl.gamebox.games.twoofoureight;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.rules.GameRuleMultiRewards;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules extends GameRuleMultiRewards {
    private boolean undoLastMove;

    public GameRules(Tofe game, String key, double cost, boolean saveStats, boolean undoLastMove){
        super(key, saveStats, SaveType.SCORE, cost);
        loadRewards(game);
        this.undoLastMove = undoLastMove;
    }

    private void loadRewards(Tofe game) {
        if(!game.getConfig().isConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals")) return;
        ConfigurationSection onGameEnd = game.getConfig().getConfigurationSection("gameBox.gameButtons." + key + ".scoreIntervals");
        for (String key : onGameEnd.getKeys(false)) {
            int keyInt;
            try {
                keyInt = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning(game.getGameLang().PREFIX + " NumberFormatException while getting the rewards from config!");
                continue;
            }
            if (onGameEnd.isSet(key + ".money") && (onGameEnd.isDouble(key + ".money") || onGameEnd.isInt(key + ".money"))) {
                addMoneyReward(keyInt, onGameEnd.getDouble(key + ".money"));
            } else {
                addMoneyReward(keyInt, 0.);
            }
            if (onGameEnd.isSet(key + ".tokens") && (onGameEnd.isDouble(key + ".tokens") || onGameEnd.isInt(key + ".tokens"))) {
                addTokenReward(keyInt, onGameEnd.getInt(key + ".tokens"));
            } else {
                addTokenReward(keyInt, 0);
            }
        }
    }

    public boolean isUndoLastMove() {
        return undoLastMove;
    }
}
