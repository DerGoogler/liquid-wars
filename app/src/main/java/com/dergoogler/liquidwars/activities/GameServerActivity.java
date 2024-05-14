

package com.dergoogler.liquidwars.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.Gravity;
import android.widget.TextView;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.dergoogler.liquidwars.MyGLSurfaceView;
import com.dergoogler.liquidwars.MyRenderer;
import com.dergoogler.liquidwars.NativeInterface;
import com.dergoogler.liquidwars.PlayerHistory;
import com.dergoogler.liquidwars.R;
import com.dergoogler.liquidwars.server.Server;
import com.dergoogler.liquidwars.StaticBits;
import com.dergoogler.liquidwars.Util;

import java.lang.Thread;

public class GameServerActivity extends AppCompatActivity implements Server.ServerCallbacks, Runnable, MyGLSurfaceView.SurfaceCallbacks {
    private MyGLSurfaceView myGLSurfaceView = null;
    private boolean running;
    private int gameStep = 0;
    private int[] clientLags = new int[6];
    private PlayerHistory playerHistory = new PlayerHistory();
    private int playerWithMissedStepsId = -1;
    private int playerWithMissedStepsStep = 0;
    private short[][] xs = new short[6][5];
    private short[][] ys = new short[6][5];
    private boolean[] ready = new boolean[6];
    private Context context;
    private boolean gameFinished;
    private boolean lostGame;
    private boolean frozen;
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

        Util.loadPlayerInitialPositions(xs, ys);

        createReadyList();

        for(int i = 0; i < clientLags.length; i++)
            clientLags[i] = 0;

        NativeInterface.init(getAssets());
        NativeInterface.createGame(StaticBits.team, StaticBits.map, StaticBits.seed, StaticBits.dotsPerTeam);

