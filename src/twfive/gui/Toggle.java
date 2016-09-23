package twfive.gui;

import twfive.android.MainGamePanel;
import android.graphics.Bitmap;
import android.view.MotionEvent;


public class Toggle extends GUIObject 
{

	
	private boolean touched;	// Tracks if the button has been pressed
	
	private Bitmap enabled;
	private Bitmap disabled;
	
	private MainGamePanel parent;
	private boolean toggledState;
	
	public Toggle(MainGamePanel parent, Bitmap enabled, Bitmap disabled, boolean toggledState, int x, int y) 
	{
		super(x, y);
		
		this.parent = parent;
		
		this.enabled = enabled;
		this.disabled = disabled;
			
		this.toggledState = toggledState;
		super.setBitmap(toggledState ? enabled : disabled);
	}
	

	public boolean getToggledState() 
	{
		// Returns what state the toggle is in
		return toggledState;
	}
	
	
	public boolean isTouched() 
	{
		// Returns if something has been touched or not.
		return touched;
	}
	
	public void handleTouch(boolean touched) 
	{
		// Handles if something has been touched
		this.touched = touched;
	}
	
	public void setTouched(boolean touched, boolean isRelease) 
	{
		// Set boolean if anything has been touched and released
		if (this.touched && !touched && isRelease) 
		{
			toggledState = !toggledState; // Toggle state changed
			parent.handleToggle(this);
			super.setBitmap(toggledState ? enabled : disabled);
		}
		handleTouch(touched);
	}
	
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
	public void handleActionDown(int eventX, int eventY) 
	{
		Bitmap bm = super.getBitmap();
		if (eventX >= (super.getX() - bm.getWidth() / 2) && (eventX <= (super.getX() + bm.getWidth()/2))) 
		{
			if (eventY >= (super.getY() - bm.getHeight() / 2) && (eventY <= (super.getY() +bm.getHeight() / 2))) 
			{
				handleTouch(true);
			} 
			else 
			{
				handleTouch(false);
			}
		} 
		else 
		{
			handleTouch(false);
		}

	}

	
}