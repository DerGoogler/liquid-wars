

package com.xenris.liquidwarsos.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.xenris.liquidwarsos.R;

public class InstructionsActivity extends AppCompatActivity {
    private static final String instructions1 = "Aim:\n Convert the enemy's army to your own colour to win.";
    private static final String instructions2 = "How to play:\n Wherever you touch the screen your army will run to.\n Use up to five fingers to guide your army.\n Enemy liquid will be converted to your own when they collide.\n Try to surround the enemy for the best effect.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.instructions);

        TextView tv;
        tv = (TextView)findViewById(R.id.instructions1);
        tv.setText(instructions1);
        tv = (TextView)findViewById(R.id.instructions2);
        tv.setText(instructions2);
    }
}
