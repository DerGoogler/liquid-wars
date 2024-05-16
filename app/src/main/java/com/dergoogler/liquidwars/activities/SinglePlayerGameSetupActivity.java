

package com.dergoogler.liquidwars.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.graphics.drawable.Drawable;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.view.WindowCompat;

import com.dergoogler.liquidwars.R;
import com.dergoogler.liquidwars.StaticBits;
import com.dergoogler.liquidwars.activities.GameActivity;
import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.io.IOException;

public class SinglePlayerGameSetupActivity extends AppCompatActivity implements OnItemSelectedListener, OnLongClickListener {
    private Spinner teamSpinner;
    private Spinner mapSpinner;
    private Spinner timeoutSpinner;
    private Spinner teamSizeSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_game_setup);
        StaticBits.init();
        refreshMapImage();
        initSpinners();
        initButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        StaticBits.newSeed();
    }

    private void initSpinners() {
        ArrayAdapter<CharSequence> adapter;
        final int simpleSpinnerItem = android.R.layout.simple_spinner_item;

        teamSpinner = findViewById(R.id.team_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.teams_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);
        teamSpinner.setOnItemSelectedListener(this);

        mapSpinner = findViewById(R.id.map_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.maps_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapSpinner.setAdapter(adapter);
        mapSpinner.setOnItemSelectedListener(this);

        timeoutSpinner = findViewById(R.id.timeout_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.timeout_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutSpinner.setAdapter(adapter);
        timeoutSpinner.setOnItemSelectedListener(this);
        timeoutSpinner.setSelection(2);

        teamSizeSpinner = findViewById(R.id.teamsize_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.teamsize_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSizeSpinner.setAdapter(adapter);
        teamSizeSpinner.setOnItemSelectedListener(this);
        teamSizeSpinner.setSelection(2);
    }

    private void initButtons() {
        MaterialButton previousButton = findViewById(R.id.previous_button);
        previousButton.setOnLongClickListener(this);
        MaterialButton nextButton = findViewById(R.id.next_button);
        nextButton.setOnLongClickListener(this);
    }

    public void start(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
        final int id = view.getId();
        if(id == R.id.next_button) {
            int pos = mapSpinner.getSelectedItemPosition();
            pos += 20;
            if(pos > StaticBits.NUMBER_OF_MAPS)
                pos = StaticBits.NUMBER_OF_MAPS;
            mapSpinner.setSelection(pos);
        } else if(id == R.id.previous_button) {
            int pos = mapSpinner.getSelectedItemPosition();
            pos -= 20;
            if(pos < 0)
                pos = 0;
            mapSpinner.setSelection(pos);
        }
        return true;
    }

    public void nextMap(View view) {
        int pos = mapSpinner.getSelectedItemPosition();
        pos++;
        if(pos > StaticBits.NUMBER_OF_MAPS)
            pos = StaticBits.NUMBER_OF_MAPS;
        mapSpinner.setSelection(pos);
    }

    public void previousMap(View view) {
        int pos = mapSpinner.getSelectedItemPosition();
        pos--;
        if(pos < 0)
            pos = 0;
        mapSpinner.setSelection(pos);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        final int spinnerId = parent.getId();
        if(spinnerId == R.id.map_spinner) {
            StaticBits.map = pos - 1;
            refreshMapImage();
        } else if(spinnerId == R.id.team_spinner) {
            StaticBits.team = pos;
        } else if(spinnerId == R.id.timeout_spinner) {
            if(pos == 0)
                StaticBits.timeLimit = 30;
            else if(pos == 1)
                StaticBits.timeLimit = 60;
            else if(pos == 2)
                StaticBits.timeLimit = 60*2;
            else if(pos == 3)
                StaticBits.timeLimit = 60*3;
            else if(pos == 4)
                StaticBits.timeLimit = 60*5;
            else if(pos == 5)
                StaticBits.timeLimit = 60*10;
            else if(pos == 6)
                StaticBits.timeLimit = 60*60*24*23;
        } else if(spinnerId == R.id.teamsize_spinner) {
            if(view != null) {
                StaticBits.dotsPerTeam = Integer.parseInt(((TextView)view).getText() + "");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private void refreshMapImage() {
        InputStream is = null;
        try {
            if(StaticBits.map == -1)
                is = getAssets().open("maps/random-map.png");
            else
                is = getAssets().open("maps/" + StaticBits.map +"-image.png");
        } catch(IOException e) {
            try {
                is = getAssets().open("maps/" + StaticBits.map +"-map.png");
            } catch(IOException ex) { }
        }
        Drawable d = Drawable.createFromStream(is, null);
        ImageView iv = findViewById(R.id.map_imageview);
        iv.setImageDrawable(d);
    }
}
