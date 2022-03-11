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
import android.os.Bundle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;



import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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

        private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

        public String currentlayout = "main";

        private TextToSpeech tts;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            //Полноэкранный режим
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            //Создание файла-базы (по-хорошему поменять бы способ создания)
            FileOutputStream fos = null;
            try {
                String text = "00:f6:63:d7:31: 206\n" +
                        "cc:16:7e:aa:f1: 208\n" +
                        "00:81:c4:73:bf: 209\n" +
                        "2c:33:11:f8:86: 210\n" +
                        "cc:16:7e:96:cb: 211\n" +
                        "00:f6:63:97:19: 212\n" +
                        "b8:11:4b:93:4a: 215\n" +
                        "c0:c9:e3:08:bc: дом\n" +
                        "c8:d3:ff:59:05: принтер";

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
            else { //Включение геолокации
                LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean geoloc = gps && nw;
                if (! geoloc){
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                else{
                    //Запуск сканирования
                    potok.start();
                }
            }
        }

        public void start(View view) {
            if (!cab.equals(" ")) {
                setContentView(R.layout.questions);
                currentlayout = "quest";

                TextView tq = findViewById(R.id.tquest);

                tq.setText("Вы находитесь внутри кабинета?");
                tts.speak("Вы находитесь внутри кабинета?", TextToSpeech.QUEUE_FLUSH, null);

                questact();
            }
        }

        public void reset(View view) {

            ImageView leg = findViewById(R.id.legend);
            ImageView iv = findViewById(R.id.iv);
            iv.setImageResource(R.drawable.logo);
            leg.setImageResource(R.drawable.logo);
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

        }

        public void zhighoul(View view) {
            tts.speak("uvu давай заведем старый жигуль? " +
                    "вивививививививививививививививививививививививививививививививививививививививививививививививививививипрпрпррпрвививививививививививививи " +
                    "Марат Радикович не сидите давайте помогайте до гаража толкать может там он заведётся.]", TextToSpeech.QUEUE_FLUSH, null);
        }

        public void yes(View view) {
            if ((! cab.equals(" ")) & (currentlayout.equals("quest"))){
                setContentView(R.layout.activity_main);

                //Разговоры и вывод маршрута
                if (! cab.equals("")) {
                    ImageView iv = findViewById(R.id.iv);
                    ImageView leg = findViewById(R.id.legend);

                    String imgname = "cab_" + cab;
                    int holderint = getResources().getIdentifier(imgname, "drawable",
                            this.getPackageName());
                    iv.setImageResource(holderint);
                    leg.setImageResource(R.drawable.legend);

                    tts.speak("Вы находитесь в " + cab + " кабинете", TextToSpeech.QUEUE_FLUSH, null);

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
                }
                F = true;
                currentlayout = "main";
            }
        }

        public void scan() {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
        }

        //Порядковые номера элементов в списке egug с заданными кабинетами
        public void getnumsl1(){
            for (int i=0; i<egug.size(); i++) {
                if (egug.get(i).getcab().substring(5).equals("дом")) {
                    numsl1.add(i);
                    break;
                }
            }
            for (int i=0; i<egug.size(); i++) {
                if (egug.get(i).getcab().substring(5).equals("принтер")){
                    numsl1.add(i);
                    break;
                }
            }
        }

        //Порядковые номера элементов в списке egug с заданными кабинетами
        public void getnumsl2(){
            for (int i=0; i<egug.size(); i++) {
                if (egug.get(i).getcab().substring(5).equals("дом")) {
                    numsl2.add(i);
                    break;
                }
            }
            for (int i=0; i<egug.size(); i++) {
                if (egug.get(i).getcab().substring(5).equals("принтер")){
                    numsl2.add(i);
                    break;
                }
            }
        }

        //Апдейт маршрутов и болтовня при дохождении до лестницы
        public void update() {

            getnumsl1();
            getnumsl2();

            try {
                if (currentlayout.equals("main")) {

                    ImageView iv = findViewById(R.id.iv);

                    if ((F) & //Условие разности сигналов из кабинетов под порядковыми номерами в списке numsl1 или numsl2 меньше 5
                            (Math.abs(Integer.valueOf(egug.get(numsl1.get(0)).strength.substring(10)) -
                                    Integer.valueOf(egug.get(numsl1.get(1)).strength.substring(10)) + 0) <= 8)) {

                        iv.setImageResource(R.drawable.l_1);
                        tts.speak("Вы дошли до лестницы", TextToSpeech.QUEUE_FLUSH, null);
                        F = false;
                    }
                    else if ((F) &
                            (Math.abs(Integer.valueOf(egug.get(numsl2.get(0)).strength.substring(10)) -
                            Integer.valueOf(egug.get(numsl2.get(1)).strength.substring(10)) + 0) <= 8)){

                        iv.setImageResource(R.drawable.l_2);
                        tts.speak("Вы дошли до лестницы", TextToSpeech.QUEUE_FLUSH, null);
                        F = false;
                    }
                }
            } catch (NumberFormatException e) {}

            numsl1.clear();
        }

        public adap liw(){

            final adap adapter =  new adap(this, egug);

            return (adapter);
        }

        //Код вкладки списка
        public void listact(){
            if (currentlayout.equals("list")) {
                adap ad = liw();
                ListView lw = findViewById(R.id.wifilist);
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

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                results = wifiManager.getScanResults();
                unregisterReceiver(this);

                maxlev1 = -198;
                cab = " ";
                egug.clear();

                //Загон всех Item'ов, содержащих резельтаты скана и кабинет, в массив egug
                for (ScanResult scanResult : results) {
                    if (bss.contains(scanResult.BSSID.substring(0, 15))){
                        egug.add(new Item("SSID: " + scanResult.SSID, "BSSID: " + scanResult.BSSID, "strength: " +
                                scanResult.level, "cab: " +  cabs.get(bss.indexOf(scanResult.BSSID.substring(0, 15)))));
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
            }
        };

        //Поток (фоновое сканирование)
        Runnable r = new Runnable(){
            @Override
            public void run() {
                while (true) {
                    try {
                        scan();
                        potok.sleep(2300);
                        if (currentlayout.equals("list")) {
                            potok.sleep(700);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        Thread  potok = new Thread(r, "scaner");

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
                    return;
                }
            }
        }
    }