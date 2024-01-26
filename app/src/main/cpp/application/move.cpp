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

#include "move.hpp"

void Move::stepDots() {
    for(int i = 0; i < NUMBER_OF_TEAMS*state->dotsPerTeam; i++) {
        Dot* dot = state->dots[i];
        const Player* player = &state->players[dot->team];
        moveDotToward(dot, player);
    }
}

void Move::moveDotToward(Dot* dot, const Player* player) {
    const short currentX = dot->x;
    const short currentY = dot->y;
    short playerX = player->x[0];
    short playerY = player->y[0];
    int diffX = playerX - currentX;
    int diffY = playerY - currentY;
    float dist = sqrt(diffX*diffX + diffY*diffY);

    for(int i = 1; i < 5; i++) {
        if(player->x[i] == -1)
            continue;
        const short nextPlayerX = player->x[i];
        const short nextPlayerY = player->y[i];
        const int nextDiffX = nextPlayerX - currentX;
        const int nextDiffY = nextPlayerY - currentY;
        const float nextDist = sqrt(nextDiffX*nextDiffX + nextDiffY*nextDiffY);
        if(nextDist < dist) {
            playerX = nextPlayerX;
            playerY = nextPlayerY;
            dist = nextDist;
            diffX = nextDiffX;
            diffY = nextDiffY;
        }
    }

    if(diffY == 0)
        diffY = 1;
    if(diffX == 0)
        diffX = 1;
    const float absDiffX = abs(diffX);
    const float absDiffY = abs(diffY);
    const float xy = 10*absDiffX/absDiffY;
    const float yx = 10*absDiffY/absDiffX;

    const float sum = absDiffX + absDiffY + 1 + 1;

    const int randomInt = state->moveRandom->next() % 10;

    const float r = sum * randomInt / 10.0;

    int dx = 0;
    int dy = 0;

    if(r < absDiffX) {
        if(diffX > 0)
            dx = 1;
        else
            dx = -1;
        if(r < (absDiffX/xy)) {
            if(diffY > 0)
            dy = 1;
        else
            dy = -1;
        }
    } else if(r < (absDiffX + absDiffY)) {
        if(diffY > 0)
            dy = 1;
        else
            dy = -1;
        if(r < (absDiffX + absDiffY/yx)) {
            if(diffX > 0)
            dx = 1;
        else
            dx = -1;
        }
    } else if(r < (absDiffX + absDiffY + 1)) {
        if(diffX > 0)
            dx = -1;
        else
            dx = 1;
        if(r < (absDiffX + absDiffY + 1/xy)) {
            if(diffY > 0)
            dy = -1;
        else
            dy = 1;
        }
    } else {
        if(diffY > 0)
            dy = -1;
        else
            dy = 1;
        if(r < (absDiffX + absDiffY + 1 + 1/yx)) {
            if(diffX > 0)
            dx = -1;
        else
            dx = 1;
        }
    }

    short nx = dot->x + dx;
    short ny = dot->y + dy;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny] == NULL)) {
        state->field[nx][ny] = dot;
        state->field[dot->x][dot->y] = NULL;
        dot->x = nx;
        dot->y = ny;
        return;
    }

    int d2x;
    int d2y;

    if((randomInt % 2) == 1)
        turnClockwise(dx, dy, &d2x, &d2y);
    else
        turnAnticlockwise(dx, dy, &d2x, &d2y);

    nx = dot->x + d2x;
    ny = dot->y + d2y;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny] == NULL)) {
        state->field[nx][ny] = dot;
        state->field[dot->x][dot->y] = NULL;
        dot->x = nx;
        dot->y = ny;
        return;
    }

    int d3x;
    int d3y;

    if((randomInt % 2) == 0)
        turnClockwise(dx, dy, &d3x, &d3y);
    else
        turnAnticlockwise(dx, dy, &d3x, &d3y);

    nx = dot->x + d3x;
    ny = dot->y + d3y;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny] == NULL)) {
        state->field[nx][ny] = dot;
        state->field[dot->x][dot->y] = NULL;
        dot->x = nx;
        dot->y = ny;
        return;
    }

    nx = dot->x + dx;
    ny = dot->y + dy;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny]->team != dot->team)) {
        state->field[nx][ny]->health--;
        if(state->field[nx][ny]->health <= 0) {
            state->players[state->field[nx][ny]->team].score--;
            state->players[dot->team].score++;
            state->field[nx][ny]->team = dot->team;
            state->field[nx][ny]->health = MAX_HEALTH/2;
        }
        return;
    }

    nx = dot->x + d2x;
    ny = dot->y + d2y;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny]->team != dot->team)) {
        state->field[nx][ny]->health--;
        if(state->field[nx][ny]->health <= 0) {
            state->players[state->field[nx][ny]->team].score--;
            state->players[dot->team].score++;
            state->field[nx][ny]->team = dot->team;
            state->field[nx][ny]->health = MAX_HEALTH/2;
        }
        return;
    }

    nx = dot->x + dx;
    ny = dot->y + dy;
    if(!state->map->isWall(nx, ny) && (state->field[nx][ny]->team == dot->team)) {
        if(state->field[nx][ny]->health < MAX_HEALTH) {
            if((state->moveRandom->next() % 100) > 95)
                state->field[nx][ny]->health++;
        }
        return;
    }
}

void Move::turnClockwise(int dx, int dy, int* x, int* y) {
    *x = 0;
    *y = 0;
    if(dx == 0) {
        if(dy == 1)
            *x = 1;
        else
            *x = -1;
        return;
    }
    if(dy == 0) {
        if(dx == 1)
            *y = -1;
        else
            *y = 1;
        return;
    }
    if(dx == 1) {
        if(dy == 1)
            *y = 0;
        else
            *x = 0;
        return;
    }
    if(dx == -1) {
        if(dy == 1)
            *x = 0;
        else
            *y = 0;
        return;
    }
}

void Move::turnAnticlockwise(int dx, int dy, int* x, int* y) {
    *x = 0;
    *y = 0;
    if(dx == 0) {
        if(dy == 1)
            *x = -1;
        else
            *x = 1;
        return;
    }
    if(dy == 0) {
        if(dx == 1)
            *y = 1;
        else
            *y = -1;
        return;
    }
    if(dx == 1) {
        if(dy == 1)
            *x = 0;
        else
            *y = 0;
        return;
    }
    if(dx == -1) {
        if(dy == 1)
            *y = 0;
        else
            *x = 0;
        return;
    }
}
