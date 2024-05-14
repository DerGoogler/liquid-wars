#include "dot.hpp"

Dot::Dot(unsigned short x, unsigned short y, unsigned char team) {
    this->x = x;
    this->y = y;
    this->team = team;
    this->health = 1;//MAX_HEALTH;
}

float Dot::getRed() {
    switch(team) {
        case 0: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 1: return 0.15*(float)(health+3)/MAX_HEALTH;
        case 2: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 3: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 4: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 5: return 1.0*(float)(health+3)/MAX_HEALTH;
    }
    return 1;
}

float Dot::getGreen() {
    switch(team) {
        case 0: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 1: return 0.15*(float)(health+3)/MAX_HEALTH;
        case 2: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 3: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 4: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 5: return 0.0*(float)(health+3)/MAX_HEALTH;
    }
    return 1;
}

float Dot::getBlue() {
    switch(team) {
        case 0: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 1: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 2: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 3: return 1.0*(float)(health+3)/MAX_HEALTH;
        case 4: return 0.0*(float)(health+3)/MAX_HEALTH;
        case 5: return 1.0*(float)(health+3)/MAX_HEALTH;
    }
    return 1;
}
