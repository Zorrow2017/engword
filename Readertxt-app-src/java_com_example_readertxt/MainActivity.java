package com.example.readertxt;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    MyTTSUtil textToSpeech;
    final int MYINT_OPEN_FILE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to define some const
        final String pathss = fileTest();
        final String ttscnEngine = "com.iflytek.speechcloud"; //com.iflytek.speechsuite
        final TextView maintext_outputDebug = findViewById(R.id.text_outputdebug);
        Button mb_sysExit0 = findViewById(R.id.button_sysExit0);
        Button mb_readText = findViewById(R.id.button_readtext);
        Button mb_printLog = findViewById(R.id.button_printlog);
        Button mb_read = findViewById(R.id.button_read);
        Button mb_readCN = findViewById(R.id.button_readcn);
        Button mb_stop = findViewById(R.id.button_stop);
        final Switch mb_isMultiline = findViewById(R.id.button_isMultiline);
        final Spinner mainspinner_menu = findViewById(R.id.spinner_menu);
        //to bind list data
        RecyclerView mainrecycler_bookList = findViewById(R.id.recycler_booklist);
        BookRecyclerAdapter bookRecyclerAdapter = new BookRecyclerAdapter(this, null);
        bookRecyclerAdapter.manage(mainrecycler_bookList);

        //to bind onClickListener for buttons:
        mb_sysExit0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("readertxt_mainDebug", "onClick: toast error");
                Toast.makeText(MainActivity.this, "okk", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        });
        mb_readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aimf = "/storage/emulated/0/DCIM/Camera/IMG_20200831_030057.jpg";
                StringBuffer datas = new StringBuffer();
                String mylogrun = getExternalFilesDir("").getAbsolutePath() + "/mylogrun.txt";
                Log.d("readertxt_mainDebug", "getExternalFilesDir=" + mylogrun);
                String fils = "\nmylogat getExternalFilesDir: " + (new Date()).toGMTString() + Math.random() * 100;
                try {
                    FileOutputStream filsout = new FileOutputStream(mylogrun, true);
                    filsout.write(fils.getBytes("utf-8"));
                    filsout.close();//A
                } catch (IOException e) {
                    datas.append("write wrong\n");
                    e.printStackTrace();
                }
                String randomFileStorage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                try {
                    getStorageWrite();
                    String f1 = randomFileStorage + "new_6", f2 = randomFileStorage + "new_26.0";
                    randomFileStorage += "new_test.puk"; //+(int)Math.floor(Math.random()*100);
                    FileOutputStream filsout = new FileOutputStream(randomFileStorage);
                    filsout.write(("random is test puk= " + Math.random() + "\n hw.").getBytes("utf-8"));
                    filsout.close();

                    byte[] bys = new byte[512];
                    FileInputStream f2in = new FileInputStream(f2);
                    int byslen = f2in.read(bys);
                    f2in.close();
                    datas.append("\n" + new String(bys, 0, byslen));
                    //Log.d("readertxt_mainDebug","random= "+randomFileStorage);
                } catch (IOException e) {
                    Log.d("readertxt_mainDebug", "write failure = " + randomFileStorage);
                    datas.append("write wrong\n");
                    e.printStackTrace();
                }

