

package com.dergoogler.liquidwars.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.dergoogler.liquidwars.R;

public class InstructionsActivity extends LiquidCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
    }
}
