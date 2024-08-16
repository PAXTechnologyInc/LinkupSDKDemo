package com.pax.linkupsdk.demo.module.devcon;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.models.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Consts {
    public static Item APPLE = new Item("APPLE", "Red Apple", "0.85", "123", R.drawable.apple);
    public static Item BANANA = new Item("BANANA", "Yellow Banana", "0.75", "456", R.drawable.banana);
    public static Item COFFEE = new Item("COFFEE", "Hot Coffee", "2.50", "789", R.drawable.coffee);
    public static Item CUPCAKE = new Item("CUPCAKE", "Vanilla Cupcake", "2.00", "020", R.drawable.cupcake);

    private static Map<String, Item> createSkuMap() {
        HashMap<String, Item> map = new HashMap<>();
        map.put("123", APPLE);
        map.put("456", BANANA);
        map.put("789", COFFEE);
        map.put("010", CUPCAKE);
        return Collections.unmodifiableMap(map);
    }
    public static final Map<String, Item> SKU_MAP = createSkuMap();
}
