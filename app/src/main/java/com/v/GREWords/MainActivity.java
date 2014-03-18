package com.v.GREWords;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {


    String definition = "", word = "";
    TextView mainText, defText;
    int currentIndex = -1;
    Button prevButt, nextButt, TTSButt, nightModeButt;
    boolean shownDef = false, shownFade = false, isNightMode = false;
    TextToSpeech TTSObj;
    List wordList = new ArrayList();
    List defList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Initialize();
        setActionListeners();
        newWord();


    }


    private void Initialize() {
        defText = (TextView) findViewById(R.id.textView2);
        mainText = (TextView) findViewById(R.id.textView);
        prevButt = (Button) findViewById(R.id.buttonPrev);
        nextButt = (Button) findViewById(R.id.buttonNext);
        TTSButt = (Button) findViewById(R.id.TTSButton);
        nightModeButt = (Button) findViewById(R.id.NightButton);
        prevButt.setVisibility(View.GONE);

    }

    private void setActionListeners() {

        nightModeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout lay = (RelativeLayout) findViewById(R.id.container);
                ObjectAnimator colorFade = ObjectAnimator.ofObject(lay, "backgroundColor", new ArgbEvaluator(), Color.argb(211, 211, 211, 211), 0xff000000);
                colorFade.setDuration(1000);

                if (!isNightMode) {
                    colorFade.start();
                    mainText.setTextColor(Color.LTGRAY);
                    defText.setTextColor(Color.LTGRAY);
                    prevButt.setBackgroundResource(R.drawable.n_ic_action_prev_item);
                    nextButt.setBackgroundResource(R.drawable.n_ic_action_next_item);
                    nightModeButt.setBackgroundResource(R.drawable.n_ic_action_brightness_high);
                    TTSButt.setBackgroundResource(R.drawable.n_ic_action_volume_on);

                    isNightMode = true;
                } else {
                    colorFade.reverse();
                    mainText.setTextColor(Color.BLACK);
                    defText.setTextColor(Color.BLACK);
                    prevButt.setBackgroundResource(R.drawable.action_prev_item);
                    nextButt.setBackgroundResource(R.drawable.action_next_item);
                    nightModeButt.setBackgroundResource(R.drawable.ic_action_brightness_high);
                    TTSButt.setBackgroundResource(R.drawable.ic_action_volume_on);
                    isNightMode = false;
                }


            }
        });

        TTSObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    TTSObj.setLanguage(Locale.US);
                }

            }
        });

        TTSButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PronounceWord();
            }
        });

        nextButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(200);


                if (shownDef) {
                    findViewById(R.id.textView).clearAnimation();
                    defText.setText(" ");
                }


                if (!shownFade) {
                    prevButt.setVisibility(View.VISIBLE);
                    prevButt.setAnimation(in);
                    shownFade = true;
                }

                if (((wordList.size()) - 1) > currentIndex)
                    getWordFromList();
                else
                    newWord();


            }
        });

        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex > 0)
                    goBack();


            }
        });

        mainText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shownDef == false)
                    showDefinition();
                else
                    hideDefinition();
            }
        });

    }

    private void PronounceWord() {
        TTSObj.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void hideDefinition() {
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(300);
        defText.setVisibility(View.GONE);
        defText.setAnimation(out);
        TranslateAnimation translation;
        translation = new TranslateAnimation(0f, 0f, (getDisplayHeight() - getDisplayHeight() / 2), 0f);
        translation.setStartOffset(0);
        translation.setDuration(400);
        translation.setFillAfter(true);
        translation.setInterpolator(new DecelerateInterpolator());
        findViewById(R.id.textView).startAnimation(translation);
        shownDef = false;
    }

    private void showDefinition() {

        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(300);
        findViewById(R.id.textView).clearAnimation();
        defText.setVisibility(View.VISIBLE);
        TranslateAnimation translation;
        translation = new TranslateAnimation(0f, 0F, 0f, (getDisplayHeight() - getDisplayHeight() / 2));
        translation.setStartOffset(0);
        translation.setDuration(400);
        translation.setFillAfter(true);
        translation.setInterpolator(new DecelerateInterpolator());
        findViewById(R.id.textView).startAnimation(translation);

        defText.setText(definition);
        defText.startAnimation(in);
        shownDef = true;

    }

    private void getWordFromList() {

        currentIndex++;
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(200);
        word = wordList.get(currentIndex).toString();
        definition = defList.get(currentIndex).toString();
        mainText.setText(word);
        mainText.setAnimation(in);
        shownDef = false;


    }

    private boolean checkStack() {
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(200);

        if (currentIndex < 1) {
            prevButt.setVisibility(View.GONE);
            prevButt.setAnimation(out);
            shownFade = false;
            return false;
        }
        return true;
    }

    private void goBack() {
        currentIndex--;
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(200);
        findViewById(R.id.textView).clearAnimation();
        defText.setText(" ");
        mainText.setText(wordList.get(currentIndex).toString());
        mainText.setAnimation(in);
        definition = defList.get(currentIndex).toString();
        shownDef = false;
        checkStack();

    }


    private void newWord() {

        DataBaseHelper data;
        data = new DataBaseHelper(this);
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(500);


        try {
            data.createDataBase();
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }

        try {

            data.openDataBase();
            word = data.getWord();
            mainText.setText(word);
            mainText.setAnimation(in);
            definition = data.getDefinition();
        } catch (SQLException sqle) {

            throw sqle;
        }

        wordList.add(word);
        defList.add(definition);

        currentIndex++;

        shownDef = false;
    }


    private int getDisplayHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
}





