package twfive.android;

import twfive.game.Statistics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class TwentyFiveActivity extends Activity 
{
    /** Called when the activity is first created. */
	
	private static final String TAG = TwentyFiveActivity.class.getSimpleName();
	
	private GameSound sound;
	
	private SharedPreferences savedVars;
	private Statistics stats;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        // Turn off title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set game to full screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        savedVars = getSharedPreferences("TwentyFive", 0);
        sound = new GameSound(this, savedVars);
        stats = new Statistics(savedVars);
        
        MainGamePanel game = new MainGamePanel(this, sound, stats);
        
        setContentView(game);
        Log.d(TAG, "View added");
        
        sound.prepareMusic();
        
	    super.onCreate(savedInstanceState);	    
    }
    
	@Override
	protected void onDestroy() 
	{
		// Destroy for cleanup
		Log.d(TAG, "Destroying...");
		sound.releaseMusic();
		stats.registerForfeit();
		System.gc();
		super.onDestroy();
	}

	@Override
	protected void onStop() 
	{
		// Stop activity
		Log.d(TAG, "Stopping...");
		sound.pauseMusic();
		System.gc();
		super.onStop();
	}
	
	@Override
	protected void onResume() 
	{
		// Resume activity
		super.onResume();
		sound.resumeMusic();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) // Android 14 API
	@Override
	public void onTrimMemory(int level) 
	{
		if (level == TRIM_MEMORY_UI_HIDDEN) 
		{
			sound.pauseMusic();
		}
		super.onTrimMemory(level);
	}
    
}