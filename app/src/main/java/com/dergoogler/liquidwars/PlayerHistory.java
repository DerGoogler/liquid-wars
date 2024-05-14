package com.dergoogler.liquidwars;

public class PlayerHistory {
    public final int MAX_HISTORY = 50;
    public short[][][] playerX = new short[MAX_HISTORY][6][5];
    public short[][][] playerY = new short[MAX_HISTORY][6][5];

    public int historyIndex = 0;
    //[historyIndex][player number][finger]

    public PlayerHistory() {
        for(int p = 0; p < 6; p++)
            for(int i = 0; i < 5; i++)
                playerY[historyIndex][p][i] = playerX[historyIndex][p][i] = -1;
        playerX[historyIndex][0][0] = 20;
        playerY[historyIndex][0][0] = 20;
        playerX[historyIndex][1][0] = 20;
        playerY[historyIndex][1][0] = MyRenderer.HEIGHT - 20;
        playerX[historyIndex][2][0] = MyRenderer.WIDTH / 2;
        playerY[historyIndex][2][0] = 20;
        playerX[historyIndex][3][0] = MyRenderer.WIDTH / 2;
        playerY[historyIndex][3][0] = MyRenderer.HEIGHT - 20;
        playerX[historyIndex][4][0] = MyRenderer.WIDTH - 20;
        playerY[historyIndex][4][0] = 20;
        playerX[historyIndex][5][0] = MyRenderer.WIDTH - 20;
        playerY[historyIndex][5][0] = MyRenderer.HEIGHT - 20;
    }

    public void savePlayerPositions(short[][] xs, short[][] ys) {
        historyIndex++;
        if(historyIndex >= MAX_HISTORY)
            historyIndex = 0;
        for(int p = 0; p < 6; p++) {
            for(int i = 0; i < 5; i++) {
                playerX[historyIndex][p][i] = xs[p][i];
                playerY[historyIndex][p][i] = ys[p][i];
            }
        }
    }

    public void serialiseCurrentPlayerState(int[] data, int offset) {
        for(int p = 0; p < 6; p++) {
            for(int xy = 0; xy < 5; xy++) {
                data[offset++] = playerX[historyIndex][p][xy];
                data[offset++] = playerY[historyIndex][p][xy];
            }
        }
    }

    public void serialiseHistoricalPlayerState(int stepsBack, int[] data, int offset) {
        final int index = getHistoryIndex(stepsBack);
        for(int p = 0; p < 6; p++) {
            for(int xy = 0; xy < 5; xy++) {
                data[offset++] = playerX[index][p][xy];
                data[offset++] = playerY[index][p][xy];
            }
        }
    }

    public int getHistoryIndex(int stepsBack) {
        if((stepsBack >= MAX_HISTORY) || (stepsBack < 0))
            return -1;
        if(stepsBack <= historyIndex)
            return (historyIndex - stepsBack);
        else
            return (MAX_HISTORY - (stepsBack - historyIndex));
    }
}
