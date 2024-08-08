package com.pax.linkupsdk.demo.module.devcon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Consts {
    public static final Map<String, Item> SKU_MAP = createSkuMap();

    private static Map<String, Item> createSkuMap() {
        HashMap<String, Item> map = new HashMap<>();
        map.put("123", new Item("Pen", "Black Ink Pen", "1.75", "SKU123"));
        map.put("456", new Item("Paper", "3 Ring Paper", "1.35", "SKU456"));
        map.put("789", new Item("Notebook", "Spiral Notebook", "3.00", "SKU789"));
        map.put("010", new Item("Chocolate", "Chocolate Bar", "1.15", "SKU010"));
        return Collections.unmodifiableMap(map);
    }
}
