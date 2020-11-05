package com.example.readertxt;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

/**
 * @author zorrow2017
 * @created on 2020/11/1-15:01:00
 * @description my sqlite database util
 * @since v1.0.1
 */
public class MyDBengwordUtil extends SQLiteOpenHelper {
    //private Connection conn;
    private Context context;

    public MyDBengwordUtil(Context context) {
        this(context, getDBName(), 1);
    }

    public MyDBengwordUtil(Context context, String name, int version) {
        super(context, name, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String engwordSql = "create table if not exists engword(id integer primary key autoincrement," +
                "word char(128) not null,lang char(8) default 'en',explainw nvarchar(4000));";
        db.execSQL(engwordSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * @return String database name "school.db3"
     */
    public static final String getDBName() {
        return "school.db3";
    }

    /**
     * @param keyword an attribute of engword
     * @param offset  select the word near keyword, prev=-1 cur=0 next=1
     * @return new String[4]{word, id, lang, explainw}
     */
    public String[] read(String keyword, int offset) {
        String[] aim = new String[4];
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\'", "\'\'").toLowerCase();
        }
        //mode  judge different select mode according keyword
        int mode = 0;
        String query;
        //
        if (keyword.isEmpty()) {
            //means select * where id=offset;
            int position = offset;
            if (offset <= 0) {
                position = 0;
            }
            query = "select id,word,lang,explainw from engword where id=" + position + ";";
            if (offset == 0) {
                //Load_word_history_step2_
                String[] word0 = read(keyword, -1);
                if (word0 == null) {
                    //no history, then see Load_word_history_step4_ at EnglishActivity
                    return null;
                }
                String[] res = read(word0[0], 0);
                //then see Load_word_history_step3_ at EnglishActivity
                return res;
            }
        } else if (offset != 0) {
            //means select the word before or after current keyword
            int id = getSizeOrId(keyword, 1);
            int size = getSize();
            id = (id + offset + size) % size;
            if (id <= 0) {
                id = (offset > 0) ? 1 : size - 1;
            }
            query = String.format("select id,word,lang,explainw from engword where id=%d;", id);
        } else {
            //means  select * where word=keyword or word like keyword
            query = String.format("select id,word,lang,explainw from engword where word='%s' and id!=0;", keyword);
//judge different according keyword
//            if (keyword.matches("\\d+")) {
//                int id=Integer.parseInt(keyword);
//                query="id="+id+";";
//                mode = 0;
//            } else if (keyword.matches("[Rr]and(om)?\\(\d+\\)")) {
//                query=String.format("id=%d;", (int)(Math.random()*int+1));
//                mode = 1;
//            }else if (keyword.matches("[\\w\\d\\-\']+")) {
//                query=String.format("word='%s' and id!=0;", keyword);
//                mode = 1;
//            } else if (keyword.matches("[\\w\\d\\-\'\\%_\\[\\]]+")) {
//                //'A_ple Win%s [GC]oogle [D-O]racle IB[^M]'
//                query = "select id,word,lang,explainw from engword where id!=0 and name like '" + keyword + "';";
//                mode = 2;
//                return aim;
//            } else {
//                query="";
//                mode = 3;
//            }
        }
        //
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cusor = db.rawQuery(query, null);
        if (cusor == null || cusor.moveToNext() == false) {
            return null;
        }
        aim[0] = cusor.getString(cusor.getColumnIndex("word"));
        aim[1] = "" + cusor.getInt(cusor.getColumnIndex("id"));
        aim[2] = cusor.getString(cusor.getColumnIndex("lang"));
        aim[3] = cusor.getString(cusor.getColumnIndex("explainw"));
        return aim;
    }

    public boolean write(String[] word, String desc) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean aim = true;
        if (word.length > 1 && word[1].equals("01111110")) {
            //if there is no record whose id=0, then insert one
            String[] res = read("", -1);
            if (res == null) {
                String insert = String.format("insert into engword(id,word,lang,explainw) values(0,'%s','%s','%s');", word[0], "en", desc);
                db.execSQL(insert);
            }
            return aim;
        }
        //
        word[0] = word[0].trim().replaceAll("\'", "\'\'").toLowerCase();
        desc = desc.replaceAll("\'", "\'\'"); //.replaceAll("[\\s]+","  ");
        int id = getSizeOrId(word[0], 1);
        if (id <= 0) {
            String insert = String.format("insert into engword(word,lang,explainw) values('%s','%s','%s');",
                    word[0], "en", desc);
            db.execSQL(insert);
        } else {
            String update = String.format("update engword set explainw='%s' where id=%d;", desc, id);
            db.execSQL(update);
        }
        return aim;
    }

    public int getSize() {
        return getSizeOrId("select count(*) as size from engword;", 0);
    }

    /**
     * @param word can be a sql query clause which return an int only, or a keyword
     * @param op   0:getSize(query), 1:getId(keyword)
     * @return int  -1 if null, aimed int of query, or id of keyword
     */
    public int getSizeOrId(String word, int op) {
        String query;
        if (op > 0) {
            query = String.format("select id as size from engword where word='%s' and id!=0;", word);
        } else {
            query = word;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cusor = db.rawQuery(query, null);
        if (cusor == null || cusor.moveToNext() == false) {
            return -1;
        }
        return cusor.getInt(cusor.getColumnIndex("size"));
    }

    public void saveLatest(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update  engword set word='" + word.trim().replaceAll("\'", "\'\'") + "' where id=0;");
        db.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean loadFile(String filename) {
        boolean isExists = false;
        File[] paths = context.getExternalFilesDirs(Environment.MEDIA_MOUNTED);
        for (File path : paths) {
            //search all root directories to find file engword5500.txt;
            String pathname = path.getAbsolutePath();
            int slice = pathname.indexOf("ndroid") - 1; //likes  /storage/emulated/0/Android , /storage/0403-0201/Android
            pathname = pathname.substring(0, slice) + filename; //file="engword5500.txt";
            String isFileAppend = context.getString(R.string.eng_activity_engword3);

            //to read file and insert into db
            String insert;
            try {
                FileInputStream fis = new FileInputStream(pathname);
                InputStreamReader isr = new InputStreamReader(fis, "utf-8");
                BufferedReader instream = new BufferedReader(isr);
                isExists = true;
                SQLiteDatabase db = this.getReadableDatabase();
                if (!pathname.equals(isFileAppend)) { // && db.rawQuery("if exists table engword;",null)){
                    //to delete old data
                    db.execSQL("drop table if exists engword;");
                    String engwordSql = "create table if not exists engword(id integer primary key autoincrement," +
                            "word char(128) not null,lang char(8) default 'en',explainw nvarchar(4000));";
                    db.execSQL(engwordSql);
                    insert = String.format("insert into engword(id,word,lang,explainw) values(0,'%s','%s','%s');", "impossible", "en", "");
                    db.execSQL(insert);
                }
                //
                String record = instream.readLine();
                while (record != null) {
                    if (record.trim().isEmpty()) {
                        record = instream.readLine();
                        continue;
                    }
                    String[] records = record.trim().split("\t+");
                    records[0] = records[0].trim().replaceAll("\'", "\'\'").toLowerCase();
                    StringBuffer desc = new StringBuffer();
                    for (int i = 1; i < records.length; i++) {
                        desc.append("\n" + records[i]);
                    }
                    insert = String.format("insert into engword(word,lang,explainw) values('%s','%s','%s');",
                            records[0], "en", desc.toString().replaceAll("\'", "\'\'"));
                    db.execSQL(insert);
                    record = instream.readLine();
                }
                db.close();
                instream.close();
                isr.close();
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return isExists;
    }

//    /**
//     * Since reading file and inserting into db3.engword one by one is very slow, we can copy an existed .db3 file and paste to a new device.
//     * file name: school.db3;
//     * Android -> Tomcat(Computer) -> Android Studio(Computer) -> apk -> Android(your device);
//     * However, this method is NOT CORRECT, what a pity;
//     * */
//    public static final void uploadDB3File(){
//        String path="/data/data/com.example.readertxt/databases/school.db3";
//        String server="http://10.70.80.99:8080/yylj/jsp/fileup.jsp";
////
//        try {
//            URL url=new URL(server);
//            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            conn.setUseCaches(false);
//            String boundary="*****";
//            String splitter="--*****\r\n";
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.setRequestProperty("Charset", "UTF-8");
//            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" +  boundary);
//            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//            dos.writeBytes(splitter);
//            dos.writeBytes("Content-Disposition: form-data; name=\"file1\";filename=\"" + "fngword.txt" + "\"" + "\r\n");
//            dos.writeBytes("\r\n");
//            int block_size=1024;
//            byte[] bytes=new byte[block_size];
//            FileInputStream fis=new FileInputStream(path);
//            int vals=fis.read(bytes);
//            while (vals>0){
//                dos.write(bytes,0,vals);
//                vals=fis.read(bytes);
//            }
//fis.close();
//            dos.writeBytes("\r\n");
//            dos.writeBytes(splitter);
//            dos.flush();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    public final boolean pasteDB3File(){
//        boolean res=false;
//        String path="/data/data/com.example.readertxt/databases/school.db3";
//        String used=context.getString(R.string.eng_activity_split); //"01111110"
//        try{
//            InputStream instream = context.getResources().openRawResource(R.raw.useOnce);
//            //http error,can't write useOnce, max 1024KB, give up
//            instream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return res;
//    }

}
