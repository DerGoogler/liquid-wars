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

#include "info.hpp"

#define YDIS 50
#define XDIS 20

short Info::playerStartPositionX(unsigned char t) {
    switch(t) {
        case 0: return XDIS;
        case 1: return XDIS;
        case 2: return WIDTH/2;
        case 3: return WIDTH/2;
        case 4: return WIDTH - XDIS;
        case 5: return WIDTH - XDIS;
    }
    return 0;
}

short Info::playerStartPositionY(unsigned char t) {
    switch(t) {
        case 0: return YDIS;
        case 1: return HEIGHT - YDIS;
        case 2: return YDIS;
        case 3: return HEIGHT - YDIS;
        case 4: return YDIS;
        case 5: return HEIGHT - YDIS;
    }
    return 0;
}
