package com.example.readertxt;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Locale;

public class MyTTSUtil {
    public TextToSpeech tts = null;
    public String readtxt;
    public CharSequence readcs=null;
    FileReader fr;
    private Context context;
    private String engine;
    private Locale language;
    private float speechPitch, speechVolume, speechRate;
    public final String[] engines = {"", "0", "com.iflytek.speechcloud", "?++"};

    public MyTTSUtil(final Context context) {
        this(context, 1.0f, 1.0f, 1.0f, Locale.ENGLISH, null);
    }

    public MyTTSUtil(final Context context, float speechPitch, float speechVolume, float speechRate, Locale language, String engine) {
        this.context = context;
        this.engine = getEngine(engine);
        this.language = language;
        this.speechPitch = speechPitch;
        this.speechRate = speechRate;
        this.speechVolume = speechVolume;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }

    public void speech(String readtxt) {
        this.readtxt = readtxt;
        if (this.engine.isEmpty()) {
            tts = new TextToSpeech(context, new OnInitTTSListener());
        } else {
            tts = new TextToSpeech(context, new OnInitTTSListener(), this.engine);
        }
    }
    public void speech(CharSequence readCharSequence) {
        this.readcs = readCharSequence;
        if (this.engine.isEmpty()) {
            tts = new TextToSpeech(context, new OnInitTTSListener());
        } else {
            tts = new TextToSpeech(context, new OnInitTTSListener(), this.engine);
        }
    }
    public void speech(FileReader ins) {
        this.fr = ins;
        if (this.engine.isEmpty()) {
            tts = new TextToSpeech(context, new OnInitTTSListener());
        } else {
            tts = new TextToSpeech(context, new OnInitTTSListener(), this.engine);
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public String getEngine(String engine) {
        String te = "";
        if (engine == null || engine.trim().isEmpty()) {
            if (this.engine != null && !this.engine.isEmpty()) {
                return this.engine;
            }
            te = "";
        } else if ("0".equals(engine)) {
            te = "";
        } else {
            boolean isSupport = false;
            for (String i : engines) {
                if (i.equals(engine.trim())) {
                    isSupport = true;
                    te = i;
                    break;
                }
            }
            if (!isSupport) {
                te = "";
            }
        }
        return te;
    }

    class OnInitTTSListener implements TextToSpeech.OnInitListener {
        //public final String[] languages={"", "english", "france","chinese"};
        //protected  Locale language;

//    if (language==null || language.trim().isEmpty() ||"english".equals(language.trim())){
//        this.language=Locale.ENGLISH;
//    }else{
//        this.language=Locale.CHINA;
//    }

        /**
         * @param status 1
         * @reference https://developer.android.google.cn/about/versions/oreo/index.html, https://blog.csdn.net/Sunshine_Cui001/article/details/81173993, app.rar=https://download.csdn.net/download/cyliu5156/7107003?utm_medium=distribute.pc_relevant_t0.none-task-download-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-download-BlogCommendFromMachineLearnPai2-1.channel_param, code=https://www.jianshu.com/p/71b94e841ae2
         */
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                tts.setPitch(speechPitch);
                tts.setSpeechRate(speechRate);
                int thisLang = tts.setLanguage(language);
                if (thisLang == TextToSpeech.LANG_MISSING_DATA || thisLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, language.getDisplayName() + " is failure", Toast.LENGTH_LONG).show();
                }
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, speechVolume + "f");

                if (fr!=null){
                    CharBuffer cb=CharBuffer.allocate(512);
                    tts.setOnUtteranceProgressListener(new Oo());
                    try {
                        int len=512;
                        while (len==512) {
                            len = fr.read(cb);
                            tts.speak(cb.toString(),TextToSpeech.QUEUE_FLUSH,null);
                            tts.isSpeaking();
                        }
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fr=null;
                }else if (readcs!=null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(readcs,tts.QUEUE_FLUSH,null,"10");
                    }
                    readcs=null;
                }else{
                tts.speak(readtxt, tts.QUEUE_FLUSH, params);
                }
            }
        }

    }
    class Oo extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            ;
        }

        @Override
        public void onDone(String utteranceId) {
tts.speak(readtxt,TextToSpeech.QUEUE_FLUSH,null);
        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}

class ReadFileStream{
    private File aim;
    private FileInputStream aims;
    FileReader ins;
    private int fstart,fend,size;
    private ByteBuffer buffer;
    private String charset;
    private String tempFileName, tmpFileNameExtra="$";
    private FileOutputStream tempfile;
    public ReadFileStream(){
    }
    public ReadFileStream(String filepath,String charset,int offset) throws IOException {
        fstart=offset;
        this.charset=charset;
        setAim(new File(filepath));
    }
    public ReadFileStream(File aimFile){
        try {
            setAim(aimFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasNext(){
        return true;
    }
    public String next(){
        String piece="";
        return piece;
    }
    String readPiece() throws IOException {
        char[] temp=new char[1024];
        int avai=1024;


        StringBuffer piece=new StringBuffer();
        while (avai==1024){
        avai=ins.read(temp);
        piece.append(temp,0,avai);
        int end=piece.indexOf("\n\n\n");
        }
        return piece.toString();
    }


    public File getAim() {
        return aim;
}

    public void setAim(File aim) throws IOException {
        if (aims!=null ){
                aims.close();
        }
        this.aim = aim;
        FileOutputStream aimfos=new FileOutputStream(getAim(),true);
        aimfos.close();
        tempFileName=getAim().getName()+tmpFileNameExtra;
        aims=new FileInputStream(getAim());
        new FileReader("");
    }
}


