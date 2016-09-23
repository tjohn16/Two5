package twfive.gui;

import twfive.android.MainGamePanel;
import twfive.game.Card;
import twfive.game.HandCard;
import twfive.game.TwentyFiveGame;
import twfive.game.Player;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;


public class GUICard extends GUIObject 
{

	
	private boolean touched;	// Tracks if the button has been pressed

	private Card card;
	
	private final MainGamePanel parent;
	
	private final GUIText textValue;
	private final GUIText textType;
	
	private final int originalX;
	private final int originalY;
	
	private int x;
	private int y;
	
	private final int cardNumber;
	private final int playerID;
	private final boolean isHand;

	private boolean dragged;
	
	public GUICard(MainGamePanel parent, boolean isHand, int playerID, int cardNumber, int x, int y) 
	{
		super(x, y);
		
		this.parent = parent;
		
		this.originalX = x;
		this.x = x;
		this.originalY = y;
		this.y = y;
		
		this.cardNumber = cardNumber; // The ID corresponds to card number (1-9,1-4)
		this.playerID = playerID;
		this.isHand = isHand;
		
		this.card = null; 
		
		this.textValue = new GUIText(parent, x, y, (int)(35*parent.getScale()), Paint.Align.CENTER); // Card value
		this.textValue.setText("");
		
		// Card type
		this.textType = new GUIText(parent, x, y+(int)(40*parent.getScale()), (int)(22*parent.getScale()), Paint.Align.CENTER);
		this.textType.setText("");
	}
	
	
	public void setX(int x) 
	{	
		// Sets the width of the card (x)
		int minX = (int) (parent.getWidth()/2 - (parent.getWidth()/2) + super.getBitmap().getWidth()/2);
		int maxX = (int) (parent.getWidth()/2 + (parent.getWidth()/2) - super.getBitmap().getWidth()/2);

		x = minX < x ? x : minX;
		x = maxX > x ? x : maxX;
		
		this.x = x;
		super.setX(x);
		this.textValue.setX(x);
		this.textType.setX(x);
	}
	
	public void setY(int y) 
	{
		// Sets the height of the card (y)
		int minY = (int) (parent.getHeight()/2 - (parent.getGameHeight()/2) + super.getBitmap().getHeight()/2);
		int maxY = (int) (parent.getHeight()/2 + (parent.getGameHeight()/2) - super.getBitmap().getHeight()/2);
		
		y = minY < y ? y : minY;
		y = maxY > y ? y : maxY;
		
		this.y = y;
		super.setY(y);
		this.textValue.setY(y);
		this.textType.setY(y+(int)(40*parent.getScale()));
	}
	
	public void resetPos() 
	{
		// Resets positions of card to original position
		setX(this.originalX);
		setY(this.originalY);
	}
	
	public int getCardNumber() 
	{
		// Returns numeric value of the card
		return this.cardNumber;
	}

	
	public void setCard(Card c) 
	{
		this.card = c;
		if (c != null) 
		{
			setCardGraphics();
		}	
	}
	

	public void setCardGraphics() 
	{
		if (this.card == null) 
		{
			return;
		}
		
		TwentyFiveGame game = parent.getGame();
		int playerTurn = game.getPlayerTurn();
		
		boolean showFace = true;
		
		if (this.isHand) 
		{ 
			// Card is in the hand.
			
			if (playerTurn == this.playerID) 
			{ 
				// Player's turn
				if (game.getPlayer(playerTurn).isControlledByAI()) 
				{ 
					// Never show the cards of the AI
					showFace = false;					
				}
			} 
			else 
			{ 
				// Not currently player's turn
				if (!game.getPlayer(playerTurn).isControlledByAI()) 
				{ 
					// Player hand should be visible at all time
					showFace = false; 
				}
			}
		}
		
		if (!showFace) 
		{
			// Able to see the front side of the card
			super.setBitmap(parent.getBitmapHandler().getCardbackground());
			this.textType.setText("");
			this.textValue.setText("");
			return;
		}

		this.textValue.setText(String.valueOf(this.card.getValue()));
		boolean isPos = this.card.getValue() > 0 ? true : false;
		// TIEBREAKER, FLIPPABLE, THREE_AND_SIX, TWO_AND_FOUR, ONE_AND_FIVE, NORMAL, DEALER;
		switch (this.card.getEnumType()) 
		{
		// What type of card is it?
		// Sets graphics for tiebreaker card.
		case TIEBREAKER:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Tie");
			break;
		// Sets graphics for dealt cardss
		case DEALER:
			if (isPos) 
			{
				super.setBitmap(parent.getBitmapHandler().getCardpositive()); // Card is flippable
			} 
			else 
			{
				super.setBitmap(parent.getBitmapHandler().getCardnegative()); // Card is flippable
			}
			this.textType.setText("");
			break;
		
		// Sets graphics for all flippable cards
		case FLIPPABLE:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Flip");
			break;
		// Sets graphics for regular cards.
		case NORMAL:
			if (isPos) 
			{
				super.setBitmap(parent.getBitmapHandler().getCardpositive()); // Card is positive
			}
			else 
			{
				super.setBitmap(parent.getBitmapHandler().getCardnegative()); // Card is negative
			}
			this.textType.setText("");
			break;
		
		// Sets graphics for stack flipping cards.
		case THREE_AND_SIX:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("3 & 6");
			break;
		case TWO_AND_FOUR:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("2 & 4");
			break;
		case ONE_AND_FIVE:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("1 & 5");
			break;
		// Sets graphics for double card.
		case DOUBLE:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Double");
			break;
		default:
			super.setBitmap(parent.getBitmapHandler().getCardbackground());
			this.textType.setText("");
			break;
		}
	}
	
	
	
	
	
