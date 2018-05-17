package com.example.somasur.weatherpics;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LruCache;

public class ImagesCache  implements Parcelable{

    private  LruCache<String, Bitmap> imagesWarehouse;

    private static ImagesCache cache;

    protected ImagesCache(Parcel in) {
    }

    public static final Creator<ImagesCache> CREATOR = new Creator<ImagesCache>() {
        @Override
        public ImagesCache createFromParcel(Parcel in) {
            return new ImagesCache(in);
        }

        @Override
        public ImagesCache[] newArray(int size) {
            return new ImagesCache[size];
        }
    };

    public ImagesCache() {

    }

    public static ImagesCache getInstance()
    {
        if(cache == null)
        {
            cache = new ImagesCache();
        }

        return cache;
    }

    public void initializeCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /1024);

        final int cacheSize = maxMemory / 8;

        System.out.println("cache size = "+cacheSize);

        imagesWarehouse = new LruCache<String, Bitmap>(cacheSize)
        {
            protected int sizeOf(String key, Bitmap value)
            {
                // The cache size will be measured in kilobytes rather than number of items.

                int bitmapByteCount = value.getRowBytes() * value.getHeight();

                return bitmapByteCount / 1024;
            }
        };
    }

    public void addImageToWarehouse(String key, Bitmap value)
    {
        if(imagesWarehouse != null && imagesWarehouse.get(key) == null)
        {
            imagesWarehouse.put(key, value);
        }
    }

    public Bitmap getImageFromWarehouse(String key)
    {
        if(key != null)
        {
            return imagesWarehouse.get(key);
        }
        else
        {
            return null;
        }
    }

    public void removeImageFromWarehouse(String key)
    {
        imagesWarehouse.remove(key);
    }

    public void clearCache()
    {
        if(imagesWarehouse != null)
        {
            imagesWarehouse.evictAll();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
