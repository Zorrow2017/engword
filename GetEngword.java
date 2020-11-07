/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package getweb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author zorrow2017
 * @create at 2020-11-1 10:36:01
 * @description GetEngword    require https://www.mvnjar.com/ 
 * okhttp-3.12.1.jar 
 * okio-1.17.3.jar 
 * jsoup-1.13.1.jar
 * JDK1.8 java8
 * Usage: win+r cmd -> d: -> cd d:\program\java -> javac GetEngword.java -> java GetEngword -> \wait patiently
 * if break down halfway, to change (int start=70; or int start=274;), and compile run
 */
public class GetEngword {

    public static void main(String[] args) {
        int op = 0;
        if (op == 0) {
            GetEngword geng = new GetEngword();
            int start=1;
            int resultAmount = geng.get(start);
            return;//resultAmount;
        } else if (op == 1) {
            ;
        }
        return;
    }

    /**
     * to crawl https://www.hujiang.com/
     */
    private int get(int start) {
        int aim = 0;
        int err = 0;

        //related urls
        String baseurl = "https://www.hujiang.com/", inurl = "https://www.hujiang.com/ciku/zuixinkaoyanyingyucihui/", inbaseurl = "https://www.hujiang.com/ciku/zuixinkaoyanyingyucihui_", endurl = "https://www.hujiang.com/ciku/zuixinkaoyanyingyucihui_275";
        int end = 275;

        //page tag
        String charset = "utf-8";
        String[] detailPage = new String[]{"sp-lexicon-word-sound clearfix", "sp-lexicon-word-comment clearfix", "sp-lexicon-word-change clearfix", "sp-lexicon-word-explain clearfix", "sp-lexicon-word-phrase clearfix", "sp-lexicon-word-word clearfix"};  //div[class=""]
        String[] listPage = new String[]{"ul[class=\"sp-rank-content\"]"};  //ul li a href = "/ciku/abdomen/"
        String[] splitor = new String[]{"\t\t", "\n\n"};

        //run work
        String outfile = "D:\\program\\java\\datas\\engword5500.txt";
        try (FileOutputStream fos = new FileOutputStream(outfile, true)) {
            for (int i = start; i <= end; i++) {
                //get 0-20 list 
                byte[] html = GetEngword.okhttp(inbaseurl + i, null, "", "");
                String htmlstr;
                if (html == null) {
                    //log("");
                    continue;
                } else {
                    htmlstr = new String(html, charset);
                }
                Document doc = Jsoup.parse(htmlstr);
                Elements doc_li = doc.selectFirst(listPage[0]).select("li");
                //for every word in the list
                for (int ii = 0; ii < doc_li.size(); ii++) {
                    Thread.sleep(200);
                    aim++;
                    //concat url and word
                    String record = "";
                    String detailurl = doc_li.get(ii).selectFirst("a").attr("href").trim();
                    detailurl = detailurl.replaceAll("^\\/+", "");
                    record += detailurl.split("/")[1] + splitor[0];
                    detailurl = baseurl + detailurl;
                    //get a word's 6 details
                    byte[] html2 = GetEngword.okhttp(detailurl, null, "", "");
                    String html2str;
                    if (html2 == null) {
                        //log("");
                        continue;
                    } else {
                        html2str = new String(html2, charset);
                    }
                    Document doc2 = Jsoup.parse(html2str);
                    for (String iii : detailPage) {
                        Element attr = doc2.selectFirst("div[class=\"" + iii + "\"]");
                        if (attr == null || attr.text() == null) {
                            //System.out.println(iii);
                            err++;
                            record += "null" + splitor[0];
                            continue;
                        }
                        String attrs = attr.text().replaceAll("\\s+", " ");
                        if (attrs.trim().isEmpty()) {
                            err++;
                        }
                        record += attrs + splitor[0];
                    }
                    //write file
                    record += splitor[1];
                    fos.write(record.getBytes("utf-8"));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GetEngword.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetEngword.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GetEngword.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(err);
        return aim;
    }

    public static byte[] okhttp(String url, Map<String, String> headers, String postData, String contentType) throws IOException {
        boolean isPost;
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(2000, TimeUnit.MILLISECONDS)
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(3000, TimeUnit.MILLISECONDS)
                .build();
        Request req;
        if (postData == null || postData.trim().isEmpty()) {
            isPost = false;
            req = new Request.Builder()
                    .url(url)
                    .build();
        } else {
            isPost = true;
            Request.Builder reqb = new Request.Builder();
            if (headers == null) {
                headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18362");
                headers.put("Cookie", "");
            }
            Iterator<String> header = headers.keySet().iterator();
            while (header.hasNext()) {
                String headerKey = header.next();
                reqb.header(headerKey, headers.get(headerKey));
            }
            RequestBody reqbody = RequestBody.create(MediaType.parse(contentType), postData);
            req = reqb.post(reqbody).build();
        }
        Response resp = client.newCall(req).execute();
        if (resp.isSuccessful() && resp.body() != null) {
            //autoDecode = resp.header("Content-Type");
            return resp.body().bytes();
        } else {
            return null;
        }
    }

}
