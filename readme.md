Readertxt-debug-v1.1.0
========================

An Android app to load, search and edit 5500 English words.
@author zorrow2017
@created on 2020/11/1-15:01:00
@reference https://github.com/Zorrow2017/engword


Java File Introduce
------------------------
 * MainActivity.java    
line208-line271 is critical, other lines are old and useless codes.
 * EnglishActivity.java    
Important: as user interface, controller;
 * MyDBengwordUtil.java    
Important: to create table, select one record, insert update, load file data;
 * MyTTSUtil.java    
old and useless codes.

AndroidManifest.xml    
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
thanks.txt    
a small amount of helpers for me, sincerely thank you!

 * GetEngword.java    to crawl English words from a website


Manual
------------------------
see [help.txt](https://github.com/Zorrow2017/engword/blob/master/help.txt)


Chess Blood
------------------------
chessblood is a simple Chinese Chess game, written for jdk1.8. 
you can only play with a person, CANNOT with a computer! 
every chess item has blood and attack atrribute, blood=0 means dead. 
 * name:	"車"	"馬"	"象"	"卒"
 * blood:	100	100	1000	10000
 * attack:	-30	-20	-20	-10

current chessblood version1.0.0, 
Although current version is simple, it can be developed to a fantastic game fellowing [this plan](https://github.com/Zorrow2017/engword/blob/master/chessblood/readme.html)

