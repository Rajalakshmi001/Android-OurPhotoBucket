package com.example.somasur.weatherpics.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.somasur.weatherpics.R;
import com.example.somasur.weatherpics.Util;
import com.example.somasur.weatherpics.Weather;
import com.example.somasur.weatherpics.WeatherListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callback} interface
 * to handle interaction events.
 */
public class WeatherListFragment extends Fragment implements Toolbar.OnMenuItemClickListener, View.OnClickListener, WeatherListAdapter.EditDialog {

    private Callback mCallback;
    private OnLogoutListener mListener;
    private WeatherListAdapter mAdapter;
    private Toolbar mToolbar;
    private WeatherListAdapter.EditDialog mEditDialog;
    private MenuItem mMenuItem;
    private RecyclerView view;
    private boolean isMine = true;

    public WeatherListFragment() {
        // Required empty public constructor
    }

    public interface OnLogoutListener {
        void onLogout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather_list, container, false);
        // Setup Toolbar
        mToolbar = rootView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, mToolbar.getMenu());
        mMenuItem = mToolbar.getMenu().findItem(R.id.show_mine_menu);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        final View fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //Recycler View
        view = rootView.findViewById(R.id.weather_list);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(manager);
        mAdapter = new WeatherListAdapter(getActivity(), mCallback, mEditDialog, this, isMine);
        view.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WeatherListFragment Callback");
        }

        if (context instanceof OnLogoutListener) {
            mListener = (OnLogoutListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    "must implement WeatherListFragment OnLogoutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mListener = null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_logout:
                Log.d("PK", "LOGOUT Menu Item Clicked!");
                mListener.onLogout();
                return true;
            case R.id.show_mine_menu:
                Log.d("PK", "Show mine Menu Item clicked!");
                //TODO: action to be performed on Click
                setMenuTitle((String) mMenuItem.getTitle());
                mAdapter = new WeatherListAdapter(getActivity(), mCallback, mEditDialog, this, isMine);
                view.setAdapter(mAdapter);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        mCallback.showAddEditDialog(null);
    }

    @Override
    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void setMenuTitle(String menuTitle) {
        if(menuTitle.equals("SHOW MINE")) {
            isMine = true;
            mMenuItem.setTitle("SHOW ALL");
        } else  {
            isMine = false;
            mMenuItem.setTitle("SHOW MINE");
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Callback {
        void onSelect(Weather weather);

        void showAddEditDialog(Weather weather);
    }
}