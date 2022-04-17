package com.example.pozharka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;



import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public LocationManager lm;
    public WifiManager wifiManager;
    public adap ad;

    public ImageView leg;
    public ImageView iv;
    public TextView tq;
    public ListView lw;

    public Thread potok;

    public  ArrayList<Item> egug = new ArrayList<>();
    public  List<ScanResult> results;
    public  ArrayList<String> bss = new ArrayList<>();
    public  ArrayList<String> cabs = new ArrayList<>();
    public  String[] base = new String[]{" "};
    public  ArrayList<Integer> numsl1 = new ArrayList<>();
    public  ArrayList<Integer> numsl2 = new ArrayList<>();

    public int maxlev1 = -198;
    public String cab = " ";

    public boolean F = false;
    public boolean geoloc = false;
    public boolean gps = false;
    public boolean nw = false;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    public String currentlayout = "main";

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ad = new adap(this, egug);

        tq = findViewById(R.id.tquest);
        leg = findViewById(R.id.legend);
        iv = findViewById(R.id.iv);
        lw = findViewById(R.id.wifilist);

        potok = new Thread(r, "scaner");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Полноэкранный режим
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //Инициализация ттс
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale(Locale.getDefault().getLanguage()));

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Failed");
                    } else {
                        Log.e("TTS", "Failed");
                    }
                    } else {
                    Log.e("TTS", "Failed");
                }
            }
        });

        //Создание файла-базы (по-хорошему поменять бы способ создания)
        FileOutputStream fos = null;
        try {
            String text =
                    "00:f6:63:d7:31: 206\n" +
                    "cc:16:7e:aa:f1: 208\n" +
                    "00:81:c4:73:bf: 209\n" +
                    "2c:33:11:f8:86: 210\n" +
                    "cc:16:7e:96:cb: 211\n" +
                    "00:f6:63:97:19: 212\n" +
                    "b8:11:4b:93:4a: 215\n" +
                    "c0:c9:e3:08:bc: дом\n" +
                    "c8:d3:ff:59:05: принтер\n" +
                    "e2:aa:d4:15:08: Выход\n" +
                    "62:1e:28:a2:60: А-321";


            fos = openFileOutput("bssids.txt", MODE_PRIVATE);
            fos.write(text.getBytes());
        } catch (IOException ex) {}
        finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {}
        }

        //Разделение базы на 2 списка bss и cabs
        FileInputStream fin = null;
        try {
            fin = openFileInput("bssids.txt");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String text1 = new String(bytes);
            String[] base = text1.split("\n");

            for (String s : base) {
                bss.add(s.split(" ")[0]);
                cabs.add(s.split(" ")[1]);
            }
        } catch (IOException ex) {}
        finally {
            try {
                if (fin != null)
                    fin.close();
            } catch (IOException ex) {}
        }

        //Запрос разрешения
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        else {
            //Включение геолокации
            gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            geoloc = gps && nw;
            if (! geoloc){
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            potok.start();
        }
    }

    public void start(View view) {
        if (!cab.equals(" ")) {
            setContentView(R.layout.questions);
            currentlayout = "quest";

            tq=findViewById(R.id.tquest);
            tq.setText("Вы находитесь в кабинете " + cab + "?");
            tts.speak("Вы находитесь в кабинете " + cab + "?", TextToSpeech.QUEUE_FLUSH, null);

            questact();
        }
    }

    public void reset(View view) {

        leg = findViewById(R.id.legend);
        iv = findViewById(R.id.iv);

        iv.setImageResource(R.drawable.logo);
        leg.setImageResource(R.drawable.logo);
        tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null);
        F = false;
    }

    public void gotolist(View view) {

        setContentView(R.layout.list);
        currentlayout = "list";
        listact();

    }

    public void goback(View view) {

        setContentView(R.layout.activity_main);
        currentlayout = "main";
        tts.speak("Ищите ближайший план пожарной эвакуации и следуйте инструкциям, указанным на нём", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void gobackfromlist(View view) {

        setContentView(R.layout.activity_main);
        currentlayout = "main";
    }

    public void zhighoul(View view) {
        tts.speak("uvu давай заведем старый жигуль? " +
                "вивививививививививививививививививививививививививививививививививививививививививививививививививививипрпрпррпрвививививививививививививи " +
                "Марат Радикович не сидите давайте помогайте до гаража толкать может там он заведётся.]", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void yes(View view) {
        if ((! cab.equals(" ")) & (currentlayout.equals("quest"))){
            setContentView(R.layout.activity_main);
            leg = findViewById(R.id.legend);
            iv = findViewById(R.id.iv);

            //Разговоры и вывод маршрута
            if (! cab.equals("")) {

                String imgname = "cab_" + cab;
                int holderint = getResources().getIdentifier(imgname, "drawable",
                        this.getPackageName());
                iv.setImageResource(holderint);
                leg.setImageResource(R.drawable.legend);

                if (cab.equals("212") | cab.equals("213")){
                    tts.speak("Направляйтесь к выходу из кабинета, поверните налево, через несколько метров по левой стене вы увидите лестницу",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
                if (cab.equals("210") | cab.equals("211")){
                    tts.speak("Направляйтесь к выходу из кабинета, поверните направо, через несколько метров по левой стене вы увидите лестницу",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
                if (cab.equals("206") | cab.equals("209")){
                    tts.speak("Направляйтесь к выходу из кабинета, напротив вас вы увидите лестницу",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
                if (cab.equals("208") | cab.equals("205")){
                    tts.speak("Направляйтесь к выходу из кабинета, поверните налево, через несколько метров по правой стене вы увидите лестницу",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
                if (cab.equals("215")) {
                    tts.speak("Направляйтесь к выходу из кабинета, поверните направо, через несколько метров по правой стене вы увидите лестницу",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
                if (cab.equals("А-321")) {
                    tts.speak("зхзххзхзхзхзххзхзхзхзхзхзхз",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            F = true;
            currentlayout = "main";
        }
    }

    public void scan() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    //Порядковые номера элементов в списке egug с заданными кабинетами
    public void getnumsl1(){
        for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("Выход")) {
                numsl1.add(i);
                break;
            }
        }
        /*for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("210")) {
                numsl1.add(i);
                break;
            }
        }
        for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("208")){
                numsl1.add(i);
                break;
            }
        }*/
    }

    //Порядковые номера элементов в списке egug с заданными кабинетами
    public void getnumsl2(){
        /*for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("215")) {
                numsl2.add(i);
                break;
            }
        }
        for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("208")){
                numsl2.add(i);
                break;
            }
        }
        for (int i=0; i<egug.size(); i++) {
            if (egug.get(i).getcab().substring(5).equals("206")){
                numsl2.add(i);
                break;
            }
        }*/
    }

    //Апдейт маршрутов и болтовня при дохождении до лестницы
    public void update() {

        iv = findViewById(R.id.iv);

        getnumsl1();
        //getnumsl2();

        try {
            if (currentlayout.equals("main")) {


                if (numsl1.size()==1){
                    if ((F) &
                            (Integer.valueOf(egug.get(numsl1.get(0)).strength.substring(10)) >= -41)){
                            //(Math.abs(Integer.valueOf(egug.get(numsl1.get(1)).strength.substring(10)) + 64) <=6) &
                            //(Math.abs(Integer.valueOf(egug.get(numsl1.get(2)).strength.substring(10)) + 74) <=6)) {

                        iv.setImageResource(R.drawable.l_1);
                        tts.speak("Вы находитесь недалеко от выхода", TextToSpeech.QUEUE_FLUSH, null);
                        F = false;
                    }
                }

                /*if (numsl2.size()==3){
                    if ((F) &
                            (Math.abs(Integer.valueOf(egug.get(numsl2.get(0)).strength.substring(10)) + 67) <=6) &
                            (Math.abs(Integer.valueOf(egug.get(numsl2.get(1)).strength.substring(10)) + 61) <=6) &
                            (Math.abs(Integer.valueOf(egug.get(numsl2.get(2)).strength.substring(10)) + 50) <=6)) {

                        iv.setImageResource(R.drawable.l_2);
                        tts.speak("Вы дошли до лестницы", TextToSpeech.QUEUE_FLUSH, null);
                        F = false;
                    }
                }*/
            }
        } catch (NumberFormatException e) {}

        numsl1.clear();
        //numsl2.clear();
    }


    //Код вкладки списка
    public void listact(){
        if (currentlayout.equals("list")) {

            lw = findViewById(R.id.wifilist);
            lw.setAdapter(ad);

            ad.notifyDataSetChanged();
        }
    }

    //Код вкладки вопроса
    public void questact() {

        if (currentlayout.equals("quest")) {

            if (cab.equals(" ")) {
                tts.speak("Вы не находитесь в контролируемой территории", TextToSpeech.QUEUE_FLUSH, null);
                setContentView(R.layout.activity_main);
                currentlayout = "main";
            }
        }
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            maxlev1 = -198;
            cab = " ";
            egug.clear();

            //Загон всех Item'ов, содержащих резельтаты скана и кабинет, в массив egug
            for (ScanResult scanResult : results) {
                if (bss.contains(scanResult.BSSID.substring(0, 15))){
                        egug.add(new Item("SSID: " + scanResult.SSID, "BSSID: " + scanResult.BSSID, "strength: " +
                                scanResult.level, "cab: " + cabs.get(bss.indexOf(scanResult.BSSID.substring(0, 15)))));
                }
                else {
                    egug.add(new Item("SSID: " + scanResult.SSID, "BSSID: " + scanResult.BSSID, "strength: " +
                            scanResult.level, "cab: none"));
                }
            }

            Collections.sort(egug, Item.COMPARE_BY_STRENGTH);

            //Определение кабинета, из которого идёт сильнейший сигнал
            for (int i = 0; i < egug.size(); i++) {
                if (!egug.get(i).getcab().equals("cab: none")) {
                    cab = egug.get(i).getcab().substring(5);
                    break;
                }
            }

            questact();
            listact();
            update();

            if (cab.equals(" ")){
                cab = "";
            }

            Log.wtf("scan", "poluchen");
        }
    };


    //Поток (фоновое сканирование)
    Runnable r = new Runnable(){
        @Override
        public void run() {
            while (true) {
                if (! geoloc) {
                    gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    geoloc = gps && nw;
                }
                if (geoloc) {
                    try {
                        Log.wtf("scan", "nachat");
                        scan();
                        potok.sleep(1000);
                        if (currentlayout.equals("list")) {
                            potok.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    };





    //Обработка ответа пользователя на разрешение
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (! (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                else{
                    gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    geoloc = gps && nw;
                    if (! geoloc){
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                    potok.start();
                }

                return;
            }
        }
    }
}