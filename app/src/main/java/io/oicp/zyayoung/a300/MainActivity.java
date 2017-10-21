package io.oicp.zyayoung.a300;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public Word[] words = new Word[305];
    public int WordCnt=0, meaningsPerSet = 50;
    Random random = new Random();
    private Meaning[] curLearningSet = new Meaning[200];
    private int[] curLearningState = new int[200];
    private int[] nextLearningPos = new int[200];
    private int totalLearningCnt = 0, curLearningPos, curLearningCnt = 0, lastLearningPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //load();
        try {
            InputStream inStream =  getResources().openRawResource(R.raw.database);
            if (inStream != null)
            {
                InputStreamReader InputReader = new InputStreamReader(inStream);
                BufferedReader BuffReader = new BufferedReader(InputReader);
                String line;
                while (( line = BuffReader.readLine()) != null) {
                    if(line.length()<=0)break;
                    words[WordCnt] = new Word();
                    words[WordCnt].word = line;
                    if(( line = BuffReader.readLine()) != null) {
                        words[WordCnt].meanings = new Meaning[Integer.parseInt(line)];
                        for (int i = Integer.parseInt(line); i > 0; i--) {
                            words[WordCnt].meanings[i-1] = new Meaning();
                            words[WordCnt].meanings[i-1].word = words[WordCnt].word;
                            if ((line = BuffReader.readLine()) != null)
                                words[WordCnt].meanings[i-1].meaning = line;
                            if ((line = BuffReader.readLine()) != null) {
                                words[WordCnt].meanings[i-1].example = new String[Integer.parseInt(line)];
                                for (int j = Integer.parseInt(line); j > 0; j--) {
                                    if ((line = BuffReader.readLine()) != null)
                                        words[WordCnt].meanings[i-1].example[j-1] = line;
                                }
                            }
                        }
                    }
                    WordCnt++;
                }
                inStream.close();
            }
        }
        catch (java.io.FileNotFoundException e)
        {
            Log.d("TestFile", "The File doesn't not exist.");
        }
        catch (IOException e)
        {
            Log.d("TestFile", e.getMessage());
        }

        final TextView MainTextView =(TextView)findViewById(R.id.maintextview);
        final TextView ExampleView =(TextView)findViewById(R.id.exampleview);
        final TextView MeaningView = (TextView)findViewById(R.id.meaningview);
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        final FloatingActionButton yes = (FloatingActionButton) findViewById(R.id.yes);
        final FloatingActionButton no = (FloatingActionButton) findViewById(R.id.no);
        final FloatingActionButton show = (FloatingActionButton) findViewById(R.id.show);
        final Button generate = (Button) findViewById(R.id.generate);
        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        final TextView welcome1 =(TextView)findViewById(R.id.welcome1);
        final TextView welcome2 =(TextView)findViewById(R.id.welcome2);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        //设置np1的当前值
        numberPicker.setValue(meaningsPerSet);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                meaningsPerSet=newVal;
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //curLearningSet
                totalLearningCnt = 0;
                curLearningCnt = 0;
                curLearningPos = 0;
                lastLearningPos = 0;
                boolean[] unAvailable = new boolean[WordCnt];
                int toChoose = random.nextInt(WordCnt);
                for(int i=0;i<meaningsPerSet;){
                    while (unAvailable[toChoose])toChoose = random.nextInt(WordCnt);
                    unAvailable[toChoose]=true;
                    Word chosenWord = words[toChoose];
                    i+=chosenWord.meanings.length;
                    for (int j=0;j<chosenWord.meanings.length;j++){
                        curLearningState[totalLearningCnt]=1;
                        nextLearningPos[totalLearningCnt]=totalLearningCnt-1;
                        curLearningSet[totalLearningCnt++]=chosenWord.meanings[j];
                    }
                }
                curLearningPos= nextLearningPos[0]=totalLearningCnt-1;

                MainTextView.setText(curLearningSet[curLearningPos].word);
                ExampleView.setText(curLearningSet[curLearningPos].example[random.nextInt(curLearningSet[curLearningPos].example.length)]);
                MeaningView.setText(curLearningSet[curLearningPos].meaning);
                progressBar.setMax(totalLearningCnt);
                progressBar.setProgress(curLearningCnt);
                MainTextView.setVisibility(View.VISIBLE);
                ExampleView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                show.setVisibility(View.VISIBLE);
                generate.setVisibility(View.INVISIBLE);
                numberPicker.setVisibility(View.INVISIBLE);
                welcome1.setVisibility(View.INVISIBLE);
                welcome2.setVisibility(View.INVISIBLE);
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeaningView.setVisibility(View.VISIBLE);
                show.setVisibility(View.INVISIBLE);
                yes.setVisibility(View.VISIBLE);
                no.setVisibility(View.VISIBLE);
            }
        });


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curLearningState[curLearningPos]--;
                if (curLearningState[curLearningPos] == 0){
                    curLearningCnt++;
                    progressBar.setProgress(curLearningCnt);
                    if(curLearningCnt>=totalLearningCnt){
                        generate.setVisibility(View.VISIBLE);
                        numberPicker.setVisibility(View.VISIBLE);
                        welcome1.setVisibility(View.VISIBLE);
                        welcome2.setVisibility(View.VISIBLE);
                        yes.setVisibility(View.INVISIBLE);
                        no.setVisibility(View.INVISIBLE);
                        MainTextView.setVisibility(View.INVISIBLE);
                        ExampleView.setVisibility(View.INVISIBLE);
                        MeaningView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        return;
                    }
                    else{
                        nextLearningPos[lastLearningPos]=nextLearningPos[curLearningPos];
                    }
                }
                else {
                    lastLearningPos = curLearningPos;
                }
                curLearningPos = nextLearningPos[curLearningPos];
                MainTextView.setText(curLearningSet[curLearningPos].word);
                ExampleView.setText(curLearningSet[curLearningPos].example[random.nextInt(curLearningSet[curLearningPos].example.length)]);
                MeaningView.setVisibility(View.INVISIBLE);
                MeaningView.setText(curLearningSet[curLearningPos].meaning);
                show.setVisibility(View.VISIBLE);
                yes.setVisibility(View.INVISIBLE);
                no.setVisibility(View.INVISIBLE);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curLearningState[curLearningPos]<2){
                    curLearningState[curLearningPos]++;
                }
                lastLearningPos = curLearningPos;
                curLearningPos = nextLearningPos[curLearningPos];
                MainTextView.setText(curLearningSet[curLearningPos].word);
                ExampleView.setText(curLearningSet[curLearningPos].example[random.nextInt(curLearningSet[curLearningPos].example.length)]);
                MeaningView.setVisibility(View.INVISIBLE);
                MeaningView.setText(curLearningSet[curLearningPos].meaning);
                show.setVisibility(View.VISIBLE);
                yes.setVisibility(View.INVISIBLE);
                no.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toGithub) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("http://github.com/zyayoung/300words");
            intent.setData(content_url);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