        StaticBits.server.setCallbacks(this);

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
        StaticBits.server.setCallbacks(StaticBits.multiplayerGameSetupActivity);
        running = false;
        if(dialog != null)
            dialog.dismiss();
    }

    @Override
    public void run() {
        running = true;
        gameFinished = false;
        lostGame = false;
        frozen = false;
        waitForEveryoneToBeReady();
        StaticBits.startTimestamp = System.currentTimeMillis();
        int aiStartDelay = 6;
        while(running) {
            playerHistory.savePlayerPositions(xs, ys);
            if(!frozen) {
                sendStepData();
                stepGame();
                final int timeDiff = (int)(System.currentTimeMillis() - StaticBits.startTimestamp)/1000;
                if(!gameFinished) {
                    StaticBits.server.sendToAll(StaticBits.TIME_DIFF, timeDiff);
                    NativeInterface.setTimeSidebar((float)timeDiff/(float)StaticBits.timeLimit);
                }
                checkTimeout(timeDiff);
                checkForWinner();
                checkIfLost();
                if(aiStartDelay-- < 0)
                    updateAI();
                resendAnyLostSteps();
            }
            waitForSlowClients();
        }
        NativeInterface.destroyGame();
        NativeInterface.uninit();
        finish();
    }

    private void checkTimeout(int timeDiff) {
        if((timeDiff >= StaticBits.timeLimit) && !gameFinished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    frozen = true;
                    gameFinished = true;
                    if(dialog != null)
                        dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    int winningTeam = 0;
                    for(int i = 0; i < 6; i++) {
                        int score = NativeInterface.teamScore(winningTeam);
                        int temp = NativeInterface.teamScore(i);
                        if(temp > score)
                            winningTeam = i;
                    }
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
                    StaticBits.server.sendToAll(StaticBits.OUT_OF_TIME, winningTeam);
                }
            });
        }
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

    private void updateAI() {
        for(int p = 0; p < 6; p++) {
            if(StaticBits.teams[p] == StaticBits.AI_PLAYER) {
                int nearestXY = NativeInterface.getNearestDot(p, xs[p][0], ys[p][0]);
                short nearestX = (short)(nearestXY >>> 16);
                short nearestY = (short)(nearestXY & 0x0000FFFF);
                xs[p][0] = nearestX;
                ys[p][0] = nearestY;
            }
        }
    }

    private void waitForEveryoneToBeReady() {
        if((StaticBits.server == null) || (ready == null))
            return;

        int countdown = 10;
        while(true) {
            if(ready[0] && ready[1] && ready[2] && ready[3] && ready[4] && ready[5])
                break;
            if(countdown-- < 0) {
                StaticBits.server.sendToAll(StaticBits.CLIENT_READY_QUERY, 0);
                countdown = 10;
            }
            try { Thread.sleep(10); } catch (InterruptedException ie) { }
        }
    }

    private void createReadyList() {
        for(int i = 0; i < 6; i++) {
            if((StaticBits.teams[i] == StaticBits.AI_PLAYER) || (StaticBits.teams[i] == 0))
                ready[i] = true;
            else
                ready[i] = false;
        }

    }

    private void sendStepData() {
        int[] data = new int[3 + 6 * 5 * 2];
        data[0] = StaticBits.STEP_GAME;
        data[1] = StaticBits.REGULATED_STEP;
        data[2] = gameStep;
        playerHistory.serialiseCurrentPlayerState(data, 3);
        StaticBits.server.sendToAll(data.length, data);
    }

    private void resendAnyLostSteps() {
        int[] data = new int[3 + 6 * 5 * 2];
        if(playerWithMissedStepsId == -1)
            return;
        data[0] = StaticBits.STEP_GAME;
        data[1] = StaticBits.FAST_STEP;
        for(int i = playerWithMissedStepsStep; i <= gameStep; i++) {
            data[2] = i;
            playerHistory.serialiseHistoricalPlayerState(gameStep - i, data, 3);
            StaticBits.server.sendToOne(playerWithMissedStepsId, data.length, data);
        }
        playerWithMissedStepsId = -1;
    }

    private void waitForSlowClients() {
        if(playerWithMissedStepsId != -1)
            return;
        int biggestIndex = 0;
        int biggestValue = 0;
        for(int i = 0; i < 6; i++) {
            if(clientLags[i] > biggestValue) {
                biggestIndex = i;
                biggestValue = clientLags[i];
            }
        }
        biggestValue = biggestValue*biggestValue*biggestValue;
        for(int i = 0; i < biggestValue; i++) {
            try { Thread.sleep(1); } catch (InterruptedException ie) { }
            if(clientLags[biggestIndex] < 4)
                break;
        }
    }

    private void stepGame() {
        setPlayerPositions();
        for(int i = 0; i < 10; i++) {
            long previousTime = System.nanoTime();
            NativeInterface.stepDots();
            Util.regulateSpeed(previousTime, StaticBits.GAME_SPEED);
        }
        gameStep++;
    }

    private void setPlayerPositions() {
        for(int i = 0; i < 6; i++) {
            short[] tempxs = playerHistory.playerX[playerHistory.historyIndex][i];
            short[] tempys = playerHistory.playerY[playerHistory.historyIndex][i];
            NativeInterface.setPlayerPosition(i, tempxs, tempys);
        }
    }

    @Override
    public void onTouch(MotionEvent event) {
        final int count = event.getPointerCount();
        final int p = clientIdToPlayerNumber(0);
        for(int i = 0; i < 5; i++) {
            if(i < count) {
                xs[p][i] = (short)((event.getX(i) / (float) MyRenderer.displayWidth) * (float)MyRenderer.WIDTH);
                ys[p][i] = (short)((MyRenderer.HEIGHT-1) - ((event.getY(i) / (float)MyRenderer.displayHeight) * (float)MyRenderer.HEIGHT));
            } else {
                xs[p][i] = -1;
                ys[p][i] = -1;
            }
        }

        if(event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            final int upIndex = event.getActionIndex();
            xs[p][upIndex] = -1;
            ys[p][upIndex] = -1;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished) {
            DialogInterface.OnClickListener clicker = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    StaticBits.server.sendToAll(StaticBits.KILL_GAME, 0);
                    finish();
                    running = false;
                }
            };
            if(dialog != null)
                dialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Game is still in play! Back to menu?");
            builder.setPositiveButton("Yes", clicker);
            builder.setNegativeButton("No", null);
            dialog = builder.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            return true;
        }
        else {
            StaticBits.server.sendToAll(StaticBits.BACK_TO_MENU, 0);
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClientMessageReceived(int id, int argc, int[] args) {
        if(args[0] == StaticBits.RESEND_STEPS) {
            if(playerWithMissedStepsId == -1)
                noteMissedSteps(id, args[1]);
        } else if(args[0] == StaticBits.PLAYER_POSITION_DATA) {
            final int p = clientIdToPlayerNumber(id);
            for(int i = 0; i < 5; i++) {
                xs[p][i] = (short)args[i+1];
                ys[p][i] = (short)args[i+5+1];
            }
        } else if(args[0] == StaticBits.CLIENT_CURRENT_GAMESTEP) {
            final int p = clientIdToPlayerNumber(id);
            final int clientGameStep = args[1];
            clientLags[p] = gameStep - clientGameStep;
        } else if(args[0] == StaticBits.CLIENT_READY) {
            final int p = clientIdToPlayerNumber(id);
            ready[p] = true;
        } else if(args[0] == StaticBits.CLIENT_EXIT) {
            final int p = clientIdToPlayerNumber(id);
            if((p >= 0) && (p < 6))
                clientLags[p] = 0;
        }
    }

    @Override
    public void onClientConnected(int id) {
    }

    @Override
    public void onClientDisconnected(int id) {
        final int p = clientIdToPlayerNumber(id);
        if((p >= 0) && (p < 6))
            clientLags[p] = 0;
        StaticBits.multiplayerGameSetupActivity.onClientDisconnected(id);
        ready[p] = true;
    }

    private void noteMissedSteps(int id, int step) {
        playerWithMissedStepsStep = step;
        playerWithMissedStepsId = id;
    }

    private int clientIdToPlayerNumber(int id) {
        for(int i = 0; i < 6; i++)
            if(StaticBits.teams[i] == id)
                return i;
        return -1;
    }
}
