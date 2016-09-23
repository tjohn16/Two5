package twfive.game;

public class HandCard extends Card 
{
	private boolean used;
	
	public HandCard(CardNames type, int val) 
	{
		super(type);
		this.used = false;
		this.value = val;
		
		switch (type) 
		{
			case TIEBREAKER:
			case FLIPPABLE:
			case ONE_AND_FIVE:
			case TWO_AND_FOUR:
			case THREE_AND_SIX:
			case DOUBLE:
				this.isSpecial = true;
				return;
			case NORMAL:
				this.isSpecial = false;
				return;
			default:
				this.isSpecial = true;
				this.value = 999;
		}
	}
	

	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 
	
	public boolean cardPlayed() 
	{
		// Returns if a card has been used
		return used;
	}
	
	public boolean isFlippableCard() 
	{
		// The card can be flipped before being played
		return (super.getEnumType() == CardNames.FLIPPABLE || super.getEnumType() == CardNames.TIEBREAKER);
	}
	
	public boolean hasStackImplications() 
	{	// Alters multiple values within the stack
		return (super.getEnumType() == CardNames.TWO_AND_FOUR ||  // Two and four reversed
				super.getEnumType() == CardNames.THREE_AND_SIX || // Three and six reversed
				super.getEnumType() == CardNames.ONE_AND_FIVE ||  // One and five reversed
				super.getEnumType() == CardNames.DOUBLE			  // Doubles last value on the stack
				);
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Methods
	 * ---------------------------------------------------------------------
	 */ 
	
	void setUsed() 
	{
		// Set if a card has been used to true
		this.used = true;
	}
	
	void flipValue() 
	{
		if (this.used || ((!this.used) && isFlippableCard())) 
		{
			this.value = - this.value; // Switch the value to the opposite (-)
		}
	}
	
	public void flipCard() 
	{ 
		if ((!this.used) && isFlippableCard()) 
		{
			this.value = - this.value; // Switch the value to the opposite (-)
		}
	}
	
	

}