/**
 *
 *
 *  I have written down comments where the code seemed a little complex or written lazily.
 *  Comments aren't too detailed, however, any decent programmer can figure out what is happening.
 *  I've mostly commented above functions or 'if' blocks.
 *  If there is no comment, I consider the code to be self explanatory.
 *
 *
 * */


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
    private boolean shownDef = false, shownFade = false, isNightModeOn = false, defChecked = false, isAlphabetSelected = false;
    private TextToSpeech TTSObj;
    private List wordList = new ArrayList();
    private List smartFilterList = new ArrayList();
    private DataBaseHelper data = new DataBaseHelper(this);
    private ArrayList<String> listSearchWords, listAlphabets;
    private ArrayAdapter<String> searchListAdapter, alphabetListAdapter;
    private SearchView search;
    private ListView searchList, alphabetList;
    private SlidingLayer slidingLayerRight, slidingLayerLeft;
    private RelativeLayout relLay;


    /**
     * What variable does what.
     *
     * I'm going to explain only the ones that aren't straight forward.
     *
     * currentIndex keeps track of what word the user is at in the app, it helps in traversing the WordList used for going back.
     *
     * shownDef changes when the definition is shown or hidden, it helps in Showing or Hiding the definition and not repeating animations.
     *
     * shownFade changes when a new alphabet filter is picked or the WordList has only one word in it, it helps in knowing if the back button is visible or not.
     *
     * defChecked changes when the Definition is shown, it helps in not repeating animations and also deciding what words are picked for the smart-filter.
     *
     * TTSObj is the Text To Speech Object that allows us to use the Pronunciation
     *
     * wordList is a list of words that contains every word that is selected randomly from the database, it helps in going back.
     *
     * smartFilterList is a list of words that are to be marked as "known" in the database so they don't show up again, this is basically the smart filter.
     *
     * listSearchWords is the list of words that populates the ListView (searchList) on the right layer according to the search query.
     *
     * listAlphabets is the list of alphabets that populates the ListView (alphabetList) on the left layer according with alphabets.
     *
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Initialize();
        setActionListeners();
        getWord();
        populateAlphabets();
        setAdapters();


    }

    //There is one ListView on each Sliding Drawer on either side of the screen, this method sets the Adapters for each of the ListViews
    private void setAdapters() {
        searchListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listSearchWords);
        searchList.setAdapter(searchListAdapter);
        searchList.setTextFilterEnabled(true);

        alphabetListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listAlphabets);
        alphabetList.setAdapter(alphabetListAdapter);
        alphabetList.setTextFilterEnabled(true);

        alphabetListAdapter.notifyDataSetChanged();
    }


    private void Initialize() {
        searchList = (ListView) findViewById(R.id.listView);
        alphabetList = (ListView) findViewById(R.id.alphabetlistView);
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
        listSearchWords = new ArrayList<String>();
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

    //This populates the listAlphabets list with the symbol ∅ and all other letters of the Alphabet
    private void populateAlphabets() {
        char temp;

        listAlphabets.add("∅");

        //The loop goes through 65 to 91, converting each number to it's ASCII equivalent and adding it to the list.
        for (int i = 65; i < 91; i++) {
            if (i != 88) {
                temp = (char) i;
                listAlphabets.add(String.valueOf(temp));
            }
        }


    }


    private void setActionListeners() {

        //This closes any sliding layer that is open when you touch something on the main layout.
        relLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slidingLayerLeft.isOpened() || slidingLayerRight.isOpened()) {
                    slidingLayerLeft.closeLayer(true);
                    slidingLayerRight.closeLayer(true);
                }
            }
        });

        //This is the code behind the ListView on the left.
        alphabetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Animation out = new AlphaAnimation(1.0f, 0.0f);
                out.setDuration(300);
                Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(300);
                // If ∅ isn't selected, we will retrieve a list of words starting from the letter that has been selected.
                if (i != 0) {
                    String letter = ((TextView) view).getText().toString();
                    wordList = data.getWordList(letter);
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
                    //The ∅ symbol (Stands for Null set) is the first entry in the list, therefore, if that is selected, all alphabet filters will be removed.
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

        //Clears the ListView on the right, below the SearchView when you tap on the icon.
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setQuery("", false);

            }
        });

        //Opens up the right SlidingDrawer and also focuses on the SearchView, hence opening the keyboard.
        searchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayerRight.openLayer(true);
                search.setIconified(false);

            }
        });


        slidingLayerRight.setOnInteractListener(new SlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {


            }

            @Override
            // On closing the SlidingDrawer, the keyboard is also closed.
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

        //Code behind the SearchView
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            //Every time the search text is changed, a SQL query is passed to the database retrieving a list of words starting from the query that is passed.
            public boolean onQueryTextChange(String s) {
                listSearchWords.clear();
                if (!s.isEmpty()) {
                    listSearchWords.addAll(data.getWordList(search.getQuery().toString()));
                } else {
                    listSearchWords.clear();
                }
                searchListAdapter.notifyDataSetChanged();
                return false;
            }
        });

        //If a word from the ListView below the SearchView is selected, the main TextView in the middle changes to the word selected.
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        //The code behind the pronunciation.
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

        //The code behind the Next Button. This is a little wacky, gets a little crazy, I'll try to explain.
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

                //The below if statement checks if we need a new word or not, if the back button was used, we have to retrieve the words from the WordList.
                if (((wordList.size()) - 1) > currentIndex)
                    getWordFromList();
                else {
                    //The below if statement checks if a specific letter has been selected from the Left Sliding Layer, if yes, we keep the currentIndex at 0 and not get a random word form the database.
                    if (isAlphabetSelected) {
                        currentIndex = 0;
                    } else {
                        //If the definition is not checked, it's added to the WordIDList.
                        if (!defChecked) {
                            smartFilterList.add(data.getWordID());
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
        String tempDef = data.getDefinition(mainText.getText().toString());
        defText.setText(tempDef);
    }

    //Pretty basic, the icons get changed from their Normal versions to the Dark versions and the background fades into Black once you toggle it on, and vice versa.
    private void toggleNightMode() {
        RelativeLayout lay = (RelativeLayout) findViewById(R.id.container);
        ObjectAnimator colorFade = ObjectAnimator.ofObject(lay, "backgroundColor", new ArgbEvaluator(), Color.argb(211, 211, 211, 211), 0xff000000);
        colorFade.setDuration(400);

        if (!isNightModeOn) {
            colorFade.start();
            mainText.setTextColor(Color.LTGRAY);
            defText.setTextColor(Color.LTGRAY);
            prevButt.setBackgroundResource(R.drawable.n_ic_action_prev_item);
            nextButt.setBackgroundResource(R.drawable.n_ic_action_next_item);
            nightModeButt.setBackgroundResource(R.drawable.n_ic_action_brightness_high);
            TTSButt.setBackgroundResource(R.drawable.n_ic_action_volume_on);
            resetButt.setBackgroundResource(R.drawable.n_ic_action_refresh);
            searchButt.setBackgroundResource(R.drawable.n_ic_action_search);

            isNightModeOn = true;
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
            isNightModeOn = false;
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

    //If the user has used the back button, this method is called to retrieve a word from the WordList
    private void getWordFromList() {

        currentIndex++;
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(300);
        word = wordList.get(currentIndex).toString();
        mainText.setText(word);
        mainText.setAnimation(in);
        shownDef = false;


    }

    //Checks if the the user can go back or not, depending on the words in the WordList. If not, then the back button is faded to oblivion.
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

    //Gets a random word from the Database.
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
        data.setWordsAsKnown(smartFilterList);

    }

}

