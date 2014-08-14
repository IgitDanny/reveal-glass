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
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.ViewFlipper;
import vferries.reveal.glass.R;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * The concrete, non-tutorial implementation of the application.
 */
public class SlideshowActivity extends Activity {
	private static String BASE_URL = "http://192.168.0.42:8080/";
	
	
    /** The Unicode character for the hollow circle representing a phrase not yet guessed. */
    private static final char HOLLOW_CIRCLE = '\u25cb';

    /** A light blue color applied to the circle representing the current phrase. */
    private static final int CURRENT_PHRASE_COLOR = Color.rgb(0x34, 0xa7, 0xff);

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
    private ViewFlipper mPhraseFlipper;

    /** TextView containing the dots that represent the scored/unscored phrases in the game. */
    private TextView mGameState;

	private int totalSlides;

	private int currentSlideIndex;

	private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        mPhraseFlipper = (ViewFlipper) findViewById(R.id.phrase_flipper);
        mGameState = (TextView) findViewById(R.id.slides_state);
		makeNetworkCall("init");
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
            if (i > 0) {
                builder.append(' ');
            }

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
        return (TextView) mPhraseFlipper.getCurrentView();
    }

	protected void handleGameGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                break;
            case SWIPE_RIGHT:
                previousSlide();
                break;
            case SWIPE_LEFT:
                nextSlide();
                break;
            default:
            	break;
        }
    }

	private void nextSlide() {
		makeNetworkCall("next");
	}

	private void previousSlide() {
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
				        currentPhrase = obj.getString("message");
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
