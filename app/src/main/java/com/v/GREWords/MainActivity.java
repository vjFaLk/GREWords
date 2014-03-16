package com.v.GREWords;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {


    String definition = "";
    TextView mainText, defText;
    Button nextBut;
    boolean shownDef = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        changeWord();
        setActionListeners();



    }

    private void setActionListeners()
    {
        nextBut = (Button)findViewById(R.id.button);
        nextBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shownDef==true)
                {
                findViewById(R.id.textView).clearAnimation();
                    defText.setText(" ");
                }

                changeWord();

            }
        });

        mainText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shownDef==false)
                showDefinition();
            }
        });


    }



    private void changeWord() {
        mainText = (TextView) findViewById(R.id.textView);
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
           mainText.setText(data.getWord());
            mainText.setAnimation(in);
           definition = data.getDefinition();
        } catch (SQLException sqle) {

            throw sqle;
        }

        shownDef = false;
    }

    private void showDefinition()
    {
        defText = (TextView) findViewById(R.id.textView2);
        findViewById(R.id.textView).clearAnimation();
        TranslateAnimation translation;
        translation = new TranslateAnimation(0f, 0F, 0f, getDisplayHeight()-300 );
        translation.setStartOffset(0);
        translation.setDuration(500);
        translation.setFillAfter(true);
        translation.setInterpolator(new DecelerateInterpolator());
        findViewById(R.id.textView).startAnimation(translation);
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(500);
        defText.setText(definition);
        defText.startAnimation(in);
        shownDef = true;



    }

    private int getDisplayHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
    }





