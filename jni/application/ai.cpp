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

#include "ai.hpp"

int AI::getNearestDot(int p, short px, short py) {
    int nearestX = 0;
    int nearestY = 0;
    float dist = 10000;
    for(int i = 0; i < NUMBER_OF_TEAMS*state->dotsPerTeam; i++) {
        if(state->dots[i]->team == p)
            continue;
        const int tempx = state->dots[i]->x;
        const int tempy = state->dots[i]->y;
        const int tempDiffx = tempx - px;
        const int tempDiffy = tempy - py;
        const float tempDist = sqrt(tempDiffx*tempDiffx + tempDiffy*tempDiffy);
        if(tempDist < dist) {
            nearestX = tempx;
            nearestY = tempy;
            dist = tempDist;
        }
    }
    if(dist != 10000) {
        nearestX += (state->aiRandom->next() % 11) - 5;
        nearestY += (state->aiRandom->next() % 11) - 5;
    } else {
        nearestX = px;
        nearestY = py;
    }
    return (nearestX << 16) | nearestY;
}
