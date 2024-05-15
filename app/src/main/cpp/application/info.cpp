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
