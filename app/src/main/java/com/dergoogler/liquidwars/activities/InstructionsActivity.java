

package com.dergoogler.liquidwars.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.dergoogler.liquidwars.R;

public class InstructionsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.instructions);
    }
}