//                try {
//                    FileInputStream inputStream = new FileInputStream(aimf);
//                    byte[] bys = new byte[512];
//                    int reads;
//                    while (true) {
//                        reads = inputStream.read(bys);
//                        if (reads <= 0) {
//                            break;
//                        }
//                        datas.append(bys[0] + " + " + bys[1]);
//                        //datas.append(new String(bys,0,reads,"utf-8"));
//                    }
//                    inputStream.close();
//                } catch (FileNotFoundException e) {
//                    datas.append("read no file");
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    datas.append("read io problem");
//                    e.printStackTrace();
//                }
                datas.append("\n" + pathss);
                maintext_outputDebug.setText(datas);
            }
        });
        mb_printLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mylogrun = getExternalFilesDir("").getAbsolutePath() + "/mylogrun.txt";
                char[] mylog = new char[2048];
                int mylen = 0;
                try {
                    FileReader fileReader = new FileReader(new File(mylogrun));
                    mylen = fileReader.read(mylog);
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mylen == 0) {
                    mylog = "read null".toCharArray();
                }
                EditText et = findViewById(R.id.text_inputread);
                et.setText(mylog, 0, mylen);
            }
        });
        mb_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.text_inputread);
                final String readtxt = et.getText().toString();
                if (textToSpeech == null || !textToSpeech.getEngine("").isEmpty()) {
                    textToSpeech = new MyTTSUtil(MainActivity.this);
                } else {
                    textToSpeech.stop();
                }
                textToSpeech.speech(readtxt);
            }
        });
        mb_readCN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.text_inputread);
                final String readtxt = et.getText().toString();
                if (textToSpeech == null || !textToSpeech.getEngine("").equals(ttscnEngine)) {
                    textToSpeech = new MyTTSUtil(MainActivity.this, 1.0f, 1.0f, 1.0f, Locale.CHINA, ttscnEngine);
                } else {
                    textToSpeech.stop();
                }
                textToSpeech.speech(readtxt);
            }
        });
        mb_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            }
        });
        mb_isMultiline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maintext_outputDebug.setSingleLine(!isChecked);
            }
        });
        mainspinner_menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] mainMenus = getResources().getStringArray(R.array.mainGlobalMenu);
                switch (mainMenus[position]) {
                    case "help":
                        //MyDBengwordUtil.uploadDB3File();
                        openBookText("");
                        break;
                    case "open...":
                        Intent openfile = new Intent();
//                        openfile.setAction(Intent.ACTION_GET_CONTENT);
//                        openfile.setType("*/*");
//                        openfile.addCategory(Intent.CATEGORY_OPENABLE);
//                        startActivityForResult(openfile,OPEN_FILE);
                        if (Build.VERSION.SDK_INT < 19) {
                            openfile = new Intent();
                            openfile.setAction(Intent.ACTION_GET_CONTENT);
                            openfile.setType("*/*");
                            startActivityForResult(openfile, MYINT_OPEN_FILE);
                        } else {
                            openfile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            openfile.addCategory(Intent.CATEGORY_OPENABLE);
                            openfile.setType("*/*");
                            startActivityForResult(openfile, MYINT_OPEN_FILE);
                        }
                        break;
                    case "setting":
                        MyDBengwordUtil db = new MyDBengwordUtil(MainActivity.this);
//                        if (db.pasteDB3File()){
//                            //if there is a school.db3 in apk file, use it;
//                            //else, try to load engword5500.txt at root storage directory;
//                            break;
//                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            //MainActivity.this.onActivityResult22(-112358, -111, null);
                            String filename=getString(R.string.eng_activity_engword2);
                            boolean res=db.loadFile(filename); //"engword5500.txt"
                            if (res==false){
                                Toast.makeText(MainActivity.this, filename+" not find in root directory! ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "res: android4.4 lowwwww", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                }
                try {
                //the follow three clause ensure spinner keep active all the time
                    Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(mainspinner_menu, AdapterView.INVALID_POSITION);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

//Android版本号	SDK版本号	版本号名称
//9	28	Build.VERSION_CODES.P
//8.1	27	Build.VERSION_CODES.O_MR1
//8.0	26	Build.VERSION_CODES.O
//7.1	25	 Build.VERSION_CODES.N_MR1
//7.0	24	Build.VERSION_CODES.N
//6.0	23	 Build.VERSION_CODES.M
//5.1	22	 Build.VERSION_CODES.LOLLIPOP_MR1
//5.0	21	Build.VERSION_CODES.LOLLIPOP
//4.4	19	 Build.VERSION_CODES.KITKAT
//4.3	18	 Build.VERSION_CODES.JELLY_BEAN_MR2
//4.2.x	17	 Build.VERSION_CODES.JELLY_BEAN_MR1
//4.1.x	16	Build.VERSION_CODES.JELLY_BEAN
//4.0.3 -	4.0.4	15  Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
//2.3.3 -	2.3.7   10	Build.VERSION_CODES.GINGERBREAD_MR1	null
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        onActivityResult22(requestCode,resultCode,data);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected final void onActivityResult22(int requestCode, int resultCode, @Nullable Intent data) {
        String filepathname = null;
        boolean passs = false;
        if (requestCode == -112358 && requestCode == -111) {
            filepathname = getString(R.string.eng_activity_engword3); //"ADDengword5500.txt"
            passs = true;
            //requestCode = MYINT_OPEN_FILE;
        }
        ;
        if (requestCode == MYINT_OPEN_FILE || requestCode==-112358) {
            //fileProvider=https://blog.csdn.net/banyinlve3147/article/details/102012956/, url2file=https://blog.csdn.net/hust_twj/article/details/76665294?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param
            //requires android.permission.MANAGE_DOCUMENTS, or grantUriPermission()
            String filepath = "";
            if (!passs) {
                if (data == null || data.getDataString() == null) {
                    return;
                }
                filepath = Uri.decode(data.getDataString());
            } else {
                filepath = filepathname;
            }
            EditText et0 = MainActivity.this.findViewById(R.id.text_inputread);
            et0.setText(filepath);
            /*String uripath=filepath;
            //filepath=FileUtils.getFilePathByUri(this, Uri.parse(filepath));
String [] proj=new String[]{MediaStore.Images.Media.DATA};
            //CursorLoader cursorLoader=new CursorLoader(getApplicationContext(),Uri.parse(filepath),proj,null,null,null);
            //Cursor cursor=cursorLoader.loadInBackground();
            ContentResolver resv = this.getContentResolver();
            Uri uri_filepath=Uri.parse(uripath);
            Cursor cursor=resv.query(uri_filepath,null,null,null,null);
            //int pos=cursor.getColumnIndexOrThrow(proj[0]);
            if (cursor==null){
                Log.d("readertxt_mainDebug", "Null =: "+filepath, null);
                Log.d("readertxt_mainDebug", "/storage/emulated/0/Android/data/com.example.readertxt/files/mylogrun.txt"+" =: "+Uri.parse("/storage/emulated/0/Android/data/com.example.readertxt/files/mylogrun.txt").getPath(), null);
                return ;
            }
            if (cursor.moveToFirst()){
                filepath=cursor.getString(cursor.getColumnIndexOrThrow(proj[0]));
            }
            //cursor.moveToFirst();
            //filepath=cursor.getString(pos);
            cursor.close();
            Log.d("readertxt_mainDebug", uripath+" =: "+filepath, null);*/
            Log.d("readertxt_mainDebug", " =: <" + filepath + "/>", null);
            ///*
            //used for engword
//            String[] engsources = filepath.split("[\\/:]");
//            String engsource = engsources[engsources.length - 1];
//            String engtxt = getString(R.string.eng_activity_engword); //engword(5500)?\\.txt
//            String engtxt3 = getString(R.string.eng_activity_engword3);
//            String numberw = getString(R.string.eng_activity_testtext);
//            if (!engsource.matches(engtxt)) {
//                //Toast.makeText(MainActivity.this, "file name must be engword5500.txt! ", Toast.LENGTH_SHORT).show();
//                //return ;
//                engsource = engtxt3;
//            }
            MyDBengwordUtil db0 = new MyDBengwordUtil(MainActivity.this);
            try{
            db0.loadFile(filepath);
            return ;
            }catch (Exception ex){
                ex.printStackTrace();
                //throw new Exception(ex);
            }

            File[] engpaths = getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            engpaths=new File[0];
            //
            //Environment.getExternalStorageDirectory().getAbsolutePath();
            for (File path : engpaths) {
                String pathname = path.getAbsolutePath();
                int slice = pathname.indexOf("ndroid") - 1;
                pathname = pathname.substring(0, slice) + filepath;
//                if (pathname.endsWith(File.separator)) {
//                    pathname += engsource;
//                } else {
//                    pathname += File.separator + engsource;
//                }
                try  {
                    FileInputStream fis = new FileInputStream(pathname); InputStreamReader isr = new InputStreamReader(fis, "utf-8"); BufferedReader instream = new BufferedReader(isr);
                    et0.append(pathname);
                    MyDBengwordUtil db = new MyDBengwordUtil(MainActivity.this);
                    String record = instream.readLine();
                    //Toast.makeText(MainActivity.this, "res: " + record, Toast.LENGTH_SHORT).show();
                    db.getWritableDatabase().execSQL("delete from engword;");
                    while (record != null) {
                        //db.write(record.split("\t+"),"");
                        if (record.trim().isEmpty()) {
                            record = instream.readLine();
                            continue;
                        }
                        String[] recres = record.trim().split("\t+");
                        StringBuffer desc = new StringBuffer();
                        for (int i = 1; i < recres.length; i++) {
                            desc.append("\n" + recres[i]);
                        }
                        db.write(recres, desc.toString());
                        record = instream.readLine();
                    }
                    db.close();
                    instream.close();
                    isr.close();
                    fis.close();
                    return;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
//            try (FileOutputStream fos = new FileOutputStream("/storage/emulated/0/engword5500.txt")) {
//                fos.write(("null\t\ten\t\t asd of nothing\nnumber\t\t"+numberw).getBytes("utf-8"));
//                return ;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //\u0075sed
            //*/

            filepath = UriTofilePath.getFilePathByUri(this, Uri.parse(filepath));
            try {
                FileInputStream fi = new FileInputStream(filepath);
                byte[] b = new byte[16];
                fi.read(b);
                filepath += "\nis" + b[5] + "," + b[10];
                fi.close();
            } catch (FileNotFoundException e) {
                filepath += "\nno file";
                e.printStackTrace();
            } catch (IOException e) {
                filepath += "\nioex";
                e.printStackTrace();
            }
            EditText et = findViewById(R.id.text_inputread);
            et.setText(filepath);
        }
    }

    protected void getStorageWrite() {
        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        int isPermitted = ActivityCompat.checkSelfPermission(this, permission[0]);
        if (isPermitted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 1);
        }
    }

    protected void openBookText(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            Intent learneng = new Intent(MainActivity.this, EnglishActivity.class);
            String helpText="manual: \n";
            try{
                InputStream instream = getResources().openRawResource(R.raw.help);
                byte[] helps=new byte[1024*100*4]; //0.4M
                int vals=instream.read(helps);
                helpText=new String(helps,0,vals,"utf-8");
                instream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            learneng.putExtra("filepath", "showHelp");
            learneng.putExtra("showHelp", helpText);
            startActivity(learneng);
            return;
        } else if ("english".equals(filepath)) {
            Intent learneng = new Intent(MainActivity.this, EnglishActivity.class);
            File eword = new File("/storage/emulated/0/mybook/EnglishWords.ewx");
            learneng.putExtra("filepath", "/storage/emulated/0/mybook/EnglishWords.ewx");
            startActivity(learneng);
            return;
        }
        Intent gotoTheBook = new Intent(MainActivity.this, BookTextActivity.class);
        gotoTheBook.putExtra("filepath", filepath);
        startActivity(gotoTheBook);
        //startActivityForResult(gotoTheBook,my_int_CODE);
    }

    /**
     * @return debug info
     * @reference https://blog.csdn.net/csdn_aiyang/article/details/80665185
     * @reference https://blog.csdn.net/ruancoder/article/details/54290807, https://www.cnblogs.com/xiobai/p/10839494.html
     * @since 2020年08月31日-20:21:42
     */
    private String fileTest() {
        ///storage/emulated/0/Android/data/com.example.readertxt/files/mylogrun.txt, /storage/emulated/0/new_test.puk
        //        JsonReader dt=new JsonReader(new FileReader("d:\\data.json"));
        String write = "";
        //        1、Environment.getDataDirectory() = /data
//                这个方法是获取内部存储的根路径
//        2、getFilesDir().getAbsolutePath() = /data/user/0/packname/files
//        这个方法是获取某个应用在内部存储中的files路径
//        3、getCacheDir().getAbsolutePath() = /data/user/0/packname/cache
//        这个方法是获取某个应用在内部存储中的cache路径
//        4、getDir(“myFile”, MODE_PRIVATE).getAbsolutePath() = /data/user/0/packname/app_myFile
//        这个方法是获取某个应用在内部存储中的自定义路径
//        方法2,3,4的路径中都带有包名，说明他们是属于某个应用
//…………………………………………………………………………………………
//        5、Environment.getExternalStorageDirectory().getAbsolutePath() = /storage/emulated/0
//        这个方法是获取外部存储的根路径
//        6、Environment.getExternalStoragePublicDirectory(“”).getAbsolutePath() = /storage/emulated/0
//        这个方法是获取外部存储的根路径
//        7、getExternalFilesDir(“”).getAbsolutePath() = /storage/emulated/0/Android/data/packname/files
//        这个方法是获取某个应用在外部存储中的files路径
//        8、getExternalCacheDir().getAbsolutePath() = /storage/emulated/0/Android/data/packname/cache
//        这个方法是获取某个应用在外部存储中的cache路径
        String paths = "0";
        File environmentGetDataDirectory = Environment.getDataDirectory();
        final File getFilesDir = getFilesDir();
        File getCacheDir = getCacheDir();
        //File getDataDir=getDataDir();
        File getExternalCacheDir = getExternalCacheDir();
        final File filsd = getExternalFilesDir("");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File[] getExternalFilesDirs = getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            for (File fi : getExternalFilesDirs) {
                paths += fi.getAbsolutePath() + ",12\n";
            }
        }
        write = String.format("environmentGetDataDirectory = %s\ngetFilesDir = %s\n getCacheDir = %s\ngetExternalCacheDir = %s\ngetExternalFilesDirs = [%s]\n", environmentGetDataDirectory.getAbsolutePath(), getFilesDir.getAbsolutePath(), getCacheDir.getAbsolutePath(), getExternalCacheDir.getAbsolutePath(), paths);
        return write;
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onDestroy();
    }
}

/**
 * @reference StaggeredGridLayoutManager=https://www.jianshu.com/p/4f9591291365
 */
class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.BookHolder> {
    private List<BookItem> bookData;
    private Context context;

    public BookRecyclerAdapter() {
        fillData();
    }

    public BookRecyclerAdapter(Context context, List<BookItem> bookData) {
        this.context = context;
        if (bookData == null) {
            this.bookData = new ArrayList<>();
            fillData();
        } else {
            this.bookData = bookData;
        }
    }

    public void manage(RecyclerView recyclerView) {
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(this);
    }

    public void fillData() {
        bookData = new ArrayList<>();
        BookItem learnEng = new BookItem("english");
        learnEng.bookuri = "english";
        bookData.add(learnEng);
        bookData.add(new BookItem("tlbb"));
        bookData.add(new BookItem("let's go go 123"));
        bookData.add(new BookItem("tlbb8"));
        bookData.add(new BookItem("sec"));
        for (int i = 0; i < 1; i++) {
            bookData.add(new BookItem(String.format("is=%x$", (long) (Math.random() * (1 << 30)))));
        }
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.books_recycler_table_item, viewGroup, false);
        return new BookHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull BookHolder bookHolder, int i) {
        final int booki = i;
        final BookItem bookitem = bookData.get(booki);
        bookHolder.bookName.setText(bookitem.bookname);
        bookHolder.bookUri.setText(bookitem.bookuri);
        bookHolder.delbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookData.remove(booki);
                BookRecyclerAdapter.this.notifyItemRemoved(booki);
            }
        });
        bookHolder.bookwrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openBookText(bookitem.bookuri);
            }
        });
    }


    @Override
    public int getItemCount() {
        return bookData.size();
    }

    class BookHolder extends RecyclerView.ViewHolder {
        TextView bookName;
        TextView bookUri;
        Button delbook;
        LinearLayout bookwrap;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            bookName = itemView.findViewById(R.id.recycler_bookname);
            bookUri = itemView.findViewById(R.id.recycler_bookuri);
            delbook = itemView.findViewById(R.id.button_delbook);
            bookwrap = itemView.findViewById(R.id.recycler_bookwrap);
        }
    }

    class BookItem {
        public String bookname;
        public String bookuri = "content://e1111";
        public int nothing;

        public BookItem(String bookname) {
            this.bookname = bookname;
            bookuri = bookname;
        }
    }
}

final class FileUtils {

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }
        //adb=https://blog.csdn.net/yulle/article/details/79568828, simple adb=https://www.cnblogs.com/wx2017/p/10883189.html
        //cd C:\Users\zorrow2017\AppData\Local\Android\Sdk\platform-tools\
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}

class UriTofilePath {
    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        } else {
            // 以 file:// 开头的
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                path = uri.getPath();
                return path;
            }
            // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex);
                        }
                    }
                    cursor.close();
                }
                return path;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