	public Card getCard() 
	{
		return this.card;
	}
	
	public boolean hasCard() 
	{
		if (this.card == null) 
		{
			return false;
		}
		
		if (card instanceof HandCard && this.isHand) 
		{
			return !((HandCard) card).cardPlayed();
		}
		
		return true;
	}
	

	
	public boolean isTouched() 
	{
		return touched;
	}
	
	public void setDragged(boolean dragged) 
	{
		this.dragged = dragged;
	}
	public void setTouched(boolean touched) 
	{
		this.touched = touched;
	}
	
	
	
	
	
	
	@SuppressWarnings("static-access")
	@Override
	public void draw(Canvas canvas) 
	{
		Bitmap bm = super.getBitmap();
		canvas.drawBitmap(bm, x - (bm.getWidth() / 2), y - (bm.getHeight() / 2), super.paint);
		this.textValue.draw(canvas);
		this.textType.draw(canvas);
	}
	
	
	
	private boolean playerCanPerformAction() 
	{
		// Can the player do this?
		int playerTurn = parent.getGame().getPlayerTurn();
		Player player = parent.getGame().getPlayer(playerTurn);
		
		// Long "or" statement and possibilities
		if (player.isControlledByAI() || // Is the player an AI?
			playerTurn != this.playerID || // Card isn't in hand of player whose turn it is
			!this.isHand || // Card doesn't exist in hand
			player.hasCompletedRound() || player.hasCompletedSet() || // Player has already done an action
			player.getStack().size() == TwentyFiveGame.CARD_LIMIT || // Card limit (9) has been reached
			(card instanceof HandCard && ((HandCard) card).cardPlayed()) || // Card has already been played
			!(parent.getGamePlayingState() == 1) ) // Not currently playing
		{ 
				
			return false; // Only able to play one card per turn, stops us from playing more
		}
		return true;
	}
	
	
	
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
	public void handleActionDown(int eventX, int eventY)
	{
		if (!playerCanPerformAction())
		{
			return;
		}
		
		Bitmap bm = super.getBitmap();
		if (eventX >= (super.getX() - bm.getWidth() / 2) && (eventX <= (super.getX() + bm.getWidth()/2))) 
		{
			if (eventY >= (super.getY() - bm.getHeight() / 2) && (eventY <= (super.getY() +bm.getHeight() / 2))) 
			{
				this.touched = true;
				return;
			}
		}
	}
	
	public boolean handleActionMove(int eventX, int eventY) 
	{
		if (!playerCanPerformAction()) 
		{
			return false;
		}
		
		if (this.touched) 
		{ 
			//Only object that has been touched
			Bitmap bm = super.getBitmap();
			int width = bm.getWidth();
			int height = bm.getHeight();
			
			int origLeft = this.originalX - (width / 2);
			int origRight = this.originalX + (width / 2);
			int origTop = this.originalY - (height / 2);
			int origBottom = this.originalY + (height / 2);
			

			if (((eventX >= origRight) || (eventX <= origLeft)) || ((eventY >= origBottom) || (eventY <=origTop))  )
			{ 
				this.setX(eventX);
				this.setY(eventY);
				this.dragged = true;
				return true;
			}
			this.dragged = false;
			this.setX(this.originalX);
			this.setY(this.originalY);
		}
		return false;
	}
	
	
	

	public void handleActionUp(int eventX, int eventY) 
	{
		if (!playerCanPerformAction()) 
		{
			return;
		}
		
		
		if (this.touched == false) 
		{ 
			return; // Card isn't touched, nothing needs to change		
		} 
		Bitmap bm = super.getBitmap();
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		
		if (eventX >= (super.getX() - width / 2) && eventX <= (super.getX() + width/2) && // Release the card
			eventY >= (super.getY() - height / 2) && eventY <= (super.getY() + height / 2)) // Beneath the cursor
		{ 
			if (this.dragged) 
			{
				
				// Actually in game field to play?
				if (this.y < (this.originalY - height*1.2)) 
				{ 
					// Within the playing field
					parent.handleCardMoves(this);
				}
				else 
				{ 
					// Weren't in field, return to original position
					setX(this.originalX);
					setY(this.originalY);
				}
			} 
			else 
			{
				parent.handleCardClicks(this);
				setCardGraphics();
			}
			this.touched = false;
			this.dragged = false;
			return;
		}
		
		this.touched = false;
		this.dragged = false;
		setX(this.originalX);
		setY(this.originalY);
	}

	
}