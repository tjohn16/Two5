package twfive.game;

import android.content.SharedPreferences;

public class Statistics implements TwentyFiveObserver 
{
	
	
	private TwentyFiveGame game;
	private boolean hasAI;
	
	private SharedPreferences savedVars;
	private SharedPreferences.Editor savedVarsEditor;
	
	/* ---------------------------------------------------------------------
	 * Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public Statistics(SharedPreferences savedVars) 
	{	
		this.savedVars = savedVars;
		this.savedVarsEditor = savedVars.edit();
	}
	
	
	/* ---------------------------------------------------------------------
	 * New Game
	 * ---------------------------------------------------------------------
	 */
	
	public void startGame(TwentyFiveGame game) 
	{
		this.game = game;
		hasAI = game.getPlayer(1).isControlledByAI();
		this.game.addObserver(this);
		
		if (hasAI) 
		{
			// Human vs computer, stores the results for the 1vAI category
			savedVarsEditor.putInt("stats_num_1vAI", savedVars.getInt("stats_num_1vAI", 0)+1).commit();
		} 
		else 
		{
			// Player vs Player, stores results for 1v1 category (not as in depth as 1vAI)
			savedVarsEditor.putInt("stats_num_1v1", savedVars.getInt("stats_num_1v1", 0)+1).commit();
		}
	}
	
	/* ---------------------------------------------------------------------
	 * Getter for Statistics
	 * ---------------------------------------------------------------------
	 */
	
	public int getStat(String var) 
	{
		return savedVars.getInt(var, 0);
	}
	

	
	/* ---------------------------------------------------------------------
	 * Number of Forfeits (Closing app, going to main menu)
	 * ---------------------------------------------------------------------
	 */
	
	public void registerForfeit() 
	{
		if (game != null && game.isGameOngoing() && hasAI) 
		{
			// Registers if the player has forfeited (can't differentiate for player vs player
			savedVarsEditor.putInt("stats_num_forfeits", savedVars.getInt("stats_num_forfeits", 0)+1).commit();
		}
	}

	@Override
	public void onSetWon(Player winner) 
	{
		if (hasAI) 
		{
			if (winner.isControlledByAI()) 
			{
				// Register that the computer won the set
				savedVarsEditor.putInt("stats_num_sets_lost", savedVars.getInt("stats_num_sets_lost", 0)+1).commit();
			} 
			else 
			{
				// Register that the player won the set
				savedVarsEditor.putInt("stats_num_sets_won", savedVars.getInt("stats_num_sets_won", 0)+1).commit();
			}
		}
	}

	@Override
	public void onSetDraw() 
	{
		if (hasAI) 
		{
			// Register that the computer and player drew
			savedVarsEditor.putInt("stats_num_sets_draw", savedVars.getInt("stats_num_sets_draw", 0)+1).commit();
		}
	}

	@Override
	public void onMatchWon(Player winner) 
	{
		if (hasAI) 
		{
			if (winner.isControlledByAI()) 
			{
				// Register that the computer won the match
				savedVarsEditor.putInt("stats_num_matches_lost", savedVars.getInt("stats_num_matches_lost", 0)+1).commit();
			} 
			else
			{
				// Register that the player won the match
				savedVarsEditor.putInt("stats_num_matches_won", savedVars.getInt("stats_num_matches_won", 0)+1).commit();
			}
			onSetWon(winner); // The winner of the match also won the previous set
		}
	}

	@Override
	public void onPlayerTurnChanged(int playerTurn) 
	{
		
	}

	@Override
	public void onPlayerCardUsed(Player player) 
	{
		if (hasAI && !player.isControlledByAI()) 
		{
			savedVarsEditor.putInt("stats_num_cards_used", savedVars.getInt("stats_num_cards_used", 0)+1).commit();
		}
	}

	@Override
	public void onPlayerCardDealt(int playerTurn) 
	{
		// TODO Auto-generated method stub
		
	}
}