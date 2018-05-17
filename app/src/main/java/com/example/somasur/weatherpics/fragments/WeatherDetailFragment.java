package com.example.somasur.weatherpics.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.somasur.weatherpics.ImagesCache;
import com.example.somasur.weatherpics.MainActivity;
import com.example.somasur.weatherpics.SquareImageView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chrisbanes.photoview.PhotoView;

import com.example.somasur.weatherpics.Weather;
import com.example.somasur.weatherpics.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherDetailFragment extends Fragment implements  Toolbar.OnMenuItemClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_WEATHER = "weather";

    private Weather mWeather;
    private SquareImageView mSquareImageView;
    private TextView mtitleView;
    private Bitmap myBitmap;
    private ImagesCache mCache;
    private WeatherListFragment.OnLogoutListener mListener;

    public WeatherDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param weather
     * @return A new instance of fragment WeatherDetailFragment.
     */
    public static WeatherDetailFragment newInstance(Weather weather, ImagesCache cache) {
        WeatherDetailFragment fragment = new WeatherDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_WEATHER, weather);
        args.putParcelable("CACHE ",  cache);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWeather = getArguments().getParcelable(ARG_WEATHER);
            mCache = getArguments().getParcelable("CACHE ");
            String urlString = mWeather.getUrl();

            Bitmap bm = mCache.getImageFromWarehouse(urlString);

            if(bm!= null) {
                myBitmap = bm;
            } else {
                new GetWeatherPic(mCache, urlString).execute(urlString);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather_detail, container, false);
        Toolbar mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, mToolbar.getMenu());
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mtitleView = view.findViewById(R.id.weather_title_text_detail_fragment);
        mSquareImageView = view.findViewById(R.id.image_detail_fragment);
        mtitleView.setText(mWeather.getTitle());
        if(myBitmap!= null) {
            mSquareImageView.setImageBitmap(myBitmap);
        }
        return view;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_logout:
                Log.d("PK", "LOGOUT Menu Item Clicked!");
                mListener.onLogout();
                return true;
        }
        return false;
    }
    public void onImageLoaded(Bitmap bitmap) {
        Bitmap mBitmap = bitmap;
        myBitmap = bitmap;
        mSquareImageView.setImageBitmap(mBitmap);
    }

    public class GetWeatherPic extends AsyncTask<String, Void, Bitmap> {

        private ImagesCache cache;
        private String url;

        public GetWeatherPic(ImagesCache cache, String url) {
            this.cache = cache;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... urlstrings) {
            String urlString = urlstrings[0];
            Bitmap bitmap= null;
            try {
                URL url = new URL(urlString);
                if (mCache.getImageFromWarehouse(urlString) == null) {
                    bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
                    mCache.addImageToWarehouse(urlString, bitmap);
                } else {
                    bitmap = mCache.getImageFromWarehouse(urlString);
                }
            } catch (IOException e) {
                Log.d("ERROR: ", e.toString());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null) {
                mCache.addImageToWarehouse(url, bitmap);
            }
            onImageLoaded(bitmap);
        }
    }

}