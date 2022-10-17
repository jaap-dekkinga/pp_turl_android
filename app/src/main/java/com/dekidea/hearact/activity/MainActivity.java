package com.dekidea.hearact.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResult;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResultListener;
import com.dekidea.tuneurl.util.TuneURLManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.dekidea.hearact.adapter.NavListAdapter;
import com.dekidea.hearact.core.preferences.UserPreferences;
import com.dekidea.hearact.core.receiver.MediaButtonReceiver;
import com.dekidea.hearact.core.service.playback.PlaybackService;
import com.dekidea.hearact.core.util.StorageUtils;
import com.dekidea.hearact.core.util.download.AutoUpdateManager;
import com.dekidea.hearact.event.MessageEvent;
import com.dekidea.hearact.fragment.AddFeedFragment;
import com.dekidea.hearact.fragment.AudioPlayerFragment;
import com.dekidea.hearact.fragment.CompletedDownloadsFragment;
import com.dekidea.hearact.fragment.EpisodesFragment;
import com.dekidea.hearact.fragment.FavoriteEpisodesFragment;
import com.dekidea.hearact.fragment.FeedItemlistFragment;
import com.dekidea.hearact.fragment.InboxFragment;
import com.dekidea.hearact.fragment.PlaybackHistoryFragment;
import com.dekidea.hearact.fragment.QueueFragment;
import com.dekidea.hearact.fragment.SearchFragment;
import com.dekidea.hearact.fragment.SubscriptionFragment;
import com.dekidea.hearact.fragment.TransitionEffect;
import com.dekidea.hearact.fragment.preferences.AutoDownloadPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.ImportExportPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.MainPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.NetworkPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.NotificationPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.PlaybackPreferencesFragment;
import com.dekidea.hearact.fragment.preferences.StoragePreferencesFragment;
import com.dekidea.hearact.fragment.preferences.SwipePreferencesFragment;
import com.dekidea.hearact.fragment.preferences.UserInterfacePreferencesFragment;
import com.dekidea.hearact.fragment.preferences.synchronization.SynchronizationPreferencesFragment;
import com.dekidea.hearact.playback.cast.CastEnabledActivity;
import com.dekidea.hearact.preferences.PreferenceUpgrader;
import com.dekidea.hearact.ui.appstartintent.MainActivityStarter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.dekidea.hearact.R;

import com.dekidea.hearact.view.LockableBottomSheetBehavior;

/**
 * The activity that is shown when the user launches the app.
 */
public class MainActivity extends CastEnabledActivity implements SearchPreferenceResultListener {

    private static final String TAG = "MainActivity";
    public static final String MAIN_FRAGMENT_TAG = "main";

    public static final String PREF_NAME = "MainActivityPrefs";
    public static final String PREF_IS_FIRST_LAUNCH = "prefMainActivityIsFirstLaunch";

    public static final String EXTRA_FRAGMENT_TAG = "fragment_tag";
    public static final String EXTRA_FRAGMENT_ARGS = "fragment_args";
    public static final String EXTRA_FEED_ID = "fragment_feed_id";
    public static final String EXTRA_REFRESH_ON_START = "refresh_on_start";
    public static final String EXTRA_STARTED_FROM_SEARCH = "started_from_search";
    public static final String EXTRA_ADD_TO_BACK_STACK = "add_to_back_stack";
    public static final String KEY_GENERATED_VIEW_ID = "generated_view_id";

    private @Nullable DrawerLayout drawerLayout;
    private @Nullable ActionBarDrawerToggle drawerToggle;
    
    private LockableBottomSheetBehavior sheetBehavior;
    private long lastBackButtonPressTime = 0;
    private RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
    private int lastTheme = 0;


    BottomNavigationView mBottomNavigationView;
    private TextView fragmentTitle;
    private TextView fragmentSubTitle;

    public void setSelectedFragmentTitle(String title){

        fragmentTitle.setText(title);
    }

    public void setSelectedFragmentSubTitle(String subTitle){

        fragmentSubTitle.setText(subTitle);
    }



