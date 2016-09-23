package twfive.gui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;


public abstract class GUIObject 
{
	protected final static Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
	

	private Bitmap bitmap;	// Current bitmap
	private int x;			// X coordinates
	private int y;			// Y coordinates

	
	public GUIObject(int x, int y) 
	{
		this.x = x;
		this.y = y;
	}
	
	// Getter methods
	
	public Bitmap getBitmap() 
	{
		return bitmap; // Returns bitmap
	}
	public int getX() 
	{
		return x; // Returns X coordinates
	}
	public int getY() 
	{
		return y; // Returns Y coordinates
	}
	
	
	
	public void setBitmap(Bitmap bitmap) 
	{
		this.bitmap = bitmap; // Sets bitmap
	}
	public void setX(int x) 
	{
		this.x = x; // Sets X coordinates
	}
	public void setY(int y) 
	{
		this.y = y; // Sets Y coordinates
	}

	
	public void draw(Canvas canvas)
	{
		// Draws the Bitmap to the canvas
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), paint);
	}


}