package twfive.android;

import twfive.game.Card;
import twfive.game.HandCard;
import twfive.game.TwentyFiveGame;
import twfive.game.TwentyFiveObserver;
import twfive.game.Player;
import twfive.game.Statistics;
import twfive.gui.BitmapHandler;
import twfive.gui.GUIButton;
import twfive.gui.GUICard;
import twfive.gui.GUIImage;
import twfive.gui.GUIText;
import twfive.gui.Toggle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



@SuppressLint("ViewConstructor")
public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback, TwentyFiveObserver 
{

	public static final int MENU_STATE_MAIN_MENU = 0;
	public static final int MENU_STATE_INSTRUCTIONS = 2;
	public static final int MENU_STATE_STATISTICS = 4;
	
	public static final int GAME_PLAYING_STATE_MENU = 0;
	public static final int GAME_PLAYING_STATE_PLAYING = 1;
	public static final int GAME_PLAYING_STATE_WON = 2;
	
	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private GameSound sound;
	private Statistics stats;

	// GUI is put in multiple layers.
	
	// 1st GUI layer
	private GUIImage background;
	
	// 2nd GUI layer
	private GUIImage mainMenu;
	private GUIImage mainMenuInfo;
	
	private GUIButton[] gameButtons;
	private GUICard[][] gamefieldCards;
	private GUICard[][] handCards;
	private GUICard draggedCard;
	private GUIText[] pNames;
	private GUIText[] pScore;
	private GUIImage[][] pSetIcon;
	
	// 3rd GUI layer
	private GUIText[] statsText;
	private GUIButton[] menuButtons;
	private GUIImage notifyBox;
	private GUIText notifyText;
	private GUIButton notifyButton;
	private Toggle[] soundToggles;
	private BitmapHandler bitmapHandler;
	private MainThread thread;
	private boolean loaded;
	private TwentyFiveGame game;
	private int gamePlayingState;
	private int menuState;
	
	public final Typeface font = Typeface.createFromAsset(getResources().getAssets(), "fonts/zeroes.TTF");
	

	
	// Dimensions of the canvas
	private double scale;
	private double height;
	
	// FPS counter
	private GUIText FPSCounter;
	private long lastFPSUpdate;
	private int[] FPSArray;
	private int FPSTickSum;
	private int FPSTickIndex;


    
    
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	

	public MainGamePanel(Context context, GameSound sound, Statistics stats) 
	{
		super(context);
		getHolder().addCallback(this);
		this.sound = sound;
		this.stats = stats;
		
		this.scale = (double)getResources().getDisplayMetrics().widthPixels/(double)1280;
		this.height = (double)(scale*(double)720);
		bitmapHandler = new BitmapHandler(this);
		
		
		// The two different states the game starts in. Both the game state and menustate are on the menu.
		gamePlayingState = GAME_PLAYING_STATE_MENU; 
		menuState = MENU_STATE_MAIN_MENU;
		
		// All the arrays used. Buttons used in the game itself, the cards, the statistics, as well as buttons used to control sound.
		gameButtons = new GUIButton[3];
		gamefieldCards = new GUICard[2][9];
		handCards = new GUICard[2][4];
		pNames = new GUIText[2];
		pScore = new GUIText[2];
		pSetIcon = new GUIImage[2][9];
		statsText = new GUIText[10];
		menuButtons = new GUIButton[5];
		soundToggles = new Toggle[2];
		
		
		
		lastFPSUpdate = System.nanoTime();
		FPSTickSum = 0;
	    FPSTickIndex = 0;
		FPSArray = new int[100];
		for (int i = 0; i < 100; i++) 
		{
			FPSArray[i] = 0;
		}
		
		
		this.thread = new MainThread(getHolder(), this);
		loaded = false;
		
		setFocusable(true);
	}
	
	// Method to get scale of game buttons/icons. 
	public double getScale() 
	{
		return scale;
	}
	
	
	public double getGameHeight() 
	{
		return height;
	}
	
	// Returns what state the game is in.
	public int getGamePlayingState() 
	{
		return gamePlayingState;
	}
	
	// Returns the Bitmap Handler used for graphics.
	public BitmapHandler getBitmapHandler() 
	{
		return bitmapHandler;
	}
	

	/* ---------------------------------------------------------------------
	 * --- Game related methods
	 * ---------------------------------------------------------------------
	 */ 	
	
	
	private void startGame(boolean AI) 
	{
		game = new TwentyFiveGame(AI); // Starts a new game with AI.
		game.addObserver(this);
		stats.startGame(game);
		gamePlayingState = GAME_PLAYING_STATE_PLAYING; // Sets the gamestate to play.
		
		// Player Names		
		pNames[0].setText(game.getPlayer(0).toString());
		pNames[1].setText(game.getPlayer(1).toString());
		
		// Hands for the players.
		for (int i = 0; i < handCards[0].length; i++) 
		{
			GUICard gcard =handCards[0][i];
			gcard.setCard(game.getPlayer(0).getHand().get(gcard.getCardNumber()));
		}
		for (int i = 0; i < handCards[1].length; i++) 
		{
			GUICard gcard = handCards[1][i];
			gcard.setCard(game.getPlayer(1).getHand().get(gcard.getCardNumber()));
		}
		
		game.getPlayer(game.getPlayerTurn()).deal(); 
		getGameInformation(); 
	}
	
	public TwentyFiveGame getGame() 
	{
		return game;
	}
	
	private void closeGame() 
	{
		for (int i = 0; i < gamefieldCards[0].length; i++) 
		{
			gamefieldCards[0][i].setCard(null);
		}
		for (int i = 0; i < gamefieldCards[1].length; i++) 
		{
			gamefieldCards[1][i].setCard(null);
		}
		
		for (int i = 0; i < handCards[0].length; i++) 
		{
			GUICard gcard = handCards[0][i];
			gcard.resetPos();
			gcard.setCard(null);
		}
		for (int i = 0; i < handCards[1].length; i++) 
		{
			GUICard gcard = handCards[1][i];
			gcard.resetPos();
			gcard.setCard(null);
		}
		
		gamePlayingState = GAME_PLAYING_STATE_MENU;
		menuState = MENU_STATE_MAIN_MENU;
		game = null;
	}
	
	public void loadGUI() 
	{
		if (loaded) 
		{ 
			return; 
		}

		String[] menuButtonTexts = getResources().getStringArray(R.array.menu_button_texts);
		String[] gamefieldButtonTexts = getResources().getStringArray(R.array.gamefield_button_texts);
		
		
		// 1st layer of the GUI
		background = new GUIImage(getWidth()/2, getHeight()/2);
		background.setBitmap(bitmapHandler.getBackground());
		
		// 2nd layer of the GUI
		mainMenu = new GUIImage(getWidth()/2, getHeight()/2);
		mainMenu.setBitmap(bitmapHandler.getMenu());
		
		mainMenuInfo = new GUIImage(getWidth()/2, getHeight()/2);
		
		
		int cardID = 0;
		// For loop to create the 3x3 playing field for player 1.
		for (int row = 1; row <= 3; row++) 
		{
			for (int column = 1; column <= 3; column++) 
			{ 
				gamefieldCards[0][cardID] = new GUICard(this, false, 0, cardID,
					getWidth()/2 - (int) ((170 + (3-column)*96)*scale),
					getHeight()/2 - (int) ((184 - (row-1)*134)*scale)
				);
				cardID += 1;
			}
		}
		
		cardID = 0;
		// For loop to create the 3x3 playing field for player 2/ the computer.
		for (int row = 1; row <= 3; row++)
		{
			for (int column = 1; column <= 3; column++) 
			{
				gamefieldCards[1][cardID] = new GUICard(this, false, 1, cardID,
					getWidth()/2 + (int) ((172 + (column-1)*96)*scale),
					getHeight()/2 - (int) ((184 - (row-1)*134)*scale)
				);
				cardID += 1;
			}
		}
		
		// For loop to create the 1x4 field used to store the player's hand.
		for (int column = 1; column <= 4; column++) 
		{ 
			handCards[0][column-1] = new GUICard(this, true, 0, column-1,
				getWidth()/2  - (int) ((121 + (column-1)*96)*scale),
				(int) ((getHeight()/2)+230*scale)
					);
		}
		
		// For loop to create the 1x4 field used to store player 2's/ the computer's hand.
		for (int column = 1; column <= 4; column++) 
		{ 
			handCards[1][column-1] = new GUICard(this, true, 1, column-1,
				getWidth()/2  + (int) ((124 + (4-column)*96)*scale),
				(int) ((getHeight()/2)+230*scale)
			);
		}

		
		for (int i = 1; i <= 3; i++) 
		{ 
			pSetIcon[0][i] = new GUIImage(getWidth()/2 - (int) (525*scale), getHeight()/2 - (int) (((3-i)*64+25)*scale) );
			pSetIcon[0][i].setBitmap(bitmapHandler.getSetlight());
			
			pSetIcon[1][i] = new GUIImage(getWidth()/2 + (int) (532*scale), getHeight()/2 - (int) (((3-i)*64+25)*scale));
			pSetIcon[1][i].setBitmap(bitmapHandler.getSetlight());
		}
		
		// Creates the nameboxes for the players. 
		pNames[0] = new GUIText(this, getWidth()/2 - (int)(268*scale), getHeight()/2 - (int)(260*scale), (int)(35*scale), Paint.Align.CENTER);
		pNames[1] = new GUIText(this, getWidth()/2 + (int)(268*scale),	getHeight()/2 - (int)(260*scale), (int)(35*scale), Paint.Align.CENTER);
		
		// Creating scoreboxes for the players.
		pScore[0] = new GUIText(this, getWidth()/2 - (int) (505*scale), getHeight()/2 - (int) (195*scale), (int)(55*this.scale), Paint.Align.CENTER);
		pScore[1] = new GUIText(this, getWidth()/2 + (int) (500*scale), getHeight()/2 - (int) (195*scale), (int)(55*this.scale), Paint.Align.CENTER);

		// Creating buttons used for Stand and End Turn.
		gameButtons[0] = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[0], getWidth()/2, (int) ((getHeight()/2)+25*scale) );
		gameButtons[1] = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[1], getWidth()/2, (int) ((getHeight()/2)+100*scale)  );
		
		
		// 3rd layer of the GUI.
		
		// Creates the buttons on the main menu and its layers. 1 Player game, 2 Player game, Instructions, Statistics, and Back.
		menuButtons[0] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[0], getWidth()/2, (int) ((getHeight()/2)-110*scale));
		menuButtons[1] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[1], getWidth()/2, (int) ((getHeight()/2)-60*scale));
		menuButtons[2] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[2], getWidth()/2, (int) ((getHeight()/2)+40*scale));
		menuButtons[3] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[3], getWidth()/2, (int) ((getHeight()/2)+90*scale));
		menuButtons[4] = new GUIButton(this, bitmapHandler.getButton(), menuButtonTexts[4], getWidth()/2, (int) ((getHeight()/2)+235*scale));
		
		
		// All of the different stats kept in the Statistics tab.
		// Stats against the computer.
		statsText[0] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2-(int)(85*scale), (int)(25*scale), Paint.Align.RIGHT);
		// Stats against another human player.
		statsText[1] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2-(int)(55*scale), (int)(25*scale), Paint.Align.RIGHT);
		// Ratio of games won/played.	
		statsText[2] = new GUIText(this, getWidth()/2, getHeight()/2+(int)(40*scale), (int)(45*scale), Paint.Align.CENTER);
		// Ratio of matches won/played.
		statsText[3] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(58*scale), (int)(20*scale), Paint.Align.RIGHT);
		// Ratio of matches lost/played.
		statsText[4] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(78*scale), (int)(20*scale), Paint.Align.RIGHT);
		// Number of forfeits.
		statsText[5] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(98*scale),	(int)(20*scale), Paint.Align.RIGHT);
		// Number of cards used from hand.		
		statsText[6] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(137*scale), (int)(20*scale), Paint.Align.RIGHT);
		// Number of sets won.
		statsText[7] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(58*scale), (int)(20*scale), Paint.Align.RIGHT);
		// Number of sets lost.
		statsText[8] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(78*scale), (int)(20*scale), Paint.Align.RIGHT);
		// Number of sets drawn.
		statsText[9] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(98*scale), (int)(20*scale), Paint.Align.RIGHT);
		
		
		notifyBox = new GUIImage(getWidth()/2, getHeight()/2);
		notifyBox.setBitmap(bitmapHandler.getNotificationbox());
		notifyText = new GUIText(this, getWidth()/2, getHeight()/2 - (int)(75*scale), (int)(35*scale), Paint.Align.CENTER);
		notifyButton = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[2], getWidth()/2, (int)((getHeight()/2)-25*scale));

		// Button to toggle if music is enabled.
		soundToggles[0] = new Toggle(this, bitmapHandler.getMusicicon(), bitmapHandler.getMusicicondisabled(), sound.isMusicEnabled(),
				getWidth()/2 + bitmapHandler.getBackground().getWidth()/2 - (int)((5+25)*scale),
				getHeight()/2 - bitmapHandler.getBackground().getHeight()/2 + (int)(26*scale)
		);
		
		// Button to toggle if sound is enabled.
		soundToggles[1] = new Toggle(this, bitmapHandler.getSoundicon(), bitmapHandler.getSoundicondisabled(), sound.isSoundEnabled(),
				getWidth()/2 + bitmapHandler.getBackground().getWidth()/2 - (int)((5+25*3+10)*scale),
				getHeight()/2 - bitmapHandler.getBackground().getHeight()/2 + (int)(26*scale)
		);
		
		// FPS tracker.
		this.FPSCounter = new GUIText(this, 
				(int)(25*scale),
				getHeight()/2 - (int)((bitmapHandler.getBackground().getHeight()/2) - 30*scale),
				(int)(22*scale), Paint.Align.LEFT
		);
	
		
		loaded = true;
	}
		



	/* ---------------------------------------------------------------------
	 * Surface
	 * ---------------------------------------------------------------------
	 */
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		loadGUI();
		
		try 
		{
			thread.setRunning(true);
			thread.start();
		}
		catch (Exception e) 
		{
			
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		Log.d(TAG, "Destroying Surface");

		boolean retry = true;
		while (retry) 
		{
			try 
			{
				thread.setRunning(false);
				thread.join();
				
				retry = false;
			} 
			catch (InterruptedException e) 
			{
				// Attempts to shut down the thread again.
			} 
			catch (Exception e) 
			{
				// Fatal error.
			}
		}
		//closeGame();
		Log.d(TAG, "Thread was shut down cleanly");
	}
	
	
	/* ---------------------------------------------------------------------
	 * Touch events
	 * ---------------------------------------------------------------------
	 */ 
	
	
	
	public synchronized boolean onTouchEvent(MotionEvent event) 
	{
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		
		switch(action) 
		{

		case MotionEvent.ACTION_DOWN:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) 
			{ 
				// gameState set to Menu.
				if (menuState > MENU_STATE_MAIN_MENU) 
				{
					menuButtons[menuButtons.length-1].handleActionDown(x,y);
				} 
				else 
				{ 
					for (int index = 0; index < (menuButtons.length-1); index++) 
					{ 
						menuButtons[index].handleActionDown(x,y);
					}
				}
				
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) 
			{ 
				// gameState set to playing
				for (int playerID = 0; playerID <= 1; playerID++) 
				{
					for (int cardID = 0; cardID <= 3; cardID++) 
					{
						handCards[playerID][cardID].handleActionDown(x, y);
					}
				}
				if (draggedCard == null) 
				{
					gameButtons[0].handleActionDown(x,y); 
					gameButtons[1].handleActionDown(x,y);
				}
				
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_WON) 
			{ 
				notifyButton.handleActionDown(x,y);
			}
			
			soundToggles[0].handleActionDown(x,y); 
			soundToggles[1].handleActionDown(x,y); 
			
			break;
		
		case MotionEvent.ACTION_MOVE:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU)
			{ 
				if (menuState > MENU_STATE_MAIN_MENU) 
				{
					menuButtons[menuButtons.length-1].handleActionDown(x,y); 
				} 
				else
				{ 
					for (int index = 0; index < (menuButtons.length-1); index++) 
					{ 
						menuButtons[index].handleActionDown(x,y);
					}
				}
			}
			else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) 
			{ 
				
				if (draggedCard == null)
				{ 
					for (int playerID = 0; playerID <= 1; playerID++) 
					{
						for (int cardID = 0; cardID <= 3; cardID++) 
						{
							handCards[playerID][cardID].handleActionDown(x, y);
						}
					}
					gameButtons[0].handleActionDown(x,y); // Button "Stand" has been pressed.
					gameButtons[1].handleActionDown(x,y); // Button "End Turn" has been pressed.
				}
				dragloop:
				for (int playerID = 0; playerID <= 1; playerID++) 
				{
					for (int cardID = 0; cardID <= 3; cardID++) 
					{
						boolean dragging = handCards[playerID][cardID].handleActionMove(x, y);
						if (dragging) 
						{
							draggedCard = handCards[playerID][cardID];
							break dragloop;
						}
					}
				}
				
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_WON) 
			{
				notifyButton.handleActionDown(x,y);
			}
			
			soundToggles[0].handleActionDown(x,y); // Music toggle has been pressed.
			soundToggles[1].handleActionDown(x,y); // Sound toggle has been pressed.
			
			break;
			
		case MotionEvent.ACTION_UP:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) 
			{ 
				// gameState set to Menu.
				for (int index = 0; index < menuButtons.length; index++) 
				{ 
					menuButtons[index].setTouched(false, true);
				}
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) 
			{   
				// gameState set to playing.
				draggedCard = null;
				gameButtons[0].setTouched(false, true);	
				gameButtons[1].setTouched(false, true);

				for (int playerID = 0; playerID <= 1; playerID++) 
				{
					for (int cardID = 0; cardID <= 3; cardID++) 
					{
						handCards[playerID][cardID].handleActionUp(x, y);
					}
				}
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_WON) 
			{ 
				// Somebody has won.
				notifyButton.setTouched(false, true);
			}
			
			
			soundToggles[0].setTouched(false, true);
			soundToggles[1].setTouched(false, true);
			break;
			

		case MotionEvent.ACTION_CANCEL:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) 
			{ 
				// Game is currently in the menu.
				for (int index = 0; index < menuButtons.length; index++) 
				{ 
					menuButtons[index].setTouched(false, false);
				}
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) 
			{ 
				// Game is currently being played.
				draggedCard = null;
				gameButtons[0].setTouched(false, false);	
				gameButtons[1].setTouched(false, false);
				for (int playerID = 0; playerID <= 1; playerID++) 
				{
					for (int cardID = 0; cardID <= 3; cardID++) 
					{
						handCards[playerID][cardID].handleActionUp(x, y);
					}
				}
			} 
			else if (gamePlayingState == GAME_PLAYING_STATE_WON) 
			{ 
				// Somebody has won.
				notifyButton.setTouched(false, false);
			}
			
			soundToggles[0].setTouched(false, false);
			soundToggles[1].setTouched(false, false);
			break;
		}
		
		return true;
	}
	
	// Handles when a button has been clicked.
	public synchronized void handleClicks(GUIButton b) 
	{
		sound.playSound("button");

		if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) 
		{
			if (b == gameButtons[0]) 
			{ 
				// "Stand" has been clicked. 
				if (game.getPlayer(game.getPlayerTurn()).isControlledByAI()) 
				{
					return;
				}
				game.getPlayer(game.getPlayerTurn()).stand();	
			} 
			else if (b == gameButtons[1]) 
			{ 
				// "End Turn" has been clicked.
				if (game.getPlayer(game.getPlayerTurn()).isControlledByAI()) 
				{
					return;
				}
				game.getPlayer(game.getPlayerTurn()).endTurn();
				
				
			}

		}

		if  ((gamePlayingState == GAME_PLAYING_STATE_WON) && b == notifyButton) 
		{ 
			// Somebody has won, take us back to menu
			closeGame();
			return;
		}		
		
		
		if (b == menuButtons[0]) 
		{ 
			// "1 Player Game", starts a game versus the AI.
			startGame(true);

		} 
		else if (b == menuButtons[1]) 
		{ 
			// "2 Player Game", starts a game versus another human player.
			startGame(false); // AI isn't started because two humans are playing.
			
		} 
		else if (b == menuButtons[2]) 
		{ 
			// "Instructions", takes us to the instructions tab.
			mainMenuInfo.setBitmap(bitmapHandler.getInstructions());
			menuState = MENU_STATE_INSTRUCTIONS; // Changes menuState to Instructions
			
		} 
		else if (b == menuButtons[3]) 
		{ 
			mainMenuInfo.setBitmap(bitmapHandler.getStatistics());
			
			String[] stats = getResources().getStringArray(R.array.statistics);
			
			// Ratio of games won is separate from the rest of statistics text below.
			
			statsText[0].setText(String.valueOf(this.stats.getStat(stats[0]))); // Stats against the computer.
			statsText[1].setText(String.valueOf(this.stats.getStat(stats[1]))); // Stats against another human player
			statsText[3].setText(String.valueOf(this.stats.getStat(stats[3]))); // Ratio of matches won/played.
			statsText[4].setText(String.valueOf(this.stats.getStat(stats[4]))); // Ratio of matches lost/played.
			statsText[5].setText(String.valueOf(this.stats.getStat(stats[5]))); // Number of forfeits.
			statsText[6].setText(String.valueOf(this.stats.getStat(stats[6]))); // Number of cards played from hand
			statsText[7].setText(String.valueOf(this.stats.getStat(stats[7]))); // Number of sets won
			statsText[8].setText(String.valueOf(this.stats.getStat(stats[8]))); // Number of sets lost
			statsText[9].setText(String.valueOf(this.stats.getStat(stats[9]))); 	// Number of sets drawn.
					
			if (!(this.stats.getStat("stats_num_1vAI") == 0)) 
			{
				statsText[2].setText(String.format("%.2f", (double)
							(((double)this.stats.getStat("stats_num_matches_won")/(double)this.stats.getStat("stats_num_1vAI"))*100))+"%");
			} 
			else 
			{ 
				statsText[2].setText("NaN"); 
			}
			
			menuState = MENU_STATE_STATISTICS;
			
			
		} 
		else if (b == menuButtons[4]) 
		{
			// "Back" button, takes us to the main menu.
			menuState = MENU_STATE_MAIN_MENU;
		}
	}
	
	// Handles which sound/music toggles were pressed.
	public synchronized void handleToggle(Toggle t) 
	{
		if (t == soundToggles[0]) 
		{
			sound.enableMusic(soundToggles[0].getToggledState());
		} 
		else if (t == soundToggles[1]) 
		{
			sound.enableSound(soundToggles[1].getToggledState());
		}
		
	}
	
	// Handles when flippable cards in hand are clicked. IE, changing +2 to -2.
	public synchronized void handleCardClicks(GUICard gcard) 
	{
		Card card = gcard.getCard();
		if (card instanceof HandCard && ((HandCard) card).isFlippableCard()) 
		{
			((HandCard) card).flipCard();
			//gcard.setCard(card);
		}
		getGameInformation();
	}
	
	// Handles when cards are dragged and moved.
	public synchronized void handleCardMoves(GUICard gcard) 
	{
		this.game.getPlayer(game.getPlayerTurn()).playCard(gcard.getCardNumber());
		
		if (this.game.getPlayer(game.getPlayerTurn()).getSum() == TwentyFiveGame.SUM_LIMIT) 
		{
			this.game.getPlayer(game.getPlayerTurn()).stand();
		}
		getGameInformation();
	}

	
	/* ---------------------------------------------------------------------
	 * --- Render
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public synchronized void render(Canvas canvas) 
	{
		background.draw(canvas);
		
		
		if (gamePlayingState == GAME_PLAYING_STATE_MENU) 
		{ // Game is currently on the menu.
			
			mainMenu.draw(canvas); // Drawing the menu screen.
			if (menuState > MENU_STATE_MAIN_MENU) 
			{
				mainMenuInfo.draw(canvas);
				
				menuButtons[menuButtons.length-1].draw(canvas); // // Neither the Main Menu or the playing, (Instructions/Statistics), "Back" button necessary
				
				if (menuState == MENU_STATE_STATISTICS) 
				{ // menuState set to Statistics
					for (int i = 0; i < statsText.length; i++) 
					{
						statsText[i].draw(canvas); // Draws the Statistics tab.
					}
				}
				
				
			} 
			else 
			{ // menuState set to Main Menu
				for (int index = 0; index < (menuButtons.length-1); index++) 
				{ 
					// Final value is "Back". Main Menu doesn't have a "Back" button
					menuButtons[index].draw(canvas);
				}
			}
			
			
		} 
		else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING || gamePlayingState == GAME_PLAYING_STATE_WON) 
		{ 
			// gameState is either playing or won
			
			for (int playerID = 0; playerID <= 1; playerID++) 
			{
				// Counter for Sets Won.
				for (int i = 1; i <= 3; i++) 
				{
					if (game.getPlayer(playerID).getSetsWon() >= i)
					{
						pSetIcon[playerID][i].draw(canvas);
					}	
				}
				for (int cardIndex = 0; cardIndex < 9; cardIndex++) 
				{
					// Cards on the playing field.
					if (gamefieldCards[playerID][cardIndex].hasCard()) 
					{
						gamefieldCards[playerID][cardIndex].draw(canvas);
					}
				}

				for (int cardIndex = 0; cardIndex < 4; cardIndex++) 
				{
					// Cards in the player's hand.
					if (handCards[playerID][cardIndex].hasCard()) 
					{
						handCards[playerID][cardIndex].draw(canvas);
					}
				}
				// Player name
				pNames[playerID].draw(canvas);
				// Player score
				pScore[playerID].draw(canvas);
			}
			
			gameButtons[0].draw(canvas);
			gameButtons[1].draw(canvas);
	

			if (gamePlayingState == GAME_PLAYING_STATE_WON) 
			{
				// Creates notify saying Player won the match in center of screen
				notifyBox.draw(canvas);
				notifyText.draw(canvas);
				notifyButton.draw(canvas);
			}
		}
		
		// Drawing sound/music toggle icons
		soundToggles[0].draw(canvas);
		soundToggles[1].draw(canvas);
		
		long now = System.nanoTime();
		FPSCounter.setText("FPS: " + CalcFPSTick((int)((1/((now-lastFPSUpdate)/Math.pow(10,9))))));
		FPSCounter.draw(canvas); // Drawing FPS counter.
		
		// Dragged card has to be at the forefront of the screen!
		if (draggedCard != null) 
		{
			draggedCard.draw(canvas);
		}
		
		lastFPSUpdate = now;
	}
	
	
	/* ---------------------------------------------------------------------
	 * CalcFPS
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public double CalcFPSTick(int newtick) 
	{
	    FPSTickSum -= FPSArray[FPSTickIndex];  /* subtract value falling off */
	    FPSTickSum += newtick;              /* add new value */
	    FPSArray[FPSTickIndex]=newtick;   /* save new value so it can be subtracted later */
	    if (++FPSTickIndex== 100) {    /* inc buffer index */
	    	FPSTickIndex=0;
	    }
	    /* return average */
	    return ((double) FPSTickSum/100);
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * Helper methods for updating the GUI.
	 * ---------------------------------------------------------------------
	 */ 
	

	private void announceWinner(Player winner) 
	{
		if (game.isGameOngoing()) 
		{ 
			return; 
		}
		// Creates notify box for player winning the game.
		notifyText.setText(winner.toString().concat(" won the match!"));
		gamePlayingState = GAME_PLAYING_STATE_WON; // gameState set to won
	}
	
	private void getGameInformation() 
	{
		// Updating the score for each player.
		pScore[0].setText(String.valueOf(game.getPlayer(0).getSum()));
		pScore[1].setText(String.valueOf(game.getPlayer(1).getSum()));
		
		// Did a player bust? Change the color to red!
		pScore[0].setTextMode(game.getPlayer(0).getSum() > TwentyFiveGame.SUM_LIMIT ? "red" : "normal");
		pScore[1].setTextMode(game.getPlayer(1).getSum() > TwentyFiveGame.SUM_LIMIT ? "red" : "normal");
		
		animNames();
		
		// Updating cards in playing field and in hand.
		for (int playerID = 0; playerID <= 1; playerID++)
		{
			// Cards on playing field
			for (int cardIndex = 0; cardIndex < 9; cardIndex++) 
			{
				GUICard gcard = gamefieldCards[playerID][cardIndex];
				if (gcard.getCardNumber() < game.getPlayer(playerID).getStack().size()) 
				{
					gcard.setCard(game.getPlayer(playerID).getStack().get(gcard.getCardNumber()));
				} 
				else { gcard.setCard(null); }
			}
			// Hand Cards
			for (int cardIndex = 0; cardIndex < 4; cardIndex++) 
			{
				handCards[playerID][cardIndex].setCardGraphics();
			}
		}
	}
	
	private void animNames() 
	{
		if (game.getPlayerTurn() == 0) 
		{ 
			// Player 1
			pNames[0].setTextMode("animate");
			pNames[1].setTextMode(game.getPlayer(1).hasCompletedSet() ? "stand" : "normal");
		} else 
		{
			// Player 2
			pNames[0].setTextMode(game.getPlayer(0).hasCompletedSet() ? "stand" : "normal");
			pNames[1].setTextMode("animate");
		}
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Handler methods for events.
	 * ---------------------------------------------------------------------
	 */ 

	@Override
	public void onSetWon(Player winner) 
	{
		// If the set ended in a win for a player
		game.getPlayer(game.getPlayerTurn()).deal();
		getGameInformation();
		
		sound.playSound("set");
	}
	
	@Override
	public void onSetDraw() 
	{
		// If the set ended a draw (more common that initially thought)
		game.getPlayer(game.getPlayerTurn()).deal();
		getGameInformation();
		sound.playSound("draw");
	}
	
	@Override
	public void onMatchWon(Player winner) 
	{
		getGameInformation();
		announceWinner(winner); // Announce who won the match
		sound.playSound("match");
	}
	
	@Override
	public void onPlayerTurnChanged(int playerTurn) 
	{
		// Turn has changed from one player to another.
		game.getPlayer(playerTurn).deal();
		getGameInformation();
		Log.d(TAG, "onPlayerTurnChanged");
		sound.playSound("dealercard");
	}
	
	@Override
	public void onPlayerCardUsed(Player player) 
	{
		// Player has played a card from hand.
		getGameInformation();
		sound.playSound("handcard");
	}

	@Override
	public void onPlayerCardDealt(int playerTurn) 
	{
		// Player has been dealt a card.
		getGameInformation();
		Log.d(TAG, "onPlayerCardDealt");
	}
}
