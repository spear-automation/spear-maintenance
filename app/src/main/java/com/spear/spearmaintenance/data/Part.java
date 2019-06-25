package com.spear.spearmaintenance.data;

import java.util.HashMap;
import java.util.Map;

public class Part {
    public String SerialNumber;
    public String Name;
    public String Quantity;

    public Part(String name, String quantity) {
        SerialNumber = (int) (Math.random() * 1000000) + "";
        Name = name;
        Quantity = quantity;
    }

    public HashMap<String, String> toDBFormat() {
        HashMap<String, String> m = new HashMap<>();
        m.put("SerialNumber", SerialNumber);
        m.put("Name", Name);
        m.put("Quantity", Quantity);
        return m;
    }
}
