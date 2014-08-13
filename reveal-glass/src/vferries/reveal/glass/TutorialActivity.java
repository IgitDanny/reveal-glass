package vferries.reveal.glass;

import vferries.reveal.glass.R;
import com.google.android.glass.touchpad.Gesture;

import android.os.Bundle;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the game that acts as a tutorial, restricting certain gestures to match
 * the instruction phrases on the screen.
 */
public class TutorialActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the status bar in tutorial mode.
        findViewById(R.id.status_bar).setVisibility(View.GONE);
    }

    /** Overridden to load the fixed tutorial phrases from the application's resources. */
    @Override
    protected SlidesModel createSlidesModel() {
        List<String> tutorialPhrases = Arrays.asList(getResources().getStringArray(
                R.array.tutorial_phrases));
        return new SlidesModel(tutorialPhrases);
    }

    /**
     * Overridden to only allow the tap gesture on the "Tap to score" screen and to only allow the
     * swipe gesture on the "Swipe to pass" screen. The game is also automatically ended when the
     * final card is either tapped or swiped.
     */
    @Override
    protected void handleGameGesture(Gesture gesture) {
        int phraseIndex = getCharadesModel().getCurrentPhraseIndex();
        switch (gesture) {
            case TAP:
                if (phraseIndex == 2) {
                    score();
                    finish();
                }
                break;
            case SWIPE_LEFT:
            	if (phraseIndex == 1) {
            		pass();
            	} else {
            		tugRight();
            	}
            	break;
            case SWIPE_RIGHT:
                if (phraseIndex == 0) {
                    pass();
                } else {
                	tugLeft();
                }
                break;
            default:
            	break;
        }
    }
}
