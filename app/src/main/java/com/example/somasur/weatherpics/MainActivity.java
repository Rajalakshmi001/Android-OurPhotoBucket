package com.example.somasur.weatherpics;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.somasur.weatherpics.fragments.LoginFragment;
import com.example.somasur.weatherpics.fragments.WeatherDetailFragment;
import com.example.somasur.weatherpics.fragments.WeatherListFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener, WeatherListFragment.Callback, GoogleApiClient.OnConnectionFailedListener, WeatherListFragment.OnLogoutListener {

    private static final int RC_ROSEFIRE_LOGIN = 2;
    private WeatherListAdapter mAdapter;
    private WeatherListFragment.Callback mCallback;
    private WeatherListFragment weatherListFragment;
    private WeatherListAdapter.EditDialog mEditDialog;
    private ImagesCache cache;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private OnCompleteListener mOncompleteListener;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        cache = ImagesCache.getInstance();
        cache.initializeCache();

        mAdapter = new WeatherListAdapter(this, mCallback, mEditDialog, weatherListFragment, true);

        mAuth = FirebaseAuth.getInstance();
        initializeListeners();
        initializeGoogle();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("APP", "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("APP", "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("APP", "facebook:onError", exception);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("APP", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        SharedPreferencesUtils.setCurrentUser(MainActivity.this, token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, mOncompleteListener);
    }

    private void initializeListeners() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("APP", "facebook:onSuccess:" + user.getUid());
                    SharedPreferencesUtils.setCurrentUser(MainActivity.this, user.getUid());
                    switchToWeatherListFragment("users/" + user.getUid());
                } else {
                    switchToLoginFragment();
                }
            }
        };

        mOncompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    showLoginError("Login failed");
                }
            }
        };
    }

    private void initializeGoogle() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).
                        build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mOncompleteListener);
    }

    @Override
    public void onGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                showLoginError("google signin failed!!");
            }
        } else if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (result.isSuccessful()) {
                SharedPreferencesUtils.setCurrentUser(this, result.getUsername());
                Log.d("APP", "facebook:onSuccess:" + result.getUsername());
                mAuth.signInWithCustomToken(result.getToken())
                        .addOnCompleteListener(this, mOncompleteListener);
            } else {
                showLoginError("Rosefire sign in  failed!!");
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(Constants.TAG, "firebaseAuthWithGoogle:" + acct.getId());
        Log.d(Constants.TAG, "Trying to print credentials: " + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Log.d("APP", "facebook:onSuccess:" + acct.getId());
        SharedPreferencesUtils.setCurrentUser(this, acct.getId());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, mOncompleteListener);
    }


    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new LoginFragment(), "Login");
        ft.commit();
    }

    private void switchToWeatherListFragment(String path) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        weatherListFragment = new WeatherListFragment();

        Bundle args = new Bundle();
        args.putString(Constants.FIREBASE_PATH, path);
        weatherListFragment.setArguments(args);
        ft.replace(R.id.fragment, weatherListFragment, "Weather List");
        ft.commit();
    }

    private void showLoginError(String message) {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("Login");
        loginFragment.onLoginError(message);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showLoginError("Google Connection failed!!");
    }

    @Override
    public void onSelect(final Weather weather) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        WeatherDetailFragment fragment = WeatherDetailFragment.newInstance(weather, cache);
        ft.replace(R.id.fragment, fragment);
        ft.addToBackStack("detail");
        ft.commit();
    }

    @Override
    public void onLogout() {
        mAuth.signOut();
    }

    @Override
    public void onRosefireLogin() {
        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key));
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);

    }

    @Override
    public void onFacebookLogin() {

    }

    public void showAddEditDialog(final Weather weather) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(weather == null ? R.string.dialog_add_title : R.string.dialog_edit_title));
        View view = getLayoutInflater().inflate(R.layout.add_or_edit_dialog, null, false);
        builder.setView(view);
        final EditText titleEditText = view.findViewById(R.id.weather_title_edit_text);
        final EditText urlEditText = view.findViewById(R.id.weather_url_edit_text);
        final Button browseButton = view.findViewById(R.id.browse_button);

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                startActivity(search);
            }
        });

        if (weather != null) {
            // pre-populate
            titleEditText.setText(weather.getTitle());
            urlEditText.setText(weather.getUrl());

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // empty
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // empty
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String title = titleEditText.getText().toString();
                    String url = urlEditText.getText().toString();
                    mAdapter.update(weather, title, url);
                }
            };

            titleEditText.addTextChangedListener(textWatcher);
            urlEditText.addTextChangedListener(textWatcher);
        }

        if (weather != null) {
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAdapter.remove(weather);
                }
            });
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (weather == null) {
                    String title = titleEditText.getText().toString();
                    String url = urlEditText.getText().toString();
                    if (url.matches("")) {
                        url = Util.randomImageUrl();
                    }
                    String uid = SharedPreferencesUtils.getCurrentUser(MainActivity.this);
                    mAdapter.add(new Weather(title, url, uid));
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.create().show();
    }

}
