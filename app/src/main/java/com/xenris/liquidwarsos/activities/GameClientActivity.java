

package com.xenris.liquidwarsos.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.xenris.liquidwarsos.Client;
import com.xenris.liquidwarsos.MyGLSurfaceView;
import com.xenris.liquidwarsos.MyRenderer;
import com.xenris.liquidwarsos.NativeInterface;
import com.xenris.liquidwarsos.R;
import com.xenris.liquidwarsos.StaticBits;
import com.xenris.liquidwarsos.Util;

import java.lang.Thread;
import java.util.ArrayList;

public class GameClientActivity extends AppCompatActivity implements Client.ClientCallbacks, Runnable, MyGLSurfaceView.SurfaceCallbacks {
    private MyGLSurfaceView myGLSurfaceView = null;
    private boolean running;
    private int gameStep = -1;
    private short[] xs = new short[5];
    private short[] ys = new short[5];
    private int touchReduction = 5;
    private Context context;
    private boolean usingNativeStateLock = false;
    private boolean gameFinished;
    private boolean lostGame;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        final int keepOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().addFlags(fullscreen | keepOn);

        setContentView(R.layout.game);
        myGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.mySurfaceView);
        myGLSurfaceView.setSurfaceCallbacks(this);

        NativeInterface.init(getAssets());
        NativeInterface.createGame(StaticBits.team, StaticBits.map, StaticBits.seed, StaticBits.dotsPerTeam);

        StaticBits.client.setCallbacks(this);

        new Thread(this).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(myGLSurfaceView != null)
            myGLSurfaceView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(myGLSurfaceView != null)
            myGLSurfaceView.onResume();
    }

    @Override
    public void onDestroy() {
        if(!isFinishing())
            finish();
        super.onDestroy();
        if(StaticBits.client != null)
            StaticBits.client.setCallbacks(StaticBits.clientGameSetupActivity);
        if(myGLSurfaceView != null)
            myGLSurfaceView.onPause();
        running = false;
        if(dialog != null)
            dialog.dismiss();
    }

    @Override
    public void run() {
        running = true;
        gameFinished = false;
        lostGame = false;
        StaticBits.client.send(StaticBits.CLIENT_READY, 0);
        while(running) {
            StaticBits.client.send(StaticBits.CLIENT_CURRENT_GAMESTEP, gameStep);
            checkForWinner();
            checkIfLost();
            try { Thread.sleep(50); } catch (InterruptedException ie) { }
        }
        while(usingNativeStateLock)
            try { Thread.sleep(3); } catch (InterruptedException ie) { }
        NativeInterface.destroyGame();
        NativeInterface.uninit();
        StaticBits.client.send(StaticBits.CLIENT_EXIT, 0);
        finish();
    }

    private void checkIfLost() {
        if(lostGame)
            return;
        if(NativeInterface.teamScore(StaticBits.team) == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lostGame = true;
                    if(dialog != null)
                        dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("You Lose");
                    dialog = builder.show();
                    TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
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
        if(gameFinished)
            return;
        for(int i = 0; i < 6; i++) {
            if(NativeInterface.teamScore(i) == StaticBits.NUMBER_OF_TEAMS*StaticBits.dotsPerTeam) {
                final int p = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameFinished = true;
                        if(dialog != null)
                            dialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        if(p == StaticBits.team)
                            builder.setMessage("You Win!");
                        else
                            builder.setMessage(Util.teamToNameString(p) + " Wins");
                        dialog = builder.show();
                        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
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

    private void sendPlayerData() {
        int[] data = new int[5 + 5 + 1];
        for(int i = 0; i < 5; i++) {
            data[i+1] = xs[i];
            data[i+5+1] = ys[i];
        }
        data[0] = StaticBits.PLAYER_POSITION_DATA;
        StaticBits.client.send(data.length, data);
    }

    @Override
    public void onTouch(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_MOVE)
            if(touchReduction-- != 0)
                return;
            else
                touchReduction = 5;

        final int count = event.getPointerCount();
        for(int i = 0; i < 5; i++) {
            if(i < count) {
                xs[i] = (short)((event.getX(i) / (float) MyRenderer.displayWidth) * (float)MyRenderer.WIDTH);
                ys[i] = (short)((MyRenderer.HEIGHT-1) - ((event.getY(i) / (float)MyRenderer.displayHeight) * (float)MyRenderer.HEIGHT));
            } else {
                xs[i] = -1;
                ys[i] = -1;
            }
        }

        if(event.getAction() == MotionEvent.ACTION_POINTER_UP) { //XXX this appears to be not working.
            final int upIndex = event.getActionIndex();
            final int upId = event.getPointerId(upIndex);
            xs[upId] = -1;
            ys[upId] = -1;
        }
        sendPlayerData();
//        android.util.Log.i("mylog", xs[1] + " " + ys[1]);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished) {
            DialogInterface.OnClickListener clicker = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    running = false;
                }
            };
            if(dialog != null)
                dialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Back to menu?");
            builder.setPositiveButton("Yes", clicker);
            builder.setNegativeButton("No", null);
            dialog = builder.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onServerMessageReceived(int argc, int[] args) {
        if(args[0] == StaticBits.STEP_GAME) {
            if(running) {
                if(args[2] == (gameStep + 1)) {
                    loadPlayerPositions(args, 3);
                    stepGame(args[1]);
                } else if(args[2] > (gameStep + 1)) {
                    StaticBits.client.send(StaticBits.RESEND_STEPS, gameStep + 1);
                }
            }
        } else if(args[0] == StaticBits.CLIENT_READY_QUERY) {
            if(running)
                StaticBits.client.send(StaticBits.CLIENT_READY, 0);
        } else if(args[0] == StaticBits.KILL_GAME) {
            ToastOnUiThread("Game canceled");
            running = false;
        } else if(args[0] == StaticBits.BACK_TO_MENU) {
            running = false;
        } else if(args[0] == StaticBits.OUT_OF_TIME) {
            outOfTimeMessage(args[1]);
        } else if(args[0] == StaticBits.TIME_DIFF) {
            NativeInterface.setTimeSidebar((float)args[1]/(float)StaticBits.timeLimit);
        }
    }

    @Override
    public void onServerConnectionMade(int id, String ip) {
    }

    @Override
    public void onServerConnectionFailed(String ip) {
        StaticBits.gameWasDisconnected = true;
        ToastOnUiThread("Connection lost");
        running = false;
    }

    @Override
    public void onServerConnectionClosed(String ip) {
        StaticBits.gameWasDisconnected = true;
        ToastOnUiThread("Connection lost");
        running = false;
    }

    private void ToastOnUiThread(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void outOfTimeMessage(final int winningTeam) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameFinished = true;
                if(dialog != null)
                    dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if(winningTeam == StaticBits.team)
                    builder.setMessage("Out of time! You win!");
                else
                    builder.setMessage("Out of time! " + Util.teamToNameString(winningTeam) + " wins!");
                dialog = builder.show();
                TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
                messageText.setTextColor(Util.teamToColour(winningTeam));
                messageText.setGravity(Gravity.CENTER);
                dialog.setCanceledOnTouchOutside(false);
                Util.makeDialogCancelableIn(dialog, 1500);
                Util.makeDialogDismissIn(dialog, 5000);
            }
        });
    }

    private void loadPlayerPositions(int[] data, int offset) {
        short[] tempxs = new short[5];
        short[] tempys = new short[5];
        for(int p = 0; p < 6; p++) {
            for(int xy = 0; xy < 5; xy++) {
                tempxs[xy] = (short)data[offset++];
                tempys[xy] = (short)data[offset++];
            }
            NativeInterface.setPlayerPosition(p, tempxs, tempys);
        }
    }

    private void stepGame(int speed) {
        if(usingNativeStateLock)
            return;
        usingNativeStateLock = true;
        for(int i = 0; i < 10; i++) {
            long previousTime = System.nanoTime();
            NativeInterface.stepDots();
            if(speed == StaticBits.REGULATED_STEP)
                Util.regulateSpeed(previousTime, StaticBits.GAME_SPEED);
        }
        usingNativeStateLock = false;
        gameStep++;
    }
}
