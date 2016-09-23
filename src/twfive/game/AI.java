package twfive.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;


public class AI implements TwentyFiveObserver 
{

	/* ---------------------------------------------------------------------
	 * The Artificial Intelligence for the computer
	 * 
	 * Controls the computer. Listens to updates and reacts.
	 * Uses "thinking time" before its decision.
	 * ---------------------------------------------------------------------
	 */ 
	
	
	/* ---------------------------------------------------------------------
	 * Global Variables
	 * ---------------------------------------------------------------------
	 */ 
	
	public static final int MAX_THINKING_TIME = 1500; // In ms
	public static final int MIN_THINKING_TIME = 1000; // In ms
	
	
	public static final int LOW_END_BUFFER = TwentyFiveGame.SUM_LIMIT - 2; // Buffer zone
	public static final int LOW_END_DEAL_BUFFER = TwentyFiveGame.SUM_LIMIT - 6; // Buffer zone
	
	/* ---------------------------------------------------------------------
	 * Variables
	 * ---------------------------------------------------------------------
	 */ 
	
	private final TwentyFiveGame parentGame;
	private final Player player;
	private final Player opponent;
	
	private final List<HandCard> handAI; // Duplicate the player's information
	private final List<Card> stackAI; // Duplicate the player's information
	
	private boolean hasPlayedCard; // Card has been used this round
	
	
	private final Handler handler;
	private final Timer timer;
	private final Runnable timerRunnable;
	
	
	/* ---------------------------------------------------------------------
	 * Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	AI(TwentyFiveGame parentGame, Player player) 
	{
		this.parentGame = parentGame;
		this.parentGame.addObserver(this);
		this.player = player;
		this.player.setControlledByAI();

		if (player.getID() == 1) 
		{
			this.opponent = this.parentGame.getPlayer(0);
		} 
		else 
		{
			this.opponent = this.parentGame.getPlayer(1);
		}
		
		handAI = new ArrayList<HandCard>();
		stackAI = new ArrayList<Card>();
		
		fetchPlayerData();
		
		
		this.handler = new Handler();
		this.timer = new Timer();
		this.timerRunnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				doBestMove(); // Does the best possible move within the given timeframe
			}
		};
	}
	
	
	/* ---------------------------------------------------------------------
	 * Getter methods
	 * ---------------------------------------------------------------------
	 */ 
	
	public TwentyFiveGame getParentGame() 
	{
		return parentGame;
	}
	
	public Player getPlayer() 
	{
		return player;
	}	
	
	
	
	/* ---------------------------------------------------------------------
	 * Gets player data
	 * ---------------------------------------------------------------------
	 */ 
	
