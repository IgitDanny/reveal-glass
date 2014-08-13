package vferries.reveal.glass;

import com.google.android.glass.media.Sounds;
import vferries.reveal.glass.R;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class StartAppActivity extends Activity {
    private final Handler mHandler = new Handler();

    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                mAudioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;
            } else {
                return false;
            }
        }
    };

    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_app);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startSlideshow();
                    }
                });
                return true;

            case R.id.instructions:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startTutorial();
                    }
                });
                return true;

            default:
                return false;
        }
    }

    private void startSlideshow() {
        startActivity(new Intent(this, SlideshowActivity.class));
        finish();
    }

    private void startTutorial() {
        startActivity(new Intent(this, TutorialActivity.class));
    }
}
