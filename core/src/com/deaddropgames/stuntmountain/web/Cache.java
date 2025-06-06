package com.deaddropgames.stuntmountain.web;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class Cache {

    private static final String LOG = "Cache";

    // cap the number of items we will store in the cache
    private static final int MAX_ITEMS = 100;

    private long ttlMilliseconds;
    private Map<String, CacheItem> data;

    Cache(long ttlMilliseconds) {

        this.ttlMilliseconds = ttlMilliseconds;
        data = new HashMap<String, CacheItem>();
    }

    void addItem(final String key, final Object item) {

        if (data.size() >= MAX_ITEMS) {

            Gdx.app.error(LOG, "Cache is full - ignoring item.");
            return;
        }
        data.put(key, new CacheItem(item));

        Gdx.app.debug(LOG, String.format(Locale.getDefault(), "Cache size is %d", data.size()));
    }

    Object getItem(final String key) {

        // see if the key exists in the cache
        if (data.containsKey(key)) {

            // cache hit - check for expiry
            if (System.currentTimeMillis() - data.get(key).getTimestamp() <= ttlMilliseconds) {

                // it's a hit!
                Gdx.app.debug(LOG, String.format("Cache hit for key '%s'", key));
                return data.get(key).getItem();
            }

            // expired cache
            Gdx.app.debug(LOG, String.format("Cache miss - expired key for '%s'", key));
            data.remove(key);
        }

        // cache miss
        Gdx.app.debug(LOG, String.format("Cache miss for key '%s'", key));
        return null;
    }

    private class CacheItem {

        private long timestamp;
        private Object item;

        CacheItem(Object item) {

            timestamp = System.currentTimeMillis();
            this.item = item;
        }

        long getTimestamp() {

            return timestamp;
        }

        Object getItem() {

            return item;
        }
    }
}
