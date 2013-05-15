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

#include "main.hpp"

void createGame(int team, int map, int seed) {
    state = new State(team, map, seed);
}

void destroyGame() {
    if(state == NULL)
        return;

    while(state->currentlyDrawing)
        usleep(20000);
    delete(state);
    state = NULL;
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
