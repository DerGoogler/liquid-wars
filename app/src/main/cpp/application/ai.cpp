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
