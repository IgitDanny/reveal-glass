package vferries.reveal.glass;

import com.google.android.glass.media.Sounds;
import vferries.reveal.glass.R;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * An abstract implementation of the application's user interface. This handles functionality shared
 * between the main application and the tutorial, such as displaying the score bar at the bottom of
 * the screen and animations between phrases when a phrase is scored or passed. It is up to
 * subclasses to provide the game's data model and map gestures to the appropriate score/pass
 * logic.
 */
public abstract class BaseActivity extends Activity {

    /** The amount of time to leave the correctly guessed phrase on screen before advancing. */
    private static final long SCORED_PHRASE_DELAY_MILLIS = 500;

    /** The Unicode character for the hollow circle representing a phrase not yet guessed. */
    private static final char HOLLOW_CIRCLE = '\u25cb';

    /** The Unicode character for the filled circle representing a correctly guessed phrase. */
    private static final char FILLED_CIRCLE = '\u25cf';

    /** A light blue color applied to the circle representing the current phrase. */
    private static final int CURRENT_PHRASE_COLOR = Color.rgb(0x34, 0xa7, 0xff);

    /** A light green color applied briefly to a phrase when it is guessed correctly. */
    private static final int SCORED_PHRASE_COLOR = Color.rgb(0x99, 0xcc, 0x33);

    /** Handler used to post a delayed animation when a phrase is scored. */
    private final Handler mHandler = new Handler();

    /** Listener for tap and swipe gestures during the game. */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (areGesturesEnabled()) {
                handleGameGesture(gesture);
                return true;
            }
            return false;
        }
    };

    /** Audio manager used to play system sound effects. */
    private AudioManager mAudioManager;

    /** Detects gestures during the game. */
    private GestureDetector mGestureDetector;

    /** Model that stores the state of the game. */
    private SlidesModel mModel;

    /**
     * Value that can be updated to enable/disable gesture handling in the game. For example,
     * gestures are disabled briefly when a phrase is scored so that the user cannot score or
     * pass again until the animation has completed.
     */
    private boolean mGesturesEnabled;

    /** View flipper with two views used to provide the flinging animations between phrases. */
    private ViewFlipper mPhraseFlipper;

    /** TextView containing the dots that represent the scored/unscored phrases in the game. */
    private TextView mGameState;

    /** Animation used to briefly tug a phrase when the user swipes left. */
    private Animation mTugRightAnimation;
    private Animation mTugLeftAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slideshow);
        setGesturesEnabled(true);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        mPhraseFlipper = (ViewFlipper) findViewById(R.id.phrase_flipper);
        mGameState = (TextView) findViewById(R.id.slides_state);
        mTugRightAnimation = AnimationUtils.loadAnimation(this, R.anim.tug_right);
        mTugLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.tug_left);

        mModel = createSlidesModel();
        updateDisplay();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    /**
     * Subclasses must override this method to create and return the data model that will be used
     * by the game.
     */
    protected abstract SlidesModel createSlidesModel();

    /**
     * Subclasses must override this method to handle {@link Gesture#TAP} and
     * {@link Gesture#SWIPE_RIGHT} gestures that occur during game play. Typically they should
     * call the {@link #score()} method on a tap and the {@link #pass()} method on a swipe, but
     * the tutorial overrides these in certain cases to make the game flow in a predetermined way.
     */
    protected abstract void handleGameGesture(Gesture gesture);

    /** Returns the data model used by this instance of the game. */
    protected SlidesModel getCharadesModel() {
        return mModel;
    }

    /** Plays the sound effect of the specified type. */
    protected void playSoundEffect(int effectType) {
        mAudioManager.playSoundEffect(effectType);
    }

    /**
     * Marks the currently visible phrase as correctly guessed. This method changes the phrase's
     * color to green, flings it off the screen, advances the game model to the next phrase, and
     * flings the new phrase into view.
     */
    protected void score() {
        // Disable gesture handling so that the user can't tap or swipe during the animation.
        setGesturesEnabled(false);

        mModel.markGuessed();
        playSoundEffect(Sounds.SUCCESS);

        getCurrentTextView().setTextColor(SCORED_PHRASE_COLOR);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mModel.areAllPhrasesGuessedCorrectly()) {
                    mPhraseFlipper.showNext();
                    updateDisplay();

                    // Re-enable gesture handling after the delay has passed.
                    setGesturesEnabled(true);
                }
            }
        }, SCORED_PHRASE_DELAY_MILLIS);
    }

    /** Passes on the current phrase and advances to the next one. */
    protected void pass() {
        mModel.pass();
        playSoundEffect(Sounds.SELECTED);
        mPhraseFlipper.showNext();
        updateDisplay();
    }

    /** Updates the main phrase label and score bar with the current state of the game. */
    private void updateDisplay() {
        getCurrentTextView().setText(mModel.getCurrentPhrase());
        getCurrentTextView().setTextColor(Color.WHITE);
        mGameState.setText(buildScoreBar());
    }

    /**
     * Builds and returns a spanned string containing hollow and filled circles that represent the
     * current state and score of the game.
     */
    private CharSequence buildScoreBar() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < mModel.getPhraseCount(); i++) {
            if (i > 0) {
                builder.append(' ');
            }

            if (i == mModel.getCurrentPhraseIndex()) {
                builder.append(HOLLOW_CIRCLE);
                builder.setSpan(new ForegroundColorSpan(CURRENT_PHRASE_COLOR),
                        builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (mModel.isPhraseGuessedCorrectly(i)) {
                builder.append(FILLED_CIRCLE);
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

    /** Returns true if gestures should be processed or false if they should be ignored. */
    private boolean areGesturesEnabled() {
        return mGesturesEnabled;
    }

    /**
     * Enables gesture handling if {@code enabled} is true, otherwise disables gesture handling.
     * Gestures are temporarily disabled when a phrase is scored so that extraneous taps and
     * swipes are ignored during the animation.
     */
    private void setGesturesEnabled(boolean enabled) {
        mGesturesEnabled = enabled;
    }

    /** Plays a tugging animation that provides feedback when the user tries to swipe backward. */
    protected void tugRight() {
        mPhraseFlipper.startAnimation(mTugRightAnimation);
    }
    /** Plays a tugging animation that provides feedback when the user tries to swipe forward. */
    protected void tugLeft() {
        mPhraseFlipper.startAnimation(mTugLeftAnimation);
    }
}