package vferries.reveal.glass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;
import vferries.reveal.glass.R;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

/**
 * The concrete, non-tutorial implementation of the application.
 */
public class SlideshowActivity extends Activity {
	private static String BASE_URL = "http://192.168.0.42:8080/";
	
    /** The Unicode character for the hollow circle representing a phrase not yet guessed. */
    private static final char HOLLOW_CIRCLE = '\u25cb';

    /** A light blue color applied to the circle representing the current phrase. */
    private static final int CURRENT_PHRASE_COLOR = Color.rgb(0x34, 0xa7, 0xff);

    private boolean voiceMenuEnabled;

    /** Listener for tap and swipe gestures during the game. */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            handleGameGesture(gesture);
            return false;
        }
    };

    private String currentPhrase;

    /** View flipper with two views used to provide the flinging animations between phrases. */
    private ViewFlipper phraseFlipper;

    /** TextView containing the dots that represent the scored/unscored phrases in the game. */
    private TextView mGameState;

	private int totalSlides;

	private int currentSlideIndex;

	private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_slideshow);
        voiceMenuEnabled = false;
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        phraseFlipper = (ViewFlipper) findViewById(R.id.phrase_flipper);
        mGameState = (TextView) findViewById(R.id.slides_state);
		makeNetworkCall("init");
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.next:
                    nextSlide();
                	break;
                case R.id.previous:
                	previousSlide();
                    break;
                default:
                    return true;
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            return voiceMenuEnabled;
        }
        return super.onPreparePanel(featureId, view, menu);
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    private void updateDisplay() {
        getCurrentTextView().setText(currentPhrase);
        getCurrentTextView().setTextColor(Color.WHITE);
        mGameState.setText(buildScoreBar());
	}

    private CharSequence buildScoreBar() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < totalSlides; i++) {
            if (i == currentSlideIndex) {
                builder.append(HOLLOW_CIRCLE);
                builder.setSpan(new ForegroundColorSpan(CURRENT_PHRASE_COLOR),
                        builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.append(HOLLOW_CIRCLE);
            }
        }
        return builder;
    }

    
    /** Returns the {@code TextView} inside the flipper that is currently on-screen. */
    private TextView getCurrentTextView() {
        return (TextView) phraseFlipper.getCurrentView();
    }

	protected void handleGameGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
            	voiceMenuEnabled = !voiceMenuEnabled;
                getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
                break;
            case SWIPE_RIGHT:
                nextSlide();
                break;
            case SWIPE_LEFT:
                previousSlide();
                break;
            case TWO_TAP:
            	voiceMenuEnabled = !voiceMenuEnabled;
                getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
            	break;
            default:
            	break;
        }
    }

	private void nextSlide() {
		phraseFlipper.showNext();
		makeNetworkCall("next");
	}

	private void previousSlide() {
		phraseFlipper.showPrevious();
		makeNetworkCall("previous");
	}
	
	private void makeNetworkCall(final String action) {
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
				    HttpResponse response = httpclient.execute(new HttpGet(BASE_URL + action));
				    StatusLine statusLine = response.getStatusLine();
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				        ByteArrayOutputStream out = new ByteArrayOutputStream();
				        response.getEntity().writeTo(out);
				        out.close();
				        String result = out.toString();
				        Log.e("MESSAGE RECEIVED", result);
				        JSONObject obj = new JSONObject(result);
				        currentPhrase = obj.getString("message").trim();
				        currentSlideIndex = obj.getInt("slideNumber");
				        totalSlides = obj.getInt("totalSlides");
				        SlideshowActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
						        updateDisplay();
							}
						});
				    } else{
				        //Closes the connection.
				        response.getEntity().getContent().close();
				        throw new IOException(statusLine.getReasonPhrase());
				    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