    @NonNull
    public static Intent getIntentToOpenFeed(@NonNull Context context, long feedId) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_FEED_ID, feedId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        lastTheme = UserPreferences.getNoTitleTheme();
        setTheme(lastTheme);
        if (savedInstanceState != null) {
            ensureGeneratedViewIdGreaterThan(savedInstanceState.getInt(KEY_GENERATED_VIEW_ID, 0));
        }
        super.onCreate(savedInstanceState);
        StorageUtils.checkStorageAvailability(this);
        setContentView(R.layout.main_new);
        fragmentTitle = findViewById(R.id.fragmentTitle);
        fragmentSubTitle = findViewById(R.id.fragmentSubTitle);
        recycledViewPool.setMaxRecycledViews(R.id.view_type_episode_item, 25);
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.action_favorite){

                    loadFragment(FavoriteEpisodesFragment.TAG, null);
                }
                else if(item.getItemId() == R.id.action_search){

                    loadFragment(AddFeedFragment.TAG, null);
                }
                else if(item.getItemId() == R.id.action_download){

                    loadFragment(CompletedDownloadsFragment.TAG, null);
                }
                else if(item.getItemId() == R.id.action_bookmark){

                    loadFragment(QueueFragment.TAG, null);
                }
                else if(item.getItemId() == R.id.action_settings){

                    loadFragment(MainPreferencesFragment.TAG, null);
                }
                
                return true;
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);

        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(MAIN_FRAGMENT_TAG) == null) {

            String lastFragment = getLastNavFragment(this);
            if (ArrayUtils.contains(NAV_DRAWER_TAGS, lastFragment)) {

                setSelectedPanel(lastFragment);
            }
            else {

                try {

                    loadFeedFragmentById(Integer.parseInt(lastFragment), null);
                }
                catch (NumberFormatException e) {
                    // it's not a number, this happens if we removed
                    // a label from the NAV_DRAWER_TAGS
                    // give them a nice default...
                    setSelectedPanel(FavoriteEpisodesFragment.TAG);
                }
            }
        }

        FragmentTransaction transaction = fm.beginTransaction();
        AudioPlayerFragment audioPlayerFragment = new AudioPlayerFragment();
        transaction.replace(R.id.audioplayerFragment, audioPlayerFragment, AudioPlayerFragment.TAG);
        transaction.commit();

        checkFirstLaunch();
        PreferenceUpgrader.checkUpgrades(this);
        View bottomSheet = findViewById(R.id.audioplayerFragment);
        sheetBehavior = (LockableBottomSheetBehavior) BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.external_player_height));
        sheetBehavior.setHideable(false);
        sheetBehavior.setBottomSheetCallback(bottomSheetCallback);
    }


    private void setSelectedPanel(String lastFragment){

        if(lastFragment.equals(FavoriteEpisodesFragment.TAG)){

            mBottomNavigationView.setSelectedItemId(R.id.action_favorite);
        }
        else if(lastFragment.equals(AddFeedFragment.TAG)){

            mBottomNavigationView.setSelectedItemId(R.id.action_search);
        }
        else if(lastFragment.equals(CompletedDownloadsFragment.TAG)){

            mBottomNavigationView.setSelectedItemId(R.id.action_download);
        }
    }

    /**
     * View.generateViewId stores the current ID in a static variable.
     * When the process is killed, the variable gets reset.
     * This makes sure that we do not get ID collisions
     * and therefore errors when trying to restore state from another view.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private void ensureGeneratedViewIdGreaterThan(int minimum) {
        while (View.generateViewId() <= minimum) {
            // Generate new IDs
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_GENERATED_VIEW_ID, View.generateViewId());
    }

    private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int state) {
                    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                        onSlide(view, 0.0f);
                    } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
                        onSlide(view, 1.0f);
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float slideOffset) {
                    AudioPlayerFragment audioPlayer = (AudioPlayerFragment) getSupportFragmentManager()
                            .findFragmentByTag(AudioPlayerFragment.TAG);
                    if (audioPlayer == null) {
                        return;
                    }

                    if (slideOffset == 0.0f) { //STATE_COLLAPSED
                        audioPlayer.scrollToPage(AudioPlayerFragment.POS_COVER);
                    }

                    float condensedSlideOffset = Math.max(0.0f, Math.min(0.2f, slideOffset - 0.2f)) / 0.2f;
                    audioPlayer.getExternalPlayerHolder().setAlpha(1 - condensedSlideOffset);
                    audioPlayer.getExternalPlayerHolder().setVisibility(
                            condensedSlideOffset > 0.99f ? View.GONE : View.VISIBLE);
                }
            };

    /*
    public void setupToolbarToggle(@NonNull Toolbar toolbar, boolean displayUpArrow) {
        if (drawerLayout != null) { // Tablet layout does not have a drawer
            if (drawerToggle != null) {
                drawerLayout.removeDrawerListener(drawerToggle);
            }
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
            drawerToggle.setDrawerIndicatorEnabled(!displayUpArrow);
            drawerToggle.setToolbarNavigationClickListener(v -> getSupportFragmentManager().popBackStack());
        } else if (!displayUpArrow) {
            toolbar.setNavigationIcon(null);
        } else {
            toolbar.setNavigationIcon(ThemeUtils.getDrawableFromAttr(this, R.attr.homeAsUpIndicator));
            toolbar.setNavigationOnClickListener(v -> getSupportFragmentManager().popBackStack());
        }
    }

     */

    private void checkFirstLaunch() {

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (prefs.getBoolean(PREF_IS_FIRST_LAUNCH, true)) {

            setSelectedPanel(AddFeedFragment.TAG);

            // for backward compatibility, we only change defaults for fresh installs
            UserPreferences.setUpdateInterval(12);
            AutoUpdateManager.restartUpdateAlarm(this);

            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(PREF_IS_FIRST_LAUNCH, false);
            edit.apply();
        }
    }


    public LockableBottomSheetBehavior getBottomSheet() {
        return sheetBehavior;
    }

    public void setPlayerVisible(boolean visible) {
        getBottomSheet().setLocked(!visible);
        FragmentContainerView mainView = findViewById(R.id.main_view);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainView.getLayoutParams();
        params.setMargins(0, 0, 0, visible ? (int) getResources().getDimension(R.dimen.external_player_height) : 0);
        mainView.setLayoutParams(params);
        findViewById(R.id.audioplayerFragment).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public RecyclerView.RecycledViewPool getRecycledViewPool() {
        return recycledViewPool;
    }

    public void loadFragment(String tag, Bundle args) {
        Log.d(TAG, "loadFragment(tag: " + tag + ", args: " + args + ")");
        Fragment fragment;
        switch (tag) {
            case QueueFragment.TAG:
                fragment = new QueueFragment();
                break;
            case InboxFragment.TAG:
                fragment = new InboxFragment();
                break;
            case EpisodesFragment.TAG:
                fragment = new EpisodesFragment();
                break;
            case CompletedDownloadsFragment.TAG:
                fragment = new CompletedDownloadsFragment();
                break;
            case PlaybackHistoryFragment.TAG:
                fragment = new PlaybackHistoryFragment();
                break;
            case AddFeedFragment.TAG:
                fragment = new AddFeedFragment();
                break;
            case SubscriptionFragment.TAG:
                fragment = new SubscriptionFragment();
                break;
            case FavoriteEpisodesFragment.TAG:
                fragment = new FavoriteEpisodesFragment();
                break;
            case MainPreferencesFragment.TAG:
                fragment = new MainPreferencesFragment();
                break;
            default:
                // default to the queue

                fragment = new QueueFragment();
                tag = QueueFragment.TAG;
                args = null;

                break;
        }

        if (args != null) {
            fragment.setArguments(args);
        }

        saveLastNavFragment(this, tag);

        loadFragment(fragment);
    }

    public void loadFeedFragmentById(long feedId, Bundle args) {

        Fragment fragment = FeedItemlistFragment.newInstance(feedId);

        if (args != null) {

            fragment.setArguments(args);
        }

        saveLastNavFragment(this, String.valueOf(feedId));

        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        // clear back stack
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.replace(R.id.main_view, fragment, MAIN_FRAGMENT_TAG);
        fragmentManager.popBackStack();
        // TODO: we have to allow state loss here
        // since this function can get called from an AsyncTask which
        // could be finishing after our app has already committed state
        // and is about to get shutdown.  What we *should* do is
        // not commit anything in an AsyncTask, but that's a bigger
        // change than we want now.
        t.commitAllowingStateLoss();
    }

    public void loadChildFragment(Fragment fragment, TransitionEffect transition) {
        Validate.notNull(fragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (transition) {
            case FADE:
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                break;
            case SLIDE:
                transaction.setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_left_out,
                        R.anim.slide_left_in,
                        R.anim.slide_right_out);
                break;
        }

        transaction
                .hide(getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG))
                .add(R.id.main_view, fragment, MAIN_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void loadChildFragment(Fragment fragment) {
        loadChildFragment(fragment, TransitionEffect.NONE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null) { // Tablet layout does not have a drawer
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) { // Tablet layout does not have a drawer
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }


    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        if (getBottomSheet().getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetCallback.onSlide(null, 1.0f);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //RatingDialog.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StorageUtils.checkStorageAvailability(this);
        handleNavIntent();
        //RatingDialog.check();

        if (lastTheme != UserPreferences.getNoTitleTheme()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lastTheme = UserPreferences.getNoTitleTheme(); // Don't recreate activity when a result is pending
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) { // Tablet layout does not have a drawer
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else if (getSupportFragmentManager().getBackStackEntryCount() != 0) {

            super.onBackPressed();
        }
        else {

            switch (UserPreferences.getBackButtonBehavior()) {

                case SHOW_PROMPT:

                    new AlertDialog.Builder(this)
                            .setMessage(R.string.close_prompt)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> MainActivity.super.onBackPressed())
                            .setNegativeButton(R.string.no, null)
                            .setCancelable(false)
                            .show();
                    break;

                case DOUBLE_TAP:

                    if (lastBackButtonPressTime < System.currentTimeMillis() - 2000) {

                        Toast.makeText(this, R.string.double_tap_toast, Toast.LENGTH_SHORT).show();
                        lastBackButtonPressTime = System.currentTimeMillis();
                    }
                    else {

                        super.onBackPressed();
                    }
                    break;

                case GO_TO_PAGE:

                    if (getLastNavFragment(this).equals(UserPreferences.getBackButtonGoToPage())) {

                        super.onBackPressed();
                    }
                    else {

                        loadFragment(UserPreferences.getBackButtonGoToPage(), null);
                    }
                    break;

                default: super.onBackPressed();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageEvent event) {
        Log.d(TAG, "onEvent(" + event + ")");

        Snackbar snackbar = showSnackbarAbovePlayer(event.message, Snackbar.LENGTH_SHORT);
        if (event.action != null) {
            snackbar.setAction(getString(R.string.undo), v -> event.action.run());
        }
    }

    private void handleNavIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FEED_ID) || intent.hasExtra(EXTRA_FRAGMENT_TAG) || intent.hasExtra(EXTRA_REFRESH_ON_START)) {
            Log.d(TAG, "handleNavIntent()");
            String tag = intent.getStringExtra(EXTRA_FRAGMENT_TAG);
            Bundle args = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS);
            boolean refreshOnStart = intent.getBooleanExtra(EXTRA_REFRESH_ON_START, false);
            if (refreshOnStart) {
                AutoUpdateManager.runImmediate(this);
            }

            long feedId = intent.getLongExtra(EXTRA_FEED_ID, 0);
            if (tag != null) {
                loadFragment(tag, args);
            } else if (feedId > 0) {
                boolean startedFromSearch = intent.getBooleanExtra(EXTRA_STARTED_FROM_SEARCH, false);
                boolean addToBackStack = intent.getBooleanExtra(EXTRA_ADD_TO_BACK_STACK, false);
                if (startedFromSearch || addToBackStack) {
                    loadChildFragment(FeedItemlistFragment.newInstance(feedId));
                } else {
                    loadFeedFragmentById(feedId, args);
                }
            }
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (intent.getBooleanExtra(MainActivityStarter.EXTRA_OPEN_PLAYER, false)) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetCallback.onSlide(null, 1.0f);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            handleDeeplink(intent.getData());
        }
        // to avoid handling the intent twice when the configuration changes
        setIntent(new Intent(MainActivity.this, MainActivity.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavIntent();
    }

    public Snackbar showSnackbarAbovePlayer(CharSequence text, int duration) {
        Snackbar s;
        if (getBottomSheet().getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            s = Snackbar.make(findViewById(R.id.main_view), text, duration);
            if (findViewById(R.id.audioplayerFragment).getVisibility() == View.VISIBLE) {
                s.setAnchorView(findViewById(R.id.audioplayerFragment));
            }
        } else {
            s = Snackbar.make(findViewById(android.R.id.content), text, duration);
        }
        s.show();
        return s;
    }

    public Snackbar showSnackbarAbovePlayer(int text, int duration) {
        return showSnackbarAbovePlayer(getResources().getText(text), duration);
    }

    /**
     * Handles the deep link incoming via App Actions.
     * Performs an in-app search or opens the relevant feature of the app
     * depending on the query.
     *
     * @param uri incoming deep link
     */
    private void handleDeeplink(Uri uri) {
        if (uri == null || uri.getPath() == null) {
            return;
        }
        Log.d(TAG, "Handling deeplink: " + uri.toString());
        switch (uri.getPath()) {
            case "/deeplink/search":
                String query = uri.getQueryParameter("query");
                if (query == null) {
                    return;
                }

                this.loadChildFragment(SearchFragment.newInstance(query));
                break;
            case "/deeplink/main":
                String feature = uri.getQueryParameter("page");
                if (feature == null) {
                    return;
                }
                switch (feature) {
                    case "DOWNLOADS":
                        loadFragment(CompletedDownloadsFragment.TAG, null);
                        break;
                    case "HISTORY":
                        loadFragment(PlaybackHistoryFragment.TAG, null);
                        break;
                    case "EPISODES":
                        loadFragment(EpisodesFragment.TAG, null);
                        break;
                    case "QUEUE":
                        loadFragment(QueueFragment.TAG, null);
                        break;
                    case "SUBSCRIPTIONS":
                        loadFragment(SubscriptionFragment.TAG, null);
                        break;
                    default:
                        showSnackbarAbovePlayer(getString(R.string.app_action_not_found, feature),
                                Snackbar.LENGTH_LONG);
                        return;
                }
                break;
            default:
                break;
        }
    }

    //Hardware keyboard support
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        View currentFocus = getCurrentFocus();
        if (currentFocus instanceof EditText) {
            return super.onKeyUp(keyCode, event);
        }

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Integer customKeyCode = null;
        EventBus.getDefault().post(event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_P:
                customKeyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                break;
            case KeyEvent.KEYCODE_J: //Fallthrough
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_COMMA:
                customKeyCode = KeyEvent.KEYCODE_MEDIA_REWIND;
                break;
            case KeyEvent.KEYCODE_K: //Fallthrough
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_PERIOD:
                customKeyCode = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
                break;
            case KeyEvent.KEYCODE_PLUS: //Fallthrough
            case KeyEvent.KEYCODE_W:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_MINUS: //Fallthrough
            case KeyEvent.KEYCODE_S:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_M:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI);
                    return true;
                }
                break;
        }

        if (customKeyCode != null) {
            Intent intent = new Intent(this, PlaybackService.class);
            intent.putExtra(MediaButtonReceiver.EXTRA_KEYCODE, customKeyCode);
            ContextCompat.startForegroundService(this, intent);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onSearchResultClicked(@NonNull SearchPreferenceResult result) {

        int screen = result.getResourceFile();
        if (screen == R.xml.feed_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.feed_settings_label);
            builder.setMessage(R.string.pref_feed_settings_dialog_msg);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        } else if (screen == R.xml.preferences_notifications) {
            openScreen(screen);
        } else {
            PreferenceFragmentCompat fragment = openScreen(result.getResourceFile());
            result.highlight(fragment);
        }
    }

    public PreferenceFragmentCompat openScreen(int screen) {

        PreferenceFragmentCompat fragment = getPreferenceScreen(screen);
        if (screen == R.xml.preferences_notifications && Build.VERSION.SDK_INT >= 26) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_view, fragment)
                    .addToBackStack(getString(getTitleOfPage(screen))).commit();
        }


        return fragment;
    }

    private PreferenceFragmentCompat getPreferenceScreen(int screen) {
        PreferenceFragmentCompat prefFragment = null;

        if (screen == R.xml.preferences_user_interface) {
            prefFragment = new UserInterfacePreferencesFragment();
        } else if (screen == R.xml.preferences_network) {
            prefFragment = new NetworkPreferencesFragment();
        } else if (screen == R.xml.preferences_storage) {
            prefFragment = new StoragePreferencesFragment();
        } else if (screen == R.xml.preferences_import_export) {
            prefFragment = new ImportExportPreferencesFragment();
        } else if (screen == R.xml.preferences_autodownload) {
            prefFragment = new AutoDownloadPreferencesFragment();
        } else if (screen == R.xml.preferences_synchronization) {
            prefFragment = new SynchronizationPreferencesFragment();
        } else if (screen == R.xml.preferences_playback) {
            prefFragment = new PlaybackPreferencesFragment();
        } else if (screen == R.xml.preferences_notifications) {
            prefFragment = new NotificationPreferencesFragment();
        } else if (screen == R.xml.preferences_swipe) {
            prefFragment = new SwipePreferencesFragment();
        }
        return prefFragment;
    }

    public static int getTitleOfPage(int preferences) {
        if (preferences == R.xml.preferences_network) {
            return R.string.network_pref;
        } else if (preferences == R.xml.preferences_autodownload) {
            return R.string.pref_automatic_download_title;
        } else if (preferences == R.xml.preferences_playback) {
            return R.string.playback_pref;
        } else if (preferences == R.xml.preferences_storage) {
            return R.string.storage_pref;
        } else if (preferences == R.xml.preferences_import_export) {
            return R.string.import_export_pref;
        } else if (preferences == R.xml.preferences_user_interface) {
            return R.string.user_interface_label;
        } else if (preferences == R.xml.preferences_synchronization) {
            return R.string.synchronization_pref;
        } else if (preferences == R.xml.preferences_notifications) {
            return R.string.notification_pref_fragment;
        } else if (preferences == R.xml.feed_settings) {
            return R.string.feed_settings_label;
        } else if (preferences == R.xml.preferences_swipe) {
            return R.string.swipeactions_label;
        }
        return R.string.settings_label;
    }


    public static final String PREF_LAST_FRAGMENT_TAG = "prefLastFragmentTag";
    private static final String PREF_OPEN_FOLDERS = "prefOpenFolders";
    public static final String[] NAV_DRAWER_TAGS = {
            QueueFragment.TAG,
            InboxFragment.TAG,
            EpisodesFragment.TAG,
            SubscriptionFragment.TAG,
            CompletedDownloadsFragment.TAG,
            PlaybackHistoryFragment.TAG,
            AddFeedFragment.TAG,
            NavListAdapter.SUBSCRIPTION_LIST_TAG
    };

    private static void saveLastNavFragment(Context context, String tag) {
        Log.d(TAG, "saveLastNavFragment(tag: " + tag + ")");
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        if (tag != null) {
            edit.putString(PREF_LAST_FRAGMENT_TAG, tag);
        } else {
            edit.remove(PREF_LAST_FRAGMENT_TAG);
        }
        edit.apply();
    }

    private static String getLastNavFragment(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastFragment = prefs.getString(PREF_LAST_FRAGMENT_TAG, QueueFragment.TAG);
        Log.d(TAG, "getLastNavFragment() -> " + lastFragment);
        return lastFragment;
    }
}
