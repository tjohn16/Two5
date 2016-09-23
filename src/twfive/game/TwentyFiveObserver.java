package twfive.game;


public interface TwentyFiveObserver 
{

	
	/* ---------------------------------------------------------------------
	 * --- TwentyFive Observer
	 * ---------------------------------------------------------------------
	 */
	
	// Tracks when sets are won/drawn, and when a match is won
	public void onSetWon(Player winner);
	public void onSetDraw();
	public void onMatchWon(Player winner);
	
	// Tracks when player's turn ends, when a player has been dealt a card, and when a player uses a card
	public void onPlayerTurnChanged(int playerTurn);
	public void onPlayerCardDealt(int playerTurn);
	public void onPlayerCardUsed(Player player);
}