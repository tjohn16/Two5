package twfive.android;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * @author Tom Johnson. Senior Project "TwentyFive".
 */
public class MainThread extends Thread 
{
	
	private static final String TAG = MainThread.class.getSimpleName();
	
	private final static int 	MAX_FPS = 60;	// 60 FPS 

	private final static int	MAX_FRAME_SKIPS = 5;	

	private final static int	FRAME_PERIOD = 1000 / MAX_FPS;	

	private SurfaceHolder surfaceHolder;

	private MainGamePanel gamePanel;

	// Holds gameState.
	private boolean running;
	public void setRunning(boolean running) 
	{
		this.running = running;
	}

	public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) 
	{
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
	}

	@Override
	public void run() 
	{
		Canvas canvas;
		Log.d(TAG, "Starting game loop");

		long startTime;		// Cycle begins.
		long timeDifference;		// Execution time of cycle.
		int sleepTime;		// ms until sleep
		int framesSkipped;	// Frames skipped 

		sleepTime = 0;
		
		while (running) 
		{
			canvas = null;
			// Lock canvas so it can be edited.
			try 
			{
				canvas = this.surfaceHolder.lockCanvas();
				
				synchronized (surfaceHolder) 
				{
					startTime = System.currentTimeMillis();
					framesSkipped = 0;	
					// Renders canvas.
					if (canvas != null) 
					{
						this.gamePanel.render(canvas);	
					}
					
					timeDifference = System.currentTimeMillis() - startTime;
					sleepTime = (int)(FRAME_PERIOD - timeDifference);
					
					if (sleepTime > 0) 
					{
						// Want sleep time to be above 0
						try 
						{
							// Make thread sleep in order to conserve battery.
							Thread.sleep(sleepTime);	
						} 
						catch (InterruptedException e) {}
					}
					
					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) 
					{
						
						sleepTime += FRAME_PERIOD;	// Check if next frame has occurred
						framesSkipped++;
					}
				}
			} 
			finally 
			{
				if (canvas != null) 
				{
					// Unlock after editing.
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}	
		}
	}
	
}