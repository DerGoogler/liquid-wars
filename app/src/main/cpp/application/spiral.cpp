#include "spiral.hpp"

void Spiral::getNextXY(int sx, int sy, int c, int* outx, int* outy) {
    const float a = 0.5;
    *outx = (int)(a*((float)c/50.0)*cos((float)c/50.0));
    *outy = (int)(a*((float)c/50.0)*sin((float)c/50.0));
    *outx += sx;
    *outy += sy;
}
