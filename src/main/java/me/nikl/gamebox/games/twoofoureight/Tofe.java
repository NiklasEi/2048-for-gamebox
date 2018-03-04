package me.nikl.gamebox.games.twoofoureight;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameSettings;
import me.nikl.gamebox.games.TofeMain;

/**
 * @author Niklas Eicker
 */
public class Tofe extends me.nikl.gamebox.game.Game {
    public Tofe(GameBox gameBox) {
        super(gameBox, TofeMain.TWO_O_FOUR_EIGHT);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {

    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.SINGLE_PLAYER);
        gameSettings.setHandleClicksOnHotbar(false);
        gameSettings.setGameGuiSize(54);
    }

    @Override
    public void loadLanguage() {
        gameLang = new Language(this);
    }

    @Override
    public void loadGameManager() {
        gameManager = new GameManager(this);
    }
}
