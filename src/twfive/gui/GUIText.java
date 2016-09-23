package twfive.gui;

import twfive.android.MainGamePanel;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;


public class GUIText 
{
	private String text;
	private int textSize;
	private int x;			// X coordinate
	private int y;			// Y coordinate
	
	private Paint paint;
	ValueAnimator colorAnimation;

	
	public GUIText(MainGamePanel parent, int x, int y, int textSize, Paint.Align align) 
	{
		this.x = x;
		this.y = y;
		
		this.textSize = textSize;
		
		// Color, Style, and Size of text set.
		paint = new Paint();
		paint.setTypeface(parent.font);
		paint.setAntiAlias(true);
		paint.setTextAlign(align);
		paint.setShadowLayer((float)(1), 2, 2, Color.BLACK);
		paint.setColor(Color.CYAN);
		paint.setStyle(Style.FILL);
		paint.setTextSize(textSize);
	
		
		this.colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.CYAN, Color.GREEN);
		this.colorAnimation.addUpdateListener(new AnimatorUpdateListener() 
		{

		    @Override
		    public void onAnimationUpdate(ValueAnimator animator) 
		    {
		    	paint.setColor((Integer)animator.getAnimatedValue());
		    }
		});
		this.colorAnimation.setRepeatCount(ValueAnimator.INFINITE); // Repeat animation 
		this.colorAnimation.setRepeatMode(ValueAnimator.REVERSE);   // Reverse direction from infinite
		
		
		
	}
	
	// Getter methods
	
	public String getText()
	{
		// Returns text
		return text;
	}

	public int getX() 
	{
		// Returns X coordinates
		return x;
	}
	public int getY() 
	{
		// Returns Y coordinates
		return y;
	}	
	
	public void setText(String text) 
	{
		this.text = text;
	}
	public void setX(int x) 
	{
		this.x = x;
	}
	public void setY(int y) 
	{
		this.y = y;
	}
	
	
	private void enableAnimation(boolean enabled) 
	{
		if (enabled) 
		{
			colorAnimation.start();
		}
		else 
		{
			colorAnimation.end();
		}
	}
	
	
	public void setTextMode(String s) 
	{
		enableAnimation(false);
		
		if (s.equals("stand")) 
		{
			// Sets paint color to yellow.
			paint.setColor(Color.YELLOW);
			
		} 
		else if (s.equals("animate"))
		{
			// Animation is turned on
			enableAnimation(true);
			
		} 
		else if (s.equals("normal"))
		{
			// Sets paint color to gray.
			paint.setColor(Color.LTGRAY);
			
		} 
		else if (s.equals("red")) 
		{
			// Sets paint color to red.
			paint.setColor(Color.RED);
		}
	}
	


	// Draws text on the canvas
	public void draw(Canvas canvas) 
	{
		canvas.drawText(text , x, y - (textSize/2), paint);
	}


}