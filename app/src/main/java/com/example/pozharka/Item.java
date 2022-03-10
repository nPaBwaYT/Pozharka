package com.example.pozharka;

import java.util.Comparator;

public class Item {

    String SSID;
    String BSSID;
    String strength;
    String cab;

    Item(String ls, String lbs, String lst, String cb) {
        this.SSID = ls;
        this.BSSID = lbs;
        this.strength = lst;
        this.cab = cb;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getcab() {
        return cab;
    }

    public void setcab(String cab) {
        this.cab = cab;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getstrength() {
        return strength;
    }

    public void setstrength(String strength) {
        this.strength = strength;
    }

    public static final Comparator<Item> COMPARE_BY_STRENGTH = new Comparator<Item>() {
        @Override
        public int compare(Item lhs, Item rhs) {
            return lhs.getstrength().compareTo(rhs.getstrength());
        }
    };
}