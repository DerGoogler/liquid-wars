package com.dergoogler.liquidwars.activities;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dergoogler.liquidwars.BuildConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class LiquidCompatActivity extends AppCompatActivity {
    private AdView adView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobileAds.initialize(this, initializationStatus -> {

        });
        this.adView = new AdView(this);
    }

    protected void hideSystemUI() {
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        // Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    }

    protected void keepOn() {
        final int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        final int keepOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().addFlags(fullscreen | keepOn);
    }

    protected void setAdsBanner(int id) {
        adView.setAdSize(new AdSize(AdSize.FULL_WIDTH, 50));
        adView.setAdUnitId(BuildConfig.BANNER_UNIT);
        LinearLayout layout = findViewById(id);
        layout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.adView.resume();
    }

    @Override
    public void onPause() {
        this.adView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        this.adView.destroy();
        super.onDestroy();
    }
}
