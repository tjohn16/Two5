package twfive.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player 
{


	/* ---------------------------------------------------------------------
	 * --- Variables
	 * ---------------------------------------------------------------------
	 */ 

	private int setsWon;
	
	private final List<HandCard> hand;
	private final List<Card> stack;
	
	private final String playerName;
	
	private final TwentyFiveGame parentGame;
	private final int id;
	
	private boolean playedTiebreaker;

	private boolean completedDeal;
	private boolean completedRound;
	private boolean completedSet;

	private boolean controlledByAI;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public Player(TwentyFiveGame parentGame, int id, String playerName)
	{
		this.parentGame = parentGame;
		this.id = id;
		this.playerName = playerName;
		
		controlledByAI = false; // AI doesn't control the player
		
		hand = new ArrayList<HandCard>(); // Array used for the player's hand
		for (int i = 0; i < 4; i++) 
		{
			hand.add(parentGame.createHandCard()); // Create four cards for the player's hand
		}
		setsWon = 0; // Zero sets won initially
		stack = new ArrayList<Card>();
		playedTiebreaker = false;

		completedDeal = false;
		completedRound = false;
		completedSet = false;
	}

	
	/* ---------------------------------------------------------------------
	 * Getters
	 * ---------------------------------------------------------------------
	 */ 
	
	public List<HandCard> getHand() 
	{
		//  Return what cards are in the player's hand
		return Collections.unmodifiableList(hand);
	}
	public List<Card> getStack() 
	{
		// Return the stack of cards
		return Collections.unmodifiableList(stack);
	}
	
	public int getID() 
	{
		// Return who is playing
		return id;
	}

	public int getSetsWon() 
	{
		// Return how many sets have been won
		return setsWon;
	}
	
	public int getSum() 
	{
		// Return the sum of the stack of cards
		int sum = 0;
		for (Card c : stack) 
		{ 
			sum += c.getValue();
		}
		return sum;
	}
	
	public int getStackSize() 
	{
		// Return how many cards are in the stack
		return stack.size();
	}
	
	public boolean hasPlayedTiebreaker() 
	{
		// Return if a player has played the tiebreaker (set to false until card is played)
		return playedTiebreaker;
	}
	
	public boolean hasCompletedRound() 
	{
		// Return if a round has been completed
		return completedRound;
	}
	public boolean hasCompletedSet() 
	{
		// Return if a player has completed the set
		return completedSet;
	}
	
	public String toString() 
	{
		// Return the player's name (can be modified)
		return playerName;
	}
	
	public boolean isControlledByAI() 
	{
		// Return if the player is controlled by AI (used for 2nd player)
		return controlledByAI;
	}
	
	/* ---------------------------------------------------------------------
	 * AI controlled
	 * ---------------------------------------------------------------------
	 */ 
	
	void setControlledByAI() 
	{
		// Sets the 2nd player as AI
		controlledByAI = true;
	}

	/* ---------------------------------------------------------------------
	 * Helper methods
	 * ---------------------------------------------------------------------
	 */ 
	
	public boolean canPerformAction() 
	{
		// Whose turn is it?
		// Have they completed the round or the set?
		// Is the number of cards greater than the card limit (9)?
		return (parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this))
				 && !(completedSet || completedRound)
				 && (getStackSize() < TwentyFiveGame.CARD_LIMIT);
	}
	
	/* ---------------------------------------------------------------------
	 * Player control methods
	 * ---------------------------------------------------------------------
	 */ 
	
	public int deal() 
	{ 
		// Returns the value of the stack + the dealt card
		if (completedDeal || !parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) 
		{ 
			return 0; 
		}
		Card c = new DealerCard(); // Dealer cards
		stack.add(c);
		completedDeal = true;
		
		completedRound = false;
		playedTiebreaker = false; // Tiebreaker only functions if it is the final card played

		parentGame.onPlayerCardDealt();
		if (getStackSize() == TwentyFiveGame.CARD_LIMIT) 
		{ 
			stand(); // Won the set by beating the 9 card limit
		} 
		return c.getValue();
	}
	
	public int playCard(int playCard) 
	{ 
		// Returns the value of the stack + the card played
		if (!completedDeal) 
		{ 
			return 0; 
		}
		// If they cannot do anything, end their turn
		if (!canPerformAction()) 
		{ 
			return 0; 
		}
		
		if (playCard >= 0 && playCard <= 3) 
		{   // Card played from hand
			HandCard card = hand.get(playCard);
			if (card.cardPlayed()) 
			{ 
				return 0; // Unable to use that card
			} 

			if (card.getEnumType() == CardNames.TIEBREAKER) 
			{
				playedTiebreaker = true; // Turns tiebreaker value to true
			}

			int val = card.getValue();
			if (card.hasStackImplications()) 
			{ 
				// GUICard modifies the stack, after which the modification is processed
				switch(card.getEnumType()) 
				{
				case ONE_AND_FIVE:
					for (Card c : stack) 
					{ 
						// Converted to card
						if (c.getValue() == 1 || c.getValue() == 5) 
						{
							c.flipValue(); // Flips value of ALL 1's and 5's played
						}
					}
					break;
				
				case TWO_AND_FOUR:
					for (Card c : stack) 
					{ 
						// Converted to card
						if (c.getValue() == 2 || c.getValue() == 4) 
						{
							c.flipValue(); // Flips value of ALL 2's and 4's played
						}
					}
					break;
					
				case THREE_AND_SIX:
					for (Card c : stack) 
					{ 
						// Converted to card
						if (c.getValue() == 3 || c.getValue() == 6) 
						{
							c.flipValue(); // Flips value of ALL 3's and 6's played
						}
					}
					break;
					
				case DOUBLE:
					stack.get(stack.size()-1).doubleValue(); // Double the value of the last card played
					break;
					
				default:
					break;
				}
			}
			stack.add(card);
			card.setUsed();
			completedRound = true;
			parentGame.onPlayerUsedCard(this);
			if (getStackSize() == TwentyFiveGame.CARD_LIMIT) 
			{
				stand(); // Already won, so unnecessary
			}
			return val;
		}
		return 0;
	}
	
	public void stand() 
	{
		
		if (!parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) 
		{ 
			return; 
		}
		if (!completedDeal) 
		{ 
			return; // Unable to do anything until the deal is completed
		} 
		
		completedDeal = false;
		completedSet = true;
		completedRound = true;
		parentGame.onPlayerStand(this);
	}
	
	public void endTurn() 
	{
		if (!parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) 
		{ 
			return; 
		}
		if (!completedDeal) 
		{
			return; // Unable to do anything until the deal is completed
		} 
		completedDeal = false;
		completedRound = true;
		parentGame.onPlayerEndTurn(this);
	}	
	
	
	/* ---------------------------------------------------------------------
	 * --- Generate new set (package-only)
	 * ---------------------------------------------------------------------
	 */ 
	
	void newSet() {
		stack.clear(); // Clears the stack
		
		// Reset all state variables
		playedTiebreaker = false;
		completedDeal = false;
		completedRound = false;
		completedSet = false;
	}
	
	/* ---------------------------------------------------------------------
	 * --- Award set victory
	 * ---------------------------------------------------------------------
	 */
	
	void setWinner() 
	{
		setsWon += 1; // Add to the set won counter
	}
}
