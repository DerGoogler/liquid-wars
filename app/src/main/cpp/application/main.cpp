#include "main.hpp"

void createGame(int team, int map, int seed, int dotsPerTeam) {
    state = new State(team, map, seed, dotsPerTeam);
}

void destroyGame() {
    if(state == NULL)
        return;

    while(state->currentlyDrawing)
        usleep(200);
    State* s = state;
    state = NULL;
    delete(s);
}

void stepDots() {
    if(state != NULL)
        Move::stepDots();
}

void setPlayerPosition(int team, short* x, short* y) {
    if(state != NULL) {
        for(int i = 0; i < 5; i++) {
            state->players[team].x[i] = x[i];
            state->players[team].y[i] = y[i];
        }
    }
}

int getNearestDot(int p, short px, short py) {
    if(state != NULL)
        return AI::getNearestDot(p, px, py);
    else
        return 0;
}

int teamScore(int p) {
    if(state != NULL)
        return state->players[p].score;
    else
        return 0;
}

void setTimeSidebar(float t) {
    if(state != NULL)
        state->timeSidebar = t;
}
