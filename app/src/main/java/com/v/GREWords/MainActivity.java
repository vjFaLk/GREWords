package com.v.GREWords;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
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


    private String word = "";
    private TextView mainText, defText;
    private int currentIndex = -1;
    private Button prevButt, nextButt, TTSButt, nightModeButt, resetButt, invisibleButt, searchButt;
    private boolean shownDef = false, shownFade = false, isNightMode = false, defChecked = false, isAlphabetSelected = false;
    private TextToSpeech TTSObj;
    private List wordList = new ArrayList();
    private List wordIDList = new ArrayList();
    private DataBaseHelper data = new DataBaseHelper(this);
    private ArrayList<String> listItems, listAlphabets;
    private ArrayAdapter<String> adapter, alphaAdapter;
    private SearchView search;
    private ListView list, alphalist;
    private SlidingLayer slidingLayerRight, slidingLayerLeft;
    private RelativeLayout relLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        Initialize();
        setActionListeners();
        getWord();
        populateAlphabets();


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
        invisibleButt = (Button) findViewById(R.id.invisibleButton);
        searchButt = (Button) findViewById(R.id.searchButton);
        search = (SearchView) findViewById(R.id.searchView);
        relLay = (RelativeLayout) findViewById(R.id.container);
        setSlidingLayers();
        listAlphabets = new ArrayList<String>();
        listItems = new ArrayList<String>();
        prevButt.setVisibility(View.GONE);

        resetButt.setVisibility(View.GONE);

    }

    private void setSlidingLayers() {
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

    }


    private void populateAlphabets() {
        char temp;

        listAlphabets.add("∅");
        for (int i = 65; i < 91; i++) {
            if (i != 88) {
                temp = (char) i;
                listAlphabets.add(String.valueOf(temp));
            }
        }


    }


    private void setActionListeners() {


        relLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slidingLayerLeft.isOpened() || slidingLayerRight.isOpened()) {
                    slidingLayerLeft.closeLayer(true);
                    slidingLayerRight.closeLayer(true);
                }
            }
        });

        alphalist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Animation out = new AlphaAnimation(1.0f, 0.0f);
                out.setDuration(300);
                Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(300);
                if (i != 0) {
                    String letter = ((TextView) view).getText().toString();
                    wordList = data.getWordByAlphabet(letter);
                    slidingLayerLeft.closeLayer(true);
                    mainText.setText(wordList.get(0).toString());
                    mainText.setAnimation(in);
                    currentIndex = 0;
                    if (prevButt.getVisibility() != View.GONE) {
                        prevButt.setVisibility(View.GONE);
                        prevButt.setAnimation(out);
                    }
                    isAlphabetSelected = true;
                    search.setQuery(letter, false);
                    shownFade = false;
                } else {
                    slidingLayerLeft.closeLayer(true);
                    wordList.clear();
                    currentIndex = 0;
                    out.setDuration(300);
                    if (prevButt.getVisibility() != View.GONE) {
                        prevButt.setVisibility(View.GONE);
                        prevButt.setAnimation(out);
                    }
                    isAlphabetSelected = false;
                    shownFade = false;
                    getWord();
                }
                hideDefinition();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setQuery("", false);
            }
        });


        searchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayerRight.openLayer(true);

            }
        });

        slidingLayerRight.setOnInteractListener(new SlidingLayer.OnInteractListener() {


            @Override
            public void onOpen() {


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
                in.setDuration(300);
                String tempWord = ((TextView) view).getText().toString();
                mainText.setText(tempWord);
                mainText.setAnimation(in);
                word = tempWord;
                hideDefinition();
                slidingLayerRight.closeLayer(true);


            }
        });


        nightModeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleNightMode();
            }
        });

        invisibleButt.setOnClickListener(new View.OnClickListener() {
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
                    Animation out = new AlphaAnimation(1.0f, 0.0f);
                    out.setDuration(100);
                    defText.setVisibility(View.GONE);
                    defText.setAnimation(out);
                }


                if (!shownFade) {
                    prevButt.setVisibility(View.VISIBLE);
                    prevButt.setAnimation(in);

                    shownFade = true;
                }

                if (((wordList.size()) - 1) > currentIndex)
                    getWordFromList();
                else {
                    if (isAlphabetSelected)
                        currentIndex = 0;
                    else {
                        if (!defChecked) {
                            wordIDList.add(data.getWordID());
                        }
                        getWord();
                    }
                }
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
                if (!shownDef)
                    showDefinition();
                else
                    hideDefinition();

                defChecked = true;

                getDefinition();


            }
        });

    }

    private void getDefinition() {
        String tempDef = data.getDefinitionforWord(mainText.getText().toString());
        defText.setText(tempDef);
    }


    private void toggleNightMode() {
        RelativeLayout lay = (RelativeLayout) findViewById(R.id.container);
        ObjectAnimator colorFade = ObjectAnimator.ofObject(lay, "backgroundColor", new ArgbEvaluator(), Color.argb(211, 211, 211, 211), 0xff000000);
        colorFade.setDuration(400);

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
        TTSObj.speak(mainText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
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
            mainText.startAnimation(translation);
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
            // defText.setText(definition);
            defText.startAnimation(in);
            shownDef = true;
        }

    }


    private void getWordFromList() {

        currentIndex++;
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(300);
        word = wordList.get(currentIndex).toString();
        mainText.setText(word);
        mainText.setAnimation(in);
        shownDef = false;


    }


    private void checkList() {
        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(300);

        if (currentIndex < 1) {
            prevButt.setVisibility(View.GONE);
            prevButt.setAnimation(out);
            shownFade = false;

        }

    }


    private void goBack() {
        currentIndex--;
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(300);
        findViewById(R.id.textView).clearAnimation();
        defText.setText(" ");
        mainText.setText(wordList.get(currentIndex).toString());
        word = mainText.getText().toString();
        mainText.setAnimation(in);
        shownDef = false;
        checkList();

    }


    private void getWord() {


        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(300);


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
        } catch (SQLException sqle) {

            throw sqle;
        }

        wordList.add(word);


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

