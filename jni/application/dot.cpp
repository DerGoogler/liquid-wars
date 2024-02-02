//    This file is part of Liquid Wars.
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
