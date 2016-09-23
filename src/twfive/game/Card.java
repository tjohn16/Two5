package twfive.game;

public abstract class Card 
{

	/* ---------------------------------------------------------------------
	 * --- GUICard Globals
	 * ---------------------------------------------------------------------
	 */ 
	static final public int DEALERMAX = 10; // Highest value that can be dealt is 10
	static final public int DEALERMIN = 1;  // Lowest value that can be dealt is 1
	
	static final public int HANDMAX = 5; // Highest value from hand is 5
	static final public int HANDMIN = 1; // Lowest value from hand is 1
	
	static final public int FLIPPABLEMAX = 5; // Highest value of a flippable card is 5
	static final public int FLIPPABLEMIN = 1; // Lowest value of a flippable card is 1


	/* ---------------------------------------------------------------------
	 * Variables
	 * ---------------------------------------------------------------------
	 */ 
	
	protected int value;
	protected CardNames enumType; // What kind of card is it?
	protected boolean isSpecial;  // Is it a special or normal card?
	
	public Card(CardNames enumType) 
	{
		this.enumType = enumType; // Specifies what kind of card it is
	}

	/* ---------------------------------------------------------------------
	 * Getter methods
	 * ---------------------------------------------------------------------
	 */ 
	
	public int getValue() 
	{
		return value; // Returns the value of the card
	}
	public boolean isSpecial() 
	{
		return isSpecial; // Returns if the card is special or not
	}
	public CardNames getEnumType() 
	{
		return this.enumType; // Returns what type of card it is
	}
	
	
	/* ---------------------------------------------------------------------
	 * Methods
	 * ---------------------------------------------------------------------
	 */ 

	abstract void flipValue(); // Not the same between Dealer and Hand
	

	void doubleValue() 
	{
		this.value *= 2; // Doubles the value of the card
	}

	public String toString() 
	{
		return "[" + this.enumType.toString() + " (" + this.value + ")]"; // Returns the text of what kind of card it is
	}
}