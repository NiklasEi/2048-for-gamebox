package me.nikl.gamebox.games;

import me.nikl.gamebox.games.twoofoureight.Tofe;
import me.nikl.gamebox.module.GameBoxModule;

/**
 * Created by Niklas on 14.04.2017.
 *
 * TofeMain class for the GameBox game 2048
 */
public class TofeMain extends GameBoxModule {
    public static final String TWO_O_FOUR_EIGHT = "twoofoureight";

    @Override
    public void onEnable() {
        registerGame(TWO_O_FOUR_EIGHT, Tofe.class, "tofe");
    }

    @Override
    public void onDisable() {

    }
}
