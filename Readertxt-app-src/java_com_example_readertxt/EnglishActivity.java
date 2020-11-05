package com.example.readertxt;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EnglishActivity extends AppCompatActivity implements View.OnClickListener {
    private MyDBengwordUtil engword;
    private MyTTSUtil tts;
    private volatile boolean editsave; //0:saveOrNot 1:editable
    private volatile String[] word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english);

        //
        tts = new MyTTSUtil(this);
        engword = new MyDBengwordUtil(this);
        String isShowHelp = this.getIntent().getStringExtra("showHelp");//splitter=getString(R.string.eng_activity_splitter);
        if (isShowHelp!=null && isShowHelp.length()>1){
            EditText textw_show = findViewById(R.id.text_showword);
            textw_show.setText("How to use readertxt?");
            TextView textw_desc2 = findViewById(R.id.text_descword2);
            textw_desc2.setText(isShowHelp);
            textw_desc2.setMovementMethod(ScrollingMovementMethod.getInstance());
            return ;
        }
        String[] engdisplay = new String[4]; //{word, id, lang, explainw}
        //Load_word_history_step1_
        engdisplay = engword.read("", 0);
        if (engdisplay == null || engdisplay.length == 0) {
            //Load_word_history_step3_
            engdisplay = getResources().getStringArray(R.array.engact_display);
            engword.write(new String[]{"impossible", "01111110", ""}, "latest recent");
        }
        this.word = engdisplay;
        editsave = true;

        //
        EditText textw_show = findViewById(R.id.text_showword);
        TextView textw_desc2 = findViewById(R.id.text_descword2);
        Button bw_last = findViewById(R.id.button_lastword);
        Button bw_next = findViewById(R.id.button_nextword);
        Button bw_read = findViewById(R.id.button_wordonly);
        Button bw_editsave = findViewById(R.id.button_editsave);
        Button bw_search = findViewById(R.id.button_search);
        SeekBar bw_goto = findViewById(R.id.seekBar_seekword);
        //
        textw_show.setText(engdisplay[0]);
        textw_desc2.setText(engdisplay[3]);
        textw_desc2.setMovementMethod(ScrollingMovementMethod.getInstance());
        bw_goto.setMax(10000);
        bw_last.setOnClickListener(this);
        bw_next.setOnClickListener(this);
        bw_search.setOnClickListener(this);
        bw_read.setOnClickListener(this);
        bw_editsave.setOnClickListener(this);
        bw_goto.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = engword.getSize();
                int position = size * progress / 10000;
                getWord("", position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        EditText show = findViewById(R.id.text_showword);
        switch (vid) {
            case R.id.button_search:
                tts.stop();
                getWord(show.getText().toString(), 0);
                break;
            case R.id.button_lastword:
                tts.stop();
                getWord(show.getText().toString(), -1);
                break;
            case R.id.button_nextword:
                tts.stop();
                getWord(show.getText().toString(), 1);
                break;
            case R.id.button_editsave:
                Button edits = findViewById(R.id.button_editsave);
                EditText desc = findViewById(R.id.text_descword);
                TextView desc2 = findViewById(R.id.text_descword2);
                if (word[0].equals("impossible")) {
                    //do something
                }
                if (editsave) { //edit
                    edits.setText("save");
                    desc.setVisibility(View.VISIBLE);
                    desc2.setVisibility(View.GONE);
                } else {
                    edits.setText("edit");
                    desc.setVisibility(View.GONE);
                    desc2.setVisibility(View.VISIBLE);
                    engword.write(word, desc.getText().toString());
                    desc2.setText(desc.getText());
                }
                editsave = !editsave;
                break;
            case R.id.button_wordonly:
                String shows = show.getText().toString();
                //to read 16 times
                for (int i = 0; i < 4; i++) {
                    shows += ", " + shows;
                }
                tts.speech(shows);
                break;
            default:
                Toast.makeText(EnglishActivity.this, "not support yet! ", Toast.LENGTH_SHORT).show();
        }
    }

    public void getWord(String wordId, int offset) {
        //pre analyze
        if (wordId.trim().matches("\\d+")){
            //select * where id=wordId;
            offset=Integer.parseInt(wordId);
            wordId="";
        }else if (wordId.trim().matches("[Rr]and(om)?\\(\\d+(\\,\\d+)?\\)")){
            //select * where id=a+rand(b-a)
            String[] nums = wordId.split("\\D+");
            int from=1,to=2;
            if (nums.length==1){
                from=1;
                to=Integer.parseInt(nums[0]);
            }else if (nums.length==2){
                from=Integer.parseInt(nums[0]);
                to=Integer.parseInt(nums[1]);
            }
            offset=(int)(Math.random()*(to-from))+from;
            wordId="";
        }
        //
        String[] word = engword.read(wordId, offset);
        if (word == null || word.length == 0) {
            word = new String[]{wordId, "new","en", ""};
        }
        this.word = word;
        //
        EditText textw_show = findViewById(R.id.text_showword);
        Button bw_editsave = findViewById(R.id.button_editsave);
        EditText textw_desc = findViewById(R.id.text_descword);
        TextView textw_desc2 = findViewById(R.id.text_descword2);
        TextView showword2 = findViewById(R.id.text_showword2);
        //
        textw_show.setText(word[0]);
        showword2.setText(word[0] + "  \t" + word[1]);
        textw_desc.setText(word[3]);
        textw_desc2.setText(word[3]);
        bw_editsave.setText("edit");
        textw_desc.setVisibility(View.GONE);
        textw_desc2.setVisibility(View.VISIBLE);
        editsave = true;
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && word!=null && word.length>1) {
            engword.saveLatest(word[0]);
        }
        super.onDestroy();
        tts.stop();
        engword.close();
    }
}
