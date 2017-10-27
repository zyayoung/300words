package io.oicp.zyayoung.a300;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public int WordCnt=0, meaningsPerSet;
    Random random = new Random();
    private String[] curLearningExample = new String[200];
    private String[] curLearningWord = new String[200];
    private String[] curLearningMeaning = new String[200];
    private int[] curLearningState = new int[200];
    private int[] nextLearningPos = new int[200];
    private int totalLearningCnt = 0, curLearningPos, curLearningCnt = 0, lastLearningPos = 0;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

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
        final FloatingActionButton del = (FloatingActionButton) findViewById(R.id.del);
        final FloatingActionButton show = (FloatingActionButton) findViewById(R.id.show);
        final Button generate = (Button) findViewById(R.id.generate);
        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        final TextView welcome1 =(TextView)findViewById(R.id.welcome1);
        final TextView welcome2 =(TextView)findViewById(R.id.welcome2);

        final SharedPreferences prefer=getSharedPreferences("mainactivity", Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editor=prefer.edit();

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        //设置np1的当前值
        meaningsPerSet = prefer.getInt("numberPicker",50);
        numberPicker.setValue(meaningsPerSet);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                meaningsPerSet=newVal;
                editor.putInt("numberPicker",newVal);
                editor.commit();
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalLearningCnt = 0;
                curLearningCnt = 0;
                curLearningPos = 0;
                lastLearningPos = 0;
                boolean[] unAvailable = new boolean[WordCnt];
                int toChoose = random.nextInt(WordCnt);
                for(int failCount = 0;failCount < 500 && totalLearningCnt<meaningsPerSet;failCount++){
                    int tmpTryCount=0;
                    while (tmpTryCount<1000 && unAvailable[toChoose]){
                        tmpTryCount++;
                        toChoose = random.nextInt(WordCnt);
                    }
                    if(tmpTryCount>=1000){
                        break;
                    }
                    unAvailable[toChoose]=true;
                    Word chosenWord = words[toChoose];

                    int meaningsCount = 0;
                    for (int j=0;j<chosenWord.meanings.length;j++){
                        Boolean isEasyWordTmp = true;
                        String[] curExampleSetTmp = chosenWord.meanings[j].example;
                        for (int k=0;k<curExampleSetTmp.length;k++){
                            if(!prefer.getBoolean(curExampleSetTmp[k],false)){
                                isEasyWordTmp = false;
                                break;
                            }
                        }
                        if(!isEasyWordTmp)meaningsCount++;
                    }
                    if (meaningsCount == 0 || meaningsCount > meaningsPerSet - totalLearningCnt + 3 )continue;

                    for (int j=0;j<chosenWord.meanings.length;j++){
                        Boolean isEasyWordTmp = true;
                        String[] curExampleSetTmp = chosenWord.meanings[j].example;
                        for (int k=0;k<curExampleSetTmp.length;k++){
                            if(!prefer.getBoolean(curExampleSetTmp[k],false)){
                                isEasyWordTmp = false;
                                break;
                            }
                        }
                        if(!isEasyWordTmp){
                            int tmpIndex = random.nextInt(curExampleSetTmp.length);
                            while (prefer.getBoolean(curExampleSetTmp[tmpIndex],false))
                                tmpIndex = random.nextInt(curExampleSetTmp.length);
                            curLearningState[totalLearningCnt]=1;
                            nextLearningPos[totalLearningCnt]=totalLearningCnt-1;
                            curLearningWord[totalLearningCnt]=chosenWord.word;
                            curLearningMeaning[totalLearningCnt]=chosenWord.meanings[j].meaning;
                            curLearningExample[totalLearningCnt++]=curExampleSetTmp[tmpIndex];
                            failCount=0;
                        }
                    }
                }
                curLearningPos= nextLearningPos[0]=totalLearningCnt-1;

                MainTextView.setText(curLearningWord[curLearningPos]);
                ExampleView.setText(curLearningExample[curLearningPos]);
                MeaningView.setText(curLearningMeaning[curLearningPos]);
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
                del.setVisibility(View.VISIBLE);
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("是否删除？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putBoolean(curLearningExample[curLearningPos],true);
                                editor.commit();
                                yes.callOnClick();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null);
                alertDialogBuilder.create().show();
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
                        del.setVisibility(View.INVISIBLE);
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
                MainTextView.setText(curLearningWord[curLearningPos]);
                ExampleView.setText(curLearningExample[curLearningPos]);
                MeaningView.setVisibility(View.INVISIBLE);
                MeaningView.setText(curLearningMeaning[curLearningPos]);
                show.setVisibility(View.VISIBLE);
                yes.setVisibility(View.INVISIBLE);
                del.setVisibility(View.INVISIBLE);
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
                MainTextView.setText(curLearningWord[curLearningPos]);
                ExampleView.setText(curLearningExample[curLearningPos]);
                MeaningView.setVisibility(View.INVISIBLE);
                MeaningView.setText(curLearningMeaning[curLearningPos]);
                show.setVisibility(View.VISIBLE);
                yes.setVisibility(View.INVISIBLE);
                no.setVisibility(View.INVISIBLE);
                del.setVisibility(View.INVISIBLE);
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
        else if (id == R.id.reset){
            AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("是否清除？")
                    .setPositiveButton("清除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefer=getSharedPreferences("mainactivity", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor=prefer.edit();
                            editor.clear();
                            editor.putInt("numberPicker",meaningsPerSet);
                            editor.commit();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", null);
            alertDialogBuilder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
