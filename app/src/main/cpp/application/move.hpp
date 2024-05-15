#ifndef MOVE_HPP
#define MOVE_HPP

#include "state.hpp"

class Move {
    private:
        static void moveDotToward(Dot* dot, const Player* player);
        static void turnClockwise(int dx, int dy, int* x, int* y);
        static void turnAnticlockwise(int dx, int dy, int* x, int* y);
    public:
        static void stepDots();
};

#endif
