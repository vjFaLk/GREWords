package com.v.GREWords;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.slidinglayer.SlidingLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {


    String definition = "", word = "";
    TextView mainText, defText;
    int currentIndex = -1;
    Button prevButt, nextButt, TTSButt, nightModeButt, resetButt, invisbleButt, searchButt;
    boolean shownDef = false, shownFade = false, isNightMode = false, defChecked = false;
    TextToSpeech TTSObj;
    List wordList = new ArrayList();
    List wordIDList = new ArrayList();
    List defList = new ArrayList();
    DataBaseHelper data = new DataBaseHelper(this);
    ArrayList<String> listItems, listAlphabets;
    ArrayAdapter<String> adapter, alphaAdapter;
    SearchView search;
    ListView list, alphalist;
    SlidingLayer slidingLayerRight, slidingLayerLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main);
        else
            setContentView(R.layout.activity_main_landscape);

        Initialize();
        setActionListeners();
        getWord();
        // populateAlphabets();


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        list.setAdapter(adapter);
        list.setTextFilterEnabled(true);

        alphaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listAlphabets);
        alphalist.setAdapter(alphaAdapter);
        alphalist.setTextFilterEnabled(true);

        alphaAdapter.notifyDataSetChanged();

    }


    private void Initialize() {
        list = (ListView) findViewById(R.id.listView);
        alphalist = (ListView) findViewById(R.id.alphabetlistView);
        defText = (TextView) findViewById(R.id.textView2);
        mainText = (TextView) findViewById(R.id.textView);
        prevButt = (Button) findViewById(R.id.buttonPrev);
        nextButt = (Button) findViewById(R.id.buttonNext);
        TTSButt = (Button) findViewById(R.id.TTSButton);
        resetButt = (Button) findViewById(R.id.resetButton);
        nightModeButt = (Button) findViewById(R.id.NightButton);
        invisbleButt = (Button) findViewById(R.id.invisibleButton);
        searchButt = (Button) findViewById(R.id.searchButton);
        search = (SearchView) findViewById(R.id.searchView);
        slidingLayerRight = (SlidingLayer) findViewById(R.id.slidingLayer1);
        slidingLayerRight.setShadowWidthRes(R.dimen.shadow_width);
        slidingLayerRight.setOffsetWidth(12);
        slidingLayerRight.setShadowDrawable(R.drawable.sidebar_shadow);
        slidingLayerRight.setStickTo(SlidingLayer.STICK_TO_RIGHT);
        slidingLayerRight.setCloseOnTapEnabled(true);
        slidingLayerLeft = (SlidingLayer) findViewById(R.id.slidingLayer2);
        slidingLayerLeft.setShadowWidthRes(R.dimen.shadow_width);
        slidingLayerLeft.setOffsetWidth(12);
        slidingLayerLeft.setShadowDrawable(R.drawable.sidebar_shadow);
        slidingLayerLeft.setStickTo(SlidingLayer.STICK_TO_LEFT);
        slidingLayerLeft.setCloseOnTapEnabled(true);
        listAlphabets = new ArrayList<String>();
        listItems = new ArrayList<String>();
        prevButt.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        resetButt.setVisibility(View.GONE);
        slidingLayerLeft.setVisibility(View.GONE);


    }

    private void populateAlphabets() {
        char temp;

        for (int i = 65, j = 0; i <= 91; i++, j++) {
            temp = (char) i;
            listAlphabets.add(String.valueOf(temp));
        }


    }


    private void setActionListeners() {


        searchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayerRight.openLayer(true);

            }
        });

        slidingLayerRight.setOnInteractListener(new SlidingLayer.OnInteractListener() {


            @Override
            public void onOpen() {

                if (search.isIconified())
                    search.setIconified(false);


            }

            @Override
            public void onClose() {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

            }

            @Override
            public void onOpened() {

            }

            @Override
            public void onClosed() {


            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String s) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                list.setVisibility(View.VISIBLE);
                listItems.clear();
                if (!s.isEmpty()) {
                    listItems.addAll(data.getWordListForSearch(search.getQuery().toString()));
                } else {
                    listItems.clear();
                }
                adapter.notifyDataSetChanged();
                return false;
            }
        });



        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(200);
                String tempWord = ((TextView) view).getText().toString();
                String tempDef = data.getDefinitionforWord(tempWord);
                mainText.setText(tempWord);
                mainText.setAnimation(in);
                definition = tempDef;
                word = tempWord;

                slidingLayerRight.closeLayer(true);





            }
        });


        nightModeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleNightMode();
            }
        });

        invisbleButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shownDef)
                    hideDefinition();
            }
        });

        resetButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.resetAllRead();

                Toast toast = Toast.makeText(getApplicationContext(), "Stats Reset", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        TTSObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    TTSObj.setLanguage(Locale.UK);
                }

            }
        });

        TTSButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pronounceWord();
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
                    getWord();

                defChecked = false;


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

                defChecked = true;


            }
        });


    }


    private void toggleNightMode() {
        RelativeLayout lay = (RelativeLayout) findViewById(R.id.container);
        ObjectAnimator colorFade = ObjectAnimator.ofObject(lay, "backgroundColor", new ArgbEvaluator(), Color.argb(211, 211, 211, 211), 0xff000000);
        colorFade.setDuration(500);

        if (!isNightMode) {
            colorFade.start();
            mainText.setTextColor(Color.LTGRAY);
            defText.setTextColor(Color.LTGRAY);
            prevButt.setBackgroundResource(R.drawable.n_ic_action_prev_item);
            nextButt.setBackgroundResource(R.drawable.n_ic_action_next_item);
            nightModeButt.setBackgroundResource(R.drawable.n_ic_action_brightness_high);
            TTSButt.setBackgroundResource(R.drawable.n_ic_action_volume_on);
            resetButt.setBackgroundResource(R.drawable.n_ic_action_refresh);
            searchButt.setBackgroundResource(R.drawable.n_ic_action_search);

            isNightMode = true;
        } else {
            colorFade.reverse();
            mainText.setTextColor(Color.BLACK);
            defText.setTextColor(Color.BLACK);
            prevButt.setBackgroundResource(R.drawable.action_prev_item);
            nextButt.setBackgroundResource(R.drawable.action_next_item);
            nightModeButt.setBackgroundResource(R.drawable.ic_action_brightness_high);
            TTSButt.setBackgroundResource(R.drawable.ic_action_volume_on);
            resetButt.setBackgroundResource(R.drawable.ic_action_refresh);
            searchButt.setBackgroundResource(R.drawable.ic_action_search);
            isNightMode = false;
        }

    }


    private void pronounceWord() {
        TTSObj.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void hideDefinition() {
        if (shownDef) {
            Animation out = new AlphaAnimation(1.0f, 0.0f);
            out.setDuration(300);
            defText.setVisibility(View.GONE);
            defText.setAnimation(out);
            TranslateAnimation translation;
            translation = new TranslateAnimation(0f, 0f, -getDisplayHeight() / 2, 0f);
            translation.setStartOffset(0);
            translation.setDuration(300);
            translation.setFillAfter(true);
            translation.setInterpolator(new AccelerateDecelerateInterpolator());
            findViewById(R.id.textView).startAnimation(translation);
            shownDef = false;
        }
    }


    private void showDefinition() {
        if (!shownDef) {
            Animation in = new AlphaAnimation(0.0f, 1.0f);
            in.setDuration(300);
            findViewById(R.id.textView).clearAnimation();
            defText.setVisibility(View.VISIBLE);
            TranslateAnimation translation;
            translation = new TranslateAnimation(0f, 0F, 0f, -getDisplayHeight() / 2);
            translation.setStartOffset(0);
            translation.setDuration(400);
            translation.setFillAfter(true);
            translation.setInterpolator(new OvershootInterpolator());
            findViewById(R.id.textView).startAnimation(translation);
            defText.setText(definition);
            defText.startAnimation(in);
            shownDef = true;
        }

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


    private boolean checkList() {
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
        word = mainText.getText().toString();
        mainText.setAnimation(in);
        definition = defList.get(currentIndex).toString();
        shownDef = false;
        checkList();

    }


    private void getWord() {


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
        wordIDList.add(data.getWordID());
        defList.add(definition);


        currentIndex++;

        shownDef = false;
    }


    private int getDisplayHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        data.setWordsAsRead(wordIDList);

    }

}

