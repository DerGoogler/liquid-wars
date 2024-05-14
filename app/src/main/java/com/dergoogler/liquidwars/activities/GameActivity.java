

package com.dergoogler.liquidwars.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Gravity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dergoogler.liquidwars.MyGLSurfaceView;
import com.dergoogler.liquidwars.MyRenderer;
import com.dergoogler.liquidwars.NativeInterface;
import com.dergoogler.liquidwars.R;
import com.dergoogler.liquidwars.StaticBits;
import com.dergoogler.liquidwars.Util;

import java.lang.Thread;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements Runnable, MyGLSurfaceView.SurfaceCallbacks {
    private MyGLSurfaceView myGLSurfaceView = null;
    private boolean running;
    private boolean paused;
    private short[][] xs = new short[6][5];
    private short[][] ys = new short[6][5];
    private Context context;
    private boolean gameFinished;
    private boolean lostGame;
    private int gameStep;
    private boolean frozen;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        // Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        super.onCreate(savedInstanceState);

        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        final int keepOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().addFlags(fullscreen | keepOn);

        setContentView(R.layout.game);
        myGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.mySurfaceView);
        myGLSurfaceView.setSurfaceCallbacks(this);

        Util.loadPlayerInitialPositions(xs, ys);

        NativeInterface.init(getAssets());
        NativeInterface.createGame(StaticBits.team, StaticBits.map, StaticBits.seed, StaticBits.dotsPerTeam);

        new Thread(this).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
        if (myGLSurfaceView != null)
            myGLSurfaceView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if (myGLSurfaceView != null)
            myGLSurfaceView.onResume();
    }

    @Override
    public void onDestroy() {
        if (!isFinishing())
            finish();
        super.onDestroy();
        running = false;
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void run() {
        running = true;
        gameFinished = false;
        lostGame = false;
        gameStep = 0;
        frozen = false;
        int aiStartDelay = 6;
        StaticBits.startTimestamp = System.currentTimeMillis();
        while (running) {
            if (!paused && !frozen) {
                stepGame();
                final int timeDiff = (int) (System.currentTimeMillis() - StaticBits.startTimestamp) / 1000;
                if (!gameFinished)
                    NativeInterface.setTimeSidebar((float) timeDiff / (float) StaticBits.timeLimit);
                checkTimeout(timeDiff);
                checkForWinner();
                checkIfLost();
                if (aiStartDelay-- < 0)
                    updateAI();
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }
        }
        NativeInterface.destroyGame();
        NativeInterface.uninit();
    }

    private void checkTimeout(int timeDiff) {
        if ((timeDiff >= StaticBits.timeLimit) && !gameFinished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    frozen = true;
                    gameFinished = true;
                    if (dialog != null)
                        dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    int winningTeam = 0;
                    for (int i = 0; i < 6; i++) {
                        int score = NativeInterface.teamScore(winningTeam);
                        int temp = NativeInterface.teamScore(i);
                        if (temp > score)
                            winningTeam = i;
                    }
                    if (winningTeam == StaticBits.team)
                        builder.setMessage("Out of time! You win!");
                    else
                        builder.setMessage("Out of time! " + Util.teamToNameString(winningTeam) + " wins!");
                    dialog = builder.show();
                    TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                    messageText.setTextColor(Util.teamToColour(winningTeam));
                    messageText.setGravity(Gravity.CENTER);
                    dialog.setCanceledOnTouchOutside(false);
                    Util.makeDialogCancelableIn(dialog, 1500);
                    Util.makeDialogDismissIn(dialog, 5000);
                }
            });
        }
    }

    private void checkIfLost() {
        if (lostGame)
            return;
        if (NativeInterface.teamScore(StaticBits.team) == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lostGame = true;
                    if (dialog != null)
                        dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("You Lose");
                    dialog = builder.show();
                    TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                    messageText.setGravity(Gravity.CENTER);
                    messageText.setTextColor(Util.teamToColour(StaticBits.team));
                    dialog.setCanceledOnTouchOutside(false);
                    Util.makeDialogCancelableIn(dialog, 1500);
                    Util.makeDialogDismissIn(dialog, 5000);
                }
            });
        }
    }

    private void checkForWinner() {
        if (gameFinished)
            return;
        for (int i = 0; i < 6; i++) {
            if (NativeInterface.teamScore(i) == StaticBits.NUMBER_OF_TEAMS * StaticBits.dotsPerTeam) {
                final int p = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameFinished = true;
                        if (dialog != null)
                            dialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        if (p == StaticBits.team)
                            builder.setMessage("You Win!");
                        else
                            builder.setMessage(Util.teamToNameString(p) + " Wins");
                        dialog = builder.show();
                        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                        messageText.setGravity(Gravity.CENTER);
                        messageText.setTextColor(Util.teamToColour(p));
                        dialog.setCanceledOnTouchOutside(false);
                        Util.makeDialogCancelableIn(dialog, 1500);
                        Util.makeDialogDismissIn(dialog, 5000);
                    }
                });
                break;
            }
        }
    }

    private void updateAI() {
        for (int p = 0; p < 6; p++) {
            if (p != StaticBits.team) {
                int nearestXY = NativeInterface.getNearestDot(p, xs[p][0], ys[p][0]);
                short nearestX = (short) (nearestXY >>> 16);
                short nearestY = (short) (nearestXY & 0x0000FFFF);
                xs[p][0] = nearestX;
                ys[p][0] = nearestY;
            }
        }
    }

    private void stepGame() {
        setPlayerPositions();
        for (int i = 0; i < 10; i++) {
            long previousTime = System.nanoTime();
            NativeInterface.stepDots();
            Util.regulateSpeed(previousTime, StaticBits.GAME_SPEED);
        }
        gameStep++;
    }

    private void setPlayerPositions() {
        for (int i = 0; i < 6; i++)
            NativeInterface.setPlayerPosition(i, xs[i], ys[i]);
    }

    @Override
    public void onTouch(MotionEvent event) {
        final int count = event.getPointerCount();
        for (int i = 0; i < 5; i++) {
            if (i < count) {
                xs[StaticBits.team][i] = (short) ((event.getX(i) / (float) MyRenderer.displayWidth) * (float) MyRenderer.WIDTH);
                ys[StaticBits.team][i] = (short) ((MyRenderer.HEIGHT - 1) - ((event.getY(i) / (float) MyRenderer.displayHeight) * (float) MyRenderer.HEIGHT));
            } else {
                xs[StaticBits.team][i] = -1;
                ys[StaticBits.team][i] = -1;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            final int upIndex = event.getActionIndex();
            final int upId = event.getPointerId(upIndex);
            xs[StaticBits.team][upId] = -1;
            ys[StaticBits.team][upId] = -1;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished && !lostGame) {
            DialogInterface.OnClickListener clicker = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };
            if (dialog != null)
                dialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Back to menu?");
            builder.setPositiveButton("Yes", clicker);
            builder.setNegativeButton("No", null);
            dialog = builder.show();
            TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        paused = !hasFocus;
    }
}
