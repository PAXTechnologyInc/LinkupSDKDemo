package com.pax.linkupsdk.demo.module.devcon;

import com.pax.linkupsdk.demo.module.devcon.models.Item;

public interface CartListener {
    void onItemAdded(Item item);

    void onItemDeleted(int position);
}