	private void fetchPlayerData() 
	{		
		// Keep stack clean
		stackAI.clear();
		for (Card c : player.getStack()) 
		{ 
			// Add the AI player's stack back in.
			if (c instanceof HandCard) 
			{
				stackAI.add(new HandCard(c.getEnumType(), c.getValue())); // Playing card from hand
			} 
			else 
			{
				stackAI.add(new DealerCard(c.getValue())); // Being dealt a card
			}
		}
		// Add the AI player's stack back in
		handAI.clear();
		for (Card c : player.getHand())
		{ 
			handAI.add(new HandCard(c.getEnumType(), c.getValue())); // Playing card from hand
		}
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * Counting Cards
	 * ---------------------------------------------------------------------
	 */ 
	
	private int sumCardPlayed(int handCardIndex, boolean flip) 
	{
		if (handCardIndex >= 0 && handCardIndex <= 3) 
		{ 
			// Has a card been played?
			HandCard card = handAI.get(handCardIndex);
			if (card.cardPlayed()) 
			{ 
				return 0; // Unable to use that card
			} 

			if (card.hasStackImplications()) 
			{ 
				// GUICard modifies the stack, modifications are processed after
				switch(card.getEnumType()) 
				{
				case ONE_AND_FIVE:
					for (Card c : stackAI) 
					{ 
						// Converting from stack to card
						if (c.getValue() == 1 || c.getValue() == 5) 
						{
							c.flipValue(); // Flipping value of all cards in stack
						}
					}
					break;
				case TWO_AND_FOUR:
					for (Card c : stackAI) 
					{ 
						// Converting from stack to card
						if (c.getValue() == 2 || c.getValue() == 4) 
						{
							c.flipValue(); // // Flipping value of all cards in stack
						}
					}
					break;
					
				case THREE_AND_SIX:
					for (Card c : stackAI) 
					{ 
						// Converting from stack to card
						if (c.getValue() == 3 || c.getValue() == 6) 
						{
							c.flipValue(); // // Flipping value of all cards in stack
						}
					}
					break;
					
				case DOUBLE:
					stackAI.get(stackAI.size()-1).doubleValue(); // Double value of last card played
					break;
					
				default:
					break;
				}
			}
			
			if (flip && card.isFlippableCard())
			{
				card.flipValue(); // Flips value of card
			}
			
			stackAI.add(card); // Add card to the stack
		}
		
		int sum = 0; 
		for (Card c : stackAI) 
		{
			sum += c.getValue(); // New value of the stack
		}
		fetchPlayerData(); // Reset what changed
		return sum; // Sum value of cards in stack
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * Helper for doBestMove
	 * ---------------------------------------------------------------------
	 */ 
	
	private void playCard(int cardIndex, boolean flipped) 
	{		
		if (player.getHand().get(cardIndex).cardPlayed()) 
		{
			// System.out.println("Attempting to reuse a card!");
			beginAction(); 
			return;
		}
		
		if (flipped) 
		{
			player.getHand().get(cardIndex).flipCard(); // Card needs to be flipped before it can be played
		}
		player.playCard(cardIndex);
		hasPlayedCard = true;
		beginAction(); // "Stand" delayed until timer runs out
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * doBestMove
	 * ---------------------------------------------------------------------
	 */ 
	

	
	/** 
	 * 7 Step Process the AI runs through during its turn:
	 * 
	 * 1: Has it used a card this turn?
	 * 2: Are there any ways to win outright? Stand/end turn depending on if so.
	 * 3: Is there a card that can be played from hand? If there isn't, Stand/end turn depending on if so.
	 * 4: Will it lose if it does not play a card?
	 * 5: Is it possible to win outright by playing a card?
	 * 6: If it isn't possible to win, should it play a card?
	 * 7: End turn.
	 * 
	 */
	
	private void doBestMove() 
	{
		int sum = player.getSum();
		int opponentSum = opponent.getSum();
		
		// 1st step
		// If a card has been used, either Stand or End turn
		if (hasPlayedCard) 
		{
			// A card has been used. Is it better to Stand or End turn?
			if ((opponent.getSum() > player.getSum()) && (opponent.getSum() <= TwentyFiveGame.SUM_LIMIT)) 
			{
				player.endTurn(); // Standing would mean the computer loses. It won't do that.
			} 
			else 
			{
				if (sum >= opponentSum && sum > LOW_END_DEAL_BUFFER) 
				{
					player.stand(); // Score is better than opponent and greater than the buffer value from 25.
					return;
				}
				player.endTurn(); // End turn.
			}
			return;
		}
		
		
		// 2nd Step
		
		
		// Plays the most logical option
		// Before playing a card, run checks to see if game can be won.
		if (sum == TwentyFiveGame.SUM_LIMIT) 
		{
			player.stand(); // Reached the perfect score. Stand.
			return;
		}
		if (opponent.hasCompletedSet() && opponentSum < sum && sum < TwentyFiveGame.SUM_LIMIT) 
		{
			player.stand(); // Opponent has finished their set and AI score is better. Stand
			return;
		}
		if (opponent.hasPlayedTiebreaker() && opponentSum == TwentyFiveGame.SUM_LIMIT) 
		{
			player.stand(); // No possible way to win as opponent has 25 + tiebreaker.
			return;
		}
		
		
		// 3rd Step
		// Check value of cards in hand
		int closest = -999;
		int closestIndex = -1;
		boolean closestIsFlip = false;
		boolean canUseACard = false;
		
		// Scan hand to see available cards.
		for (int i = 0; i < 4; i++) 
		{
			HandCard card = player.getHand().get(i);
			
			if (card.cardPlayed()) 
			{ 
				continue; // Card has been played so nothing can be played
			} 
						
			int newSum = sumCardPlayed(i, false);
			
			if ((newSum > sum) && (closest <= newSum) && (newSum <= TwentyFiveGame.SUM_LIMIT)) 
			{
				closest = newSum;
				closestIsFlip = false; // Closest card to play is NOT flippable
				canUseACard = true; // Possible to play a card.
				closestIndex = i; // Override the index currently in place.
			}
			
			if (card.isFlippableCard()) 
			{
				int newSum2 = sumCardPlayed(i, true);
				if ((newSum2 < sum) && sum <= TwentyFiveGame.SUM_LIMIT) 
				{ 
					continue;  // Doesn't make sense to flip to negative.
				}
				
				if ((closest < newSum2) && (newSum2 <= TwentyFiveGame.SUM_LIMIT)) 
				{
					closest = newSum2;
					closestIsFlip = true; // Closest card to play IS flippable
					canUseACard = true; // Possible to play a card.
					closestIndex = i; // Override the index currently in place.
				}
			}
		}
		
		// The scan is finished. Is there a card that can get <= 25?
		if (!canUseACard) 
		{
			if (opponent.hasCompletedSet() && opponentSum < sum && sum < TwentyFiveGame.SUM_LIMIT) 
			{
				player.stand(); // Opponent has finished their set and AI score is better. No need for card
				return;
			}
			if (sum >= LOW_END_BUFFER && sum >= opponentSum)
			{
				player.stand();// Score is better than opponent and greater than the buffer value from 25. No need for card
				return;
			}
			player.endTurn();
			return;
		}
		
		
		// 4th step
		
		
		if (sum > TwentyFiveGame.SUM_LIMIT) 
		{ 
			// Necessary to play a card in order to not lose
			if ((opponentSum > closest) && (opponentSum <= TwentyFiveGame.SUM_LIMIT)) 
			{
				player.stand(); // Already losing, stand
				return;
			}
			playCard(closestIndex, closestIsFlip); // Possible to bring computer below 25, play it.
			return;
		}
		
		
		// 5th Step
		
		// Outright victory
		if (opponent.hasCompletedSet()
				&& opponentSum >= LOW_END_DEAL_BUFFER
				&& opponentSum < closest
				&& closest <= TwentyFiveGame.SUM_LIMIT) 
		{
			
			playCard(closestIndex, closestIsFlip); // Guaranteed victory.
			return;
		}
		
		// Gamble, but odds are in computer's favor.
		if (opponentSum < closest 							// Opponent sum is worse than computer sum + card.
				&& closest <= TwentyFiveGame.SUM_LIMIT	// Computer sum is less than or equal to 25 + card
				&& sum >= LOW_END_DEAL_BUFFER			// Computer sum is less than or equal to the buffer values.
				&& closest >= LOW_END_BUFFER) 			// Computer sum is less than or equal to the buffer + card.
		{
			playCard(closestIndex, closestIsFlip); // Gamble it. 
			return;
		}
		
		// Is it possible to tie the set?
		boolean canDraw = false;
		if (opponent.hasCompletedSet()
				&& opponentSum == closest // Possible to draw?
				&& !opponent.hasPlayedTiebreaker() // A draw.
				&& closest <= TwentyFiveGame.SUM_LIMIT // Not worth it, essentially like losing.
				&& opponentSum >= LOW_END_BUFFER) // Does it make sense to draw?
		{ 
			canDraw = true; // Guaranteed draw.
		}
		
		
		// 6th Step
		// Does it make sense to use a card? Does it put the computer in a position to win?
		
		// High sum greater than the buffer
		if (sum >= LOW_END_BUFFER) 
		{
			// Not ideal if it isn't possible to win this turn
			if (canDraw) 
			{
				playCard(closestIndex, closestIsFlip); // Attempt to draw
			} 
			else
			{
				// Not possible to attempt a draw
				if (opponentSum > sum) 
				{
					player.endTurn(); // Gamble, hope the card dealt works
				} 
				else 
				{
					player.stand(); // Don't gamble
				}
			}
			return;
		}
		
		// Mid sum larger than deal buffer but not total buffer
		if (sum >= LOW_END_DEAL_BUFFER && sum < LOW_END_BUFFER) 
		{
			if (canDraw) 
			{
				playCard(closestIndex, closestIsFlip); // Attempt to draw
				return;
			}
		}

		
		// 7th Step
		// End turn, no need for anything else.
		player.endTurn();
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * Event helper
	 * ---------------------------------------------------------------------
	 */
	
	private void beginTurn() 
	{
		fetchPlayerData(); // Refreshing data each turn start to avoid AI problems with Stack/ Hand
	}

	private void beginAction() 
	{
		if (player.getStackSize() == TwentyFiveGame.CARD_LIMIT) 
		{
			return; // Automatic victory from filling 9 card slots, automatically stand
		}
		
		TimerTask timerTask = new TimerTask() 
		{
			@Override
			public void run() 
			{
				handler.post(timerRunnable); // Begins timer for AI thinking time
			}
		};
		this.timer.schedule(timerTask, (long) (MIN_THINKING_TIME + (Math.random() * ((MAX_THINKING_TIME - MIN_THINKING_TIME) + 1)))); 
	}
	
	
	/* ---------------------------------------------------------------------
	 * Event Listeners
	 * ---------------------------------------------------------------------
	 */ 
	
	@Override
	public void onSetWon(Player winner)
	{
		hasPlayedCard = false;
		if (player.getID() == parentGame.getPlayerTurn()) 
		{ 
			// Whose turn is it?
			beginTurn();
			beginAction();
		}
	}
	
	@Override
	public void onSetDraw() 
	{
		hasPlayedCard = false;
		if (player.getID() == parentGame.getPlayerTurn()) 
		{
			// Whose turn is it?
			beginTurn();
			beginAction();
		}
	}
	@Override
	public void onMatchWon(Player winner) 
	{
	}
	@Override
	public void onPlayerTurnChanged(int playerTurn) 
	{
		hasPlayedCard = false;		
		if (player.getID() == playerTurn) 
		{ 
			// Whose turn is it?
			beginTurn();
		}
	}
	@Override
	public void onPlayerCardUsed(Player player) 
	{
		
	}


	@Override
	public void onPlayerCardDealt(int playerTurn) 
	{
		hasPlayedCard = false;		
		if (player.getID() == playerTurn) 
		{ 
			// Whose turn is it?
			beginTurn();
			beginAction();
		}
	}

	
}