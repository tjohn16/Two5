package twfive.game;

public class DealerCard extends Card 
{

	/* ---------------------------------------------------------------------
	 * --- Constructors
	 * ---------------------------------------------------------------------
	 */
	DealerCard(int val) 
	{
		super(CardNames.DEALER);
		if (val >= DEALERMIN && val <= DEALERMAX) 
		{
			this.value = val;
		} 
		else 
		{
			this.value = 999;
		}
		this.isSpecial = false;
	}
	
	DealerCard() 
	{
		// Dealer card can range between 1-10
		this(DEALERMIN + (int)(Math.random() * ((DEALERMAX - DEALERMIN) + 1)));
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Method flipValue
	 * ---------------------------------------------------------------------
	 */ 
	
	void flipValue() 
	{ 
		this.value = - this.value; // Switch the value to the opposite (-)
	}
}