package twfive.gui;

import twfive.android.MainGamePanel;


import twfive.android.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapHandler 
{

	
	private Resources resources;
	
	private final Double scale;
	
	private Bitmap[] bitmaps;
	
	private BitmapFactory.Options options;

	
	public BitmapHandler(MainGamePanel game) 
	{
		this.resources = game.getResources();
		this.scale = game.getScale();
	
		this.options = new BitmapFactory.Options();
		this.options.inPurgeable = true;
		this.options.inInputShareable = true;
		this.options.inDither = true;
		
		
		
		bitmaps = new Bitmap[19]; 
		bitmaps[0] =  getImage(R.drawable.background);
		bitmaps[1] =  getImage(R.drawable.button);
		bitmaps[2] =  getImage(R.drawable.buttonlong);
		bitmaps[3] =  getImage(R.drawable.cardbackground);
		bitmaps[4] =  getImage(R.drawable.carddealer);
		bitmaps[5] =  getImage(R.drawable.cardnegative);
		bitmaps[6] =  getImage(R.drawable.cardpositive);
		bitmaps[7] =  getImage(R.drawable.cardspecial);
		bitmaps[9] =  getImage(R.drawable.instructions1);
		bitmaps[10] = getImage(R.drawable.graphitemenu);
		bitmaps[11] = getImage(R.drawable.musicicon);
		bitmaps[12] = getImage(R.drawable.musicicondisabled);
		bitmaps[13] = getImage(R.drawable.notificationbox);
		bitmaps[14] = getImage(R.drawable.setlight);
		bitmaps[15] = getImage(R.drawable.soundicon);
		bitmaps[16] = getImage(R.drawable.soundicondisabled);
		bitmaps[17] = getImage(R.drawable.statistics);

		
		this.resources = null;
		this.options = null;
	}
	
	/* *******************************
	 * Getter Functions
	 * *******************************
	 */
	
	public Bitmap getBackground() 				{ return bitmaps[0]; }
	// Image for background
	public Bitmap getButton() 					{ return bitmaps[1]; }
	// Image for short button
	public Bitmap getButtonlong() 				{ return bitmaps[2]; }
	// Image for long button
	public Bitmap getCardbackground()			{ return bitmaps[3]; }
	// Image for background of cards
	public Bitmap getCarddealer()				{ return bitmaps[4]; } 
	// Image for dealt cards
	public Bitmap getCardnegative() 			{ return bitmaps[5]; }
	// Image for negative cards
	public Bitmap getCardpositive() 			{ return bitmaps[6]; } 
	// Image for positive cards
	public Bitmap getCardspecial() 				{ return bitmaps[7]; }
	// Image for special cards
	public Bitmap getInstructions() 			{ return bitmaps[9]; }
	// Image for instruction tab
	public Bitmap getMenu() 					{ return bitmaps[10]; }
	// Image for menu (changed to graphitemenu)
	public Bitmap getMusicicon() 				{ return bitmaps[11]; }
	// Image for Music toggle icon
	public Bitmap getMusicicondisabled() 		{ return bitmaps[12]; }
	// Image for red X through music when disabled
	public Bitmap getNotificationbox() 			{ return bitmaps[13]; }
	// Image for notification box when game is won
	public Bitmap getSetlight() 				{ return bitmaps[14]; }
	// Image for Set counter light
	public Bitmap getSoundicon() 				{ return bitmaps[15]; }
	// Image for sound toggle icon
	public Bitmap getSoundicondisabled() 		{ return bitmaps[16]; }
	// Image for red X through sound when disabled
	public Bitmap getStatistics() 				{ return bitmaps[17]; }
	// Image for statistics tab (no values)
	
	public Bitmap getBitmapFromIndex(int index) 
	{
		return bitmaps[index];
	}

	/* ---------------------------------------------------------------------
	 * --- Bitmap image methods
	 * ---------------------------------------------------------------------
	 */ 
	
	
	private Bitmap getImage(int id) 
	{
	    Bitmap img = BitmapFactory.decodeResource(resources, id, options );	    
	    return Bitmap.createScaledBitmap(img, (int)(img.getWidth()*scale), (int)(img.getHeight()*scale), true);
	}

	public Bitmap getScaledBitmap(int id) 
	{
		// Image of scaled bm
		return bitmaps[id];
	}	
}
