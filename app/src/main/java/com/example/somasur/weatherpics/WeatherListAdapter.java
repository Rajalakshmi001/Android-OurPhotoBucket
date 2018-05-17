package com.example.somasur.weatherpics;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.somasur.weatherpics.fragments.WeatherListFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private List<Weather> mWeatherList;
    private WeatherListFragment.Callback mCallback;
    private DatabaseReference mWeatherListRef;
    private DatabaseReference mWeatherTitleRef;
    private Context mContext;
    private EditDialog mEditDialog;
    private String mUid;

    public WeatherListAdapter(Context context, WeatherListFragment.Callback callback, EditDialog editDialog, WeatherListFragment weatherListFragment, boolean meOrAll) {
        mCallback = callback;
        mContext = context;
        mEditDialog = editDialog;
        mWeatherList = new ArrayList<>();
        if(weatherListFragment != null) {
            mUid = SharedPreferencesUtils.getCurrentUser(weatherListFragment.getContext());
            Log.d(Constants.TAG, "Current user: " + mUid);

            assert (!mUid.isEmpty()); // Consider: use if (BuildConfig.DEBUG)
        }
        mWeatherListRef = FirebaseDatabase.getInstance().getReference().child("Weather Pics");
//        mWeatherTitleRef = FirebaseDatabase.getInstance().getReference().child("app_title");
        if(meOrAll) {
            Query myPicsRef = mWeatherListRef.orderByChild("uid").equalTo(mUid);
            myPicsRef.addChildEventListener(new WeatherChildEventListener());
        } else {
            mWeatherListRef.addChildEventListener(new WeatherChildEventListener());
        }
//        mWeatherTitleRef.addValueEventListener(new TitleValueListener());
        mWeatherListRef.keepSynced(true);
//        mWeatherTitleRef.keepSynced(true);
    }

    class TitleValueListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String  title = dataSnapshot.getValue(String.class);
            if(mEditDialog != null) {
                mEditDialog.setTitle(title);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
    class WeatherChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Weather weather = dataSnapshot.getValue(Weather.class);
            weather.setKey(dataSnapshot.getKey());
            mWeatherList.add(0, weather);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Weather updatedWeather = dataSnapshot.getValue(Weather.class);
            for (Weather mq: mWeatherList){
                if(mq.getKey().equals(key)){
                    mq.setValues(updatedWeather);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for (Weather mq: mWeatherList) {
                if(mq.getKey().equals(key)){
                    mWeatherList.remove(mq);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("MQ ", "Database error: " + databaseError);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_entries, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Weather weather = mWeatherList.get(position);
        holder.mTitleTextView.setText(weather.getTitle());
        holder.mUrlTextView.setText(weather.getUrl());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSelect(weather);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mWeatherList.get(position).getUid().equals(mUid)) {
                    mCallback.showAddEditDialog(weather);
                } else {
                    Toast.makeText(mContext, "This weatherpic belongs to another user", Toast.LENGTH_LONG).show();
                }
                return true;

            }
        });
    }


    public void remove(Weather weather) {
        mWeatherListRef.child(weather.getKey()).removeValue();
    }


    @Override
    public int getItemCount() {
        return mWeatherList.size();
    }

    public void add(Weather weather) {
        mWeatherListRef.push().setValue(weather);
    }

    public void update(Weather weather, String title, String url) {
        weather.setTitle(title);
        weather.setUrl(url);
        mWeatherListRef.child(weather.getKey()).setValue(weather);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;
        private TextView mUrlTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.weather_title_text_view);
            mUrlTextView = itemView.findViewById(R.id.weather_image_url_text_view);
        }
    }

    public interface EditDialog {
        void setTitle(String title);
        void setMenuTitle(String menuTitle);
    }

}

