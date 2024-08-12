package com.pax.linkupsdk.demo.module.devcon;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.models.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Consts {
    public static Item PEN = new Item("Pen", "Black Ink Pen", "1.75", "123", R.drawable.black_ink_pen);
    public static Item PAPER = new Item("PAPER", "3 Ring Paper", "1.35", "456", R.drawable.three_ring_paper);
    public static Item NOTEBOOK = new Item("NOTEBOOK", "Spiral Notebook", "3.00", "789", R.drawable.spiral_notebook);
    public static Item CHOCOLATE = new Item("CHOCOLATE", "Chocolate Bar", "1.15", "010", R.drawable.chocolate_bar);
    private static Map<String, Item> createSkuMap() {
        HashMap<String, Item> map = new HashMap<>();
        map.put("123", PEN);
        map.put("456", PAPER);
        map.put("789", NOTEBOOK);
        map.put("010", CHOCOLATE);
        return Collections.unmodifiableMap(map);
    }
    public static final Map<String, Item> SKU_MAP = createSkuMap();
}
