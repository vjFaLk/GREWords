package com.v.GREWords;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    String definition = "", word = "";
    TextView mainText, defText;
    int currentIndex =-1;
    Button prevButt, nextButt;
    boolean shownDef = false;
    boolean shownFade = false;
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



    private void Initialize()
    {
        defText = (TextView) findViewById(R.id.textView2);
        mainText = (TextView) findViewById(R.id.textView);
        prevButt = (Button) findViewById(R.id.buttonPrev);
        nextButt = (Button) findViewById(R.id.buttonNext);
        nextButt.setVisibility(View.GONE);

    }

    private void setActionListeners() {



        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(200);


                if (shownDef) {
                    findViewById(R.id.textView).clearAnimation();
                    defText.setText(" ");
                }


                if (!shownFade) {
                    nextButt.setVisibility(View.VISIBLE);
                    nextButt.setAnimation(in);
                    shownFade = true;
                }

                if (((wordList.size()) - 1) > currentIndex)
                    getWordFromList();
                else
                    newWord();


            }
        });

        nextButt.setOnClickListener(new View.OnClickListener() {
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

    private void hideDefinition() {
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(300);
        defText.setVisibility(View.GONE);
        defText.setAnimation(out);
        TranslateAnimation translation;
        translation = new TranslateAnimation(0f, 0f,(getDisplayHeight()-getDisplayHeight()/2), 0f);
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
        translation = new TranslateAnimation(0f, 0F, 0f, (getDisplayHeight()-getDisplayHeight()/2));
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

    private boolean checkStack()
    {
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(200);

        if(currentIndex<1)
        {
            nextButt.setVisibility(View.GONE);
            nextButt.setAnimation(out);
            shownFade = false;
            return false;
        }
        return true;
    }

    private void goBack()
    {
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





