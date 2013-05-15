//    This file is part of Liquid Wars.
//
//    Copyright (C) 2013 Henry Shepperd (hshepperd@gmail.com)
//
//    Liquid Wars is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquid Wars is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Liquid Wars.  If not, see <http://www.gnu.org/licenses/>.

package com.xenris.liquidwarsos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.graphics.drawable.Drawable;
import android.content.Intent;
import java.io.InputStream;
import java.io.IOException;

public class SinglePlayerGameSetupActivity extends Activity implements OnItemSelectedListener, OnLongClickListener {
    private Spinner teamSpinner;
    private Spinner mapSpinner;
    private Spinner timeoutSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        teamSpinner = (Spinner)findViewById(R.id.team_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.teams_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);
        teamSpinner.setOnItemSelectedListener(this);

        mapSpinner = (Spinner)findViewById(R.id.map_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.maps_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapSpinner.setAdapter(adapter);
        mapSpinner.setOnItemSelectedListener(this);

        timeoutSpinner = (Spinner)findViewById(R.id.timeout_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.timeout_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutSpinner.setAdapter(adapter);
        timeoutSpinner.setOnItemSelectedListener(this);
        timeoutSpinner.setSelection(2);
    }

    private void initButtons() {
        Button previousButton = (Button)findViewById(R.id.previous_button);
        previousButton.setOnLongClickListener(this);
        Button nextButton = (Button)findViewById(R.id.next_button);
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
        ImageView iv = (ImageView)findViewById(R.id.map_imageview);
        iv.setImageDrawable(d);
    }
}
