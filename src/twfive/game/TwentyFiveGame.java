package twfive.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TwentyFiveGame
{

	
	
	/* ---------------------------------------------------------------------
	 * Game Rules
	 * 
	 * To win a match, 3 sets must be won
	 * 		A match can have unlimited number of sets played
	 * 		A match can have a set draw
	 * 		A match is played until either player has won 3 sets.
	 * 
	 * The closest player to a sum of 25 wins a set. There are exceptions to this case:
	 * 		exception: Tiebreaker card played wins set if both players have equal values
	 * 		exception: 9 cards played out on the field wins a set regardless of tiebreaker and sum.
	 * 		exception: sum above 25 loses set
	 * 		exception: Equal sums result in a draw. Set is replayed. Any cards used in a draw are NOT returned.
	 * ---------------------------------------------------------------------
	 */
	
	
	/* ---------------------------------------------------------------------
	 * Globals
	 * ---------------------------------------------------------------------
	 */ 

	public static final int MAX_PLAYERS = 2; // Can be at most two players
	
	public static final int SUM_LIMIT = 25; // Sets end sum to 25.
	public static final int CARD_LIMIT = 9; // 9 card limit to play
	
	
	
	/* ---------------------------------------------------------------------
	 * Variables
	 * ---------------------------------------------------------------------
	 */ 
	
	private int playerTurn; // Is it the computer or player's turn?
	private int playerTurnStartLS;
	private boolean gameOngoing; // True until game is won
	
	private final List<Player> players;
	
	private final List<TwentyFiveObserver> observers;
	
	private final Random randomGen;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public TwentyFiveGame(boolean AI) 
	{
		randomGen = new Random();
		randomGen.setSeed(System.currentTimeMillis());
		
		this.playerTurn = randomGen.nextInt(2); // Player start
		this.playerTurnStartLS = this.playerTurn;
		this.gameOngoing = true; // Game hasn't been won yet
		
		this.observers = new ArrayList<TwentyFiveObserver>();
		
		this.players = new ArrayList<Player>();
		
		// Names can be changed and personalized
		if (AI) 
		{
			// Names for Player vs AI
			this.players.add(new Player(this, players.size(), "Player"));
			this.players.add(new Player(this, players.size(), "Computer"));
			new AI(this, getPlayer(1));
		}
		else 
		{
			// Names for Player vs Player
			this.players.add(new Player(this, players.size(), "Player 1"));
			this.players.add(new Player(this, players.size(), "Player 2"));
		}
	}

	public Player getPlayer(int index) 
	{
		return players.get(index);
	}
	
	public int getPlayerTurn() 
	{
		// Returns whose turn it is
		return playerTurn;
	}
	
	public boolean isGameOngoing() 
	{
		// Returns if the game has been won
		return gameOngoing;
	}


	public void addObserver(TwentyFiveObserver observe) 
	{
		// Add an observer to the game
		if (!observers.contains(observe)) 
		{
			observers.add(observe);
		}
	}
	
	public void removeObserver(TwentyFiveObserver observe) 
	{
		// Remove an observer from the game
		if (observers.contains(observe)) 
		{
			observers.remove(observe);
		}
	}
	
	/* ---------------------------------------------------------------------
	 * Methods to notify Observers
	 * ---------------------------------------------------------------------
	 */
	
	// Observers notified turn has changed
	private void notifyOnPlayerTurnChanged() 
	{
		for (int i = 0; i < observers.size(); i++) 
		{ 
			// AI is updated before GUI
			observers.get(i).onPlayerTurnChanged(playerTurn); // Observers are notified
		}
	}
	// Observers notified card has been dealt 
	private void notifyOnPlayerCardDealt() 
	{
		for (int i = 0; i < observers.size(); i++) 
		{ 
			// AI is updated before GUI
			observers.get(i).onPlayerCardDealt(playerTurn);  // Observers are notified
		}
	}
	// Observers notified card has been used
	private void notifyOnPlayerCardUsed(Player player) 
	{
		for (int i = 0; i < observers.size(); i++) 
		{ 
			// AI is updated before GUI
			observers.get(i).onPlayerCardUsed(player); // Observers are notified
		}
	}
	// Observers notified set has been won
	private void notifyOnSetWon(Player winner) 
	{
		for (int i = 0; i < observers.size(); i++) 
		{ 
			// AI is updated before GUI
			observers.get(i).onSetWon(winner);  // Observers are notified
		}
	}
	// Observers notified set has been drawn
	private void notifyOnSetDraw() 
	{
		for (int i = 0; i < observers.size(); i++) 
		{  
			// AI is updated before GUI
			observers.get(i).onSetDraw();  // Observers are notified
		}
	}
	// Observers notified match has been won
	private void notifyOnMatchWon(Player winner) 
	{
		for (int i = 0; i < observers.size(); i++) 
		{  
			// AI is updated before GUI
			observers.get(i).onMatchWon(winner);   // Observers are notified
		}
	}
	
	public HandCard createHandCard() 
	{
		// Creates cards in the players' hands.
		// Cards are generated randomly based on rarity.
		// Descending rarity list: Tiebreaker, Flippable, Stack-Flippable, Double, Negative, Positive
		
		double rarity = randomGen.nextDouble();
		if (rarity > 0.92) 
		{ 
			// Card is a tiebreaker
			return new HandCard(CardNames.TIEBREAKER, 1);
		} 
		else if (rarity > 0.85) 
		{ 
			// Card is flippable
			return new HandCard(
					CardNames.FLIPPABLE,
					Card.FLIPPABLEMIN + (int)(Math.random() * ((Card.FLIPPABLEMAX - Card.FLIPPABLEMIN) + 1))
			);
		} 
		else if (rarity > 0.70) 
		{ 
			// Flips the entirety of these card values on the stack (1&5, 2&4, 3&6) 
			if (Math.random() > 0.65) 
			{
				return new HandCard(CardNames.TWO_AND_FOUR, 0);
			}
			else if(Math.random() > 0.60 && Math.random() < 0.649)
			{
				return new HandCard(CardNames.ONE_AND_FIVE,0);
			}
			else
			{
				return new HandCard(CardNames.THREE_AND_SIX, 0);
			}
		}
		else if (rarity > 0.55) 
		{ 
			// Card doubles previous value on the game field
			return new HandCard(CardNames.DOUBLE, 0);

			
		} 
		else 
		{
			// Card is not special
			int value;
			if (Math.random() >= 0.3) 
			{ 
				// More likely to get a positive card then a negative
				value = Card.HANDMIN + (int)(Math.random() * ((Card.HANDMAX - Card.HANDMIN) + 1));
			} 
			else 
			{
				value = - (Card.HANDMIN + (int)(Math.random() * ((Card.HANDMAX - Card.HANDMIN) + 1)));
			}
			return new HandCard(CardNames.NORMAL, value);
		}
	}
	
	

	/* ---------------------------------------------------------------------
	 * Set victory helper method
	 * ---------------------------------------------------------------------
	 */
	
	private void setWinner(Player winner) 
	{
		if (!gameOngoing) 
		{ 
			return; 
		}
		
		winner.setWinner();

		playerTurn = (playerTurnStartLS == 0 ? 1 : 0); 
		playerTurnStartLS = playerTurn;
		// Alternates who starts each set
		
		// Three sets won, match is over, end the game
		if (winner.getSetsWon() == 3) 
		{
			gameOngoing = false;
			notifyOnMatchWon(winner);
			return;
		}
		for (Player p : players) 
		{
			p.newSet(); // Players are given new sets
		}
		notifyOnSetWon(winner);
	}
	
	/* ---------------------------------------------------------------------
	 * --- on Player events (package-only)
	 * ---------------------------------------------------------------------
	 */
	
	void onPlayerCardDealt() 
	{
		notifyOnPlayerCardDealt();
	}
	
	
	void onPlayerStand(Player player) 
	{
		if (!gameOngoing) 
		{ 
			return; 
		}
		
		// If the player has reached the card limit (9) and filled the game board, it is an automatic victory
		if (player.getStackSize() == CARD_LIMIT) 
		{
			setWinner(player);
			return;
		}
		
		/*
		 * 		Three Step Process:
		 * 		1. Check other player. If both players have completed the set, check their scores
		 * 		2. If the score is over 25, give victory to the opponent
		 * 		3. If only one player has completed the set, set playerTurn to the opponent
		 */
		
		
		// Checks how many have completed the set
		int numPlayersCompetedSet = 0;
		for (Player p : players) 
		{
			if (p.hasCompletedSet()) 
			{
				numPlayersCompetedSet += 1;
			}
		}
		
		// Other player
		Player otherPlayer;
		if (player.getID() == 1) 
		{
			otherPlayer = getPlayer(0);
		} 
		else 
		{
			otherPlayer = getPlayer(1);
		}
		
		
		// If both players are done with their sets
		if (numPlayersCompetedSet == 2) 
		{
			// Compare the sums of both players
			int[] sums = { getPlayer(0).getSum(), getPlayer(1).getSum() };
			
			// Check if either sum is above SUM_LIMIT (25)
			if (sums[player.getID()] > SUM_LIMIT) 
			{
				setWinner(otherPlayer);
				return;
			} 
			else if (sums[otherPlayer.getID()] > SUM_LIMIT) 
			{
				setWinner(player);
				return;
			}			
			// If it is a draw
			if (sums[0] == sums[1]) 
			{				
				// Check if either player has played a tiebreaker card
				int numTiebreakersPlayed = 0;
				for (Player p : players) 
				{
					if (p.hasPlayedTiebreaker()) { numTiebreakersPlayed += 1; }
				}
				// If one player has played tiebreaker, give them the w
				if (numTiebreakersPlayed == 1) 
				{
					// Give new sets to players and award the one with tiebreaker a set victory.
					Player winner;
					if (player.hasPlayedTiebreaker()) 
					{ 
						// Find who won the game
						winner = player;
					} 
					else { winner = otherPlayer; }
					setWinner(winner);
					return;
				}
				
				// If no tiebreakers have been played, it's a draw
				for (Player p : players) 
				{
					p.newSet(); // Each player gets a new set
				}
				playerTurn = (playerTurnStartLS == 0 ? 1 : 0); // Alternating who starts sets
				playerTurnStartLS = playerTurn;
				notifyOnSetDraw();
			} 
			else 
			{
				// It isn't a draw, see which player has a higher score
				if (sums[player.getID()] > sums[otherPlayer.getID()]) 
				{
					setWinner(player);
					return;
				} 
				else  
				{
					setWinner(otherPlayer);
					return;
				}
			}
		} 
		else 
		{
			// Only one player has finished their set. Check their sum
			if (player.getSum() > SUM_LIMIT) 
			{
				// The player has gone above 25, so they lose
				setWinner(otherPlayer);
				return;
			}
			// Only one player has finished the set, the turn goes to their opponent now
			playerTurn = otherPlayer.getID();
			notifyOnPlayerTurnChanged();
		}
	}

	void onPlayerEndTurn(Player player) 
	{
		if (!gameOngoing) 
		{ 
			return;
		}
		
		/*
		 * 		Two Step Process:
		 * 		1. If the score is above sum limit (25), the opponent wins
		 * 		2. Give turn to the opponent
		 */
		
		// Who is the opponent
		Player otherPlayer = getPlayer(player.getID() == 1 ? 0 : 1);
		
		// Check the player's sum
		if (player.getSum() > SUM_LIMIT) 
		{
			// The player has gone above 25, so they lose.
			setWinner(otherPlayer); // The opponent gets the w
			return;
		}
	
		// Check if it is the opponent's turn
		if (otherPlayer.hasCompletedSet()) 
		{
			playerTurn = player.getID(); // It is still the first player's turn
		} else { 
			playerTurn = otherPlayer.getID(); // It is the opponent's turn
		}
		notifyOnPlayerTurnChanged();
	}
	
	// If a player has played a card from hand
	void onPlayerUsedCard(Player player) 
	{
		notifyOnPlayerCardUsed(player);
	}
}