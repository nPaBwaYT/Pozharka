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
import java.util.HashMap;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;



import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

        public  ArrayList<HashMap<String , String>> egugstrength = new ArrayList<>();
        public  List<ScanResult> results;
        public  ArrayList<String> bss = new ArrayList<>();
        public  ArrayList<String> cabs = new ArrayList<>();
        public  String[] base = new String[]{" "};

        public int maxlev1 = -198;
        public String rbssid1 =" ";
        public String rssid1 =" ";
        public String cab =" ";

        private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

        public String currentlayout = "main";

        private TextToSpeech tts;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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
            FileOutputStream fos = null;
            try {
                String text = "00:f6:63:d7:31: 206\n" +
                        "cc:16:7e:aa:f1: 208\n" +
                        "00:81:c4:73:bf: 209\n" +
                        "2c:33:11:f8:86: 210\n" +
                        "cc:16:7e:96:cb: 211\n" +
                        "00:f6:63:97:19: 212\n" +
                        "b8:11:4b:93:4a: 215";

                fos = openFileOutput("bssids.txt", MODE_PRIVATE);
                fos.write(text.getBytes());
            } catch (IOException ex) {
                setContentView(R.layout.activity_main);
                currentlayout = "main";
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException ex) {
                    setContentView(R.layout.activity_main);
                    currentlayout = "main";
                }
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                   && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

               ActivityCompat.requestPermissions(MainActivity.this,
                       new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                       MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean nw = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean geoloc = gps && nw;
            if (! geoloc){
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }

        public void btonClick(View view) {
            setContentView(R.layout.questions);
            currentlayout = "quest";
            scan();
        }

        public void btonClick2(View view) {

            ImageView leg = findViewById(R.id.legend);
            ImageView iv = findViewById(R.id.iv);
            iv.setImageResource(R.drawable.logo);
            leg.setImageResource(R.drawable.logo);
        }

        public void gotolist(View view) {

            setContentView(R.layout.list);
            currentlayout = "list";

            scan();
        }

        public void goback(View view) {
            if (! cab.equals(" ")) {
                setContentView(R.layout.activity_main);
                currentlayout = "main";
            }
        }

        public void zhighoul(View view) {
            tts.speak("uvu давай заведем старый жигуль? " +
                    "вививививививививививививививививививививививививививививививививививививививививививививививививививививививививививививививививи " +
                    "Марат Радикович не сидите давайте помогайте до гаража толкать может там он заведётся.]", TextToSpeech.QUEUE_FLUSH, null);
        }

        public void yes(View view) {
            if ((! cab.equals(" ")) & (currentlayout.equals("quest"))){
                setContentView(R.layout.activity_main);

                if (! cab.equals("")) {
                    ImageView iv = findViewById(R.id.iv);
                    ImageView leg = findViewById(R.id.legend);

                    String imgname = "cab_" + cab;
                    int holderint = getResources().getIdentifier(imgname, "drawable",
                            this.getPackageName());
                    iv.setImageResource(holderint);
                    leg.setImageResource(R.drawable.legend);

                    tts.speak("Вы находитесь в " + cab.toString()+ " кабинете", TextToSpeech.QUEUE_FLUSH, null);

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

                currentlayout = "main";
            }
        }

        public void scan() {
            egugstrength.clear();
            cab=" ";
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
        }

        public SimpleAdapter liw(){

            final SimpleAdapter adapter =  new SimpleAdapter(this, egugstrength, android.R.layout.simple_list_item_2,
                    new String[]{"name", "strength"}, new int[]{android.R.id.text1, android.R.id.text2});

            return (adapter);
        }

        BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                results = wifiManager.getScanResults();
                unregisterReceiver(this);

                SimpleAdapter ad = liw();

                if (currentlayout.equals("list")) {
                    ListView lw = findViewById(R.id.wifilist);
                    lw.setAdapter(ad);
                }

                maxlev1 = -198;
                rbssid1 =" ";
                rssid1 =" ";
                cab =" ";

                if (currentlayout.equals("quest")) {
                    FileInputStream fin = null;
                    TextView tq = findViewById(R.id.tquest);
                    try {
                        fin = openFileInput("bssids.txt");
                        byte[] bytes = new byte[fin.available()];
                        fin.read(bytes);
                        String text1 = new String(bytes);
                        String[] base = text1.split("\n");

                        for (int i = 0; i < base.length; i++) {
                            bss.add(base[i].split(" ")[0]);
                            cabs.add(base[i].split(" ")[1]);
                        }

                    } catch (IOException ex) {
                        tq.setText(ex.getMessage());
                    } finally {
                        try {
                            if (fin != null)
                                fin.close();
                        } catch (IOException ex) {
                            tq.setText(ex.getMessage());
                        }
                    }
                }

                for(ScanResult scanResult : results ) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", "bssid: " + scanResult.BSSID);
                    map.put("strength", "strength: " + scanResult.level);

                    egugstrength.add(map);

                    if (currentlayout.equals("quest")) {
                        if ((scanResult.level > maxlev1) & (bss.indexOf(scanResult.BSSID.substring(0, 15))!=-1)) {

                            maxlev1 = scanResult.level;
                            rbssid1 = scanResult.BSSID;
                            rssid1 = scanResult.SSID;
                            cab = cabs.get(bss.indexOf(scanResult.BSSID.substring(0, 15)));
                        }
                    }
                }

                ad.notifyDataSetChanged();

                if (currentlayout.equals("quest")){

                    TextView tq = findViewById(R.id.tquest);
                    tq.setText("Вы находитесь внутри кабинета?");
                    tts.speak("Вы находитесь внутри кабинета?", TextToSpeech.QUEUE_FLUSH, null);

                    if (cab.equals(" ")) {
                        tts.speak("Вы не находитесь в контролируемой территории", TextToSpeech.QUEUE_FLUSH, null);
                        setContentView(R.layout.activity_main);
                        currentlayout = "main";
                    }
                }
                if (cab.equals(" ")) {
                    cab = "";
                }
            }
        };

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