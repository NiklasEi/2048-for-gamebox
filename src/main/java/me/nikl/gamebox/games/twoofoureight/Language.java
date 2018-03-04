package me.nikl.gamebox.games.twoofoureight;

import me.nikl.gamebox.game.GameLanguage;

public class Language extends GameLanguage {
	public String GAME_TITLE, GAME_TITLE_LOST
			, GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY, GAME_OVER_NO_PAY
			, GAME_BUTTON_UP, GAME_BUTTON_DOWN, GAME_BUTTON_RIGHT, GAME_BUTTON_LEFT;
	
	public Language(Tofe game){
		super(game);
	}

	private void getGameMessages() {
		this.GAME_PAYED = getString("game.econ.payed");
		this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
		this.GAME_WON_MONEY = getString("game.econ.wonMoney");
		this.GAME_OVER_NO_PAY = getString("game.gameOverNoPay");
		this.GAME_TITLE = getString("game.inventoryTitles.gameTitle");
		this.GAME_TITLE_LOST = getString("game.inventoryTitles.lost");
		this.GAME_BUTTON_UP = getString("game.buttons.up");
		this.GAME_BUTTON_DOWN = getString("game.buttons.down");
		this.GAME_BUTTON_RIGHT = getString("game.buttons.right");
		this.GAME_BUTTON_LEFT = getString("game.buttons.left");
	}

	@Override
	protected void loadMessages() {
		getGameMessages();
	}
}

