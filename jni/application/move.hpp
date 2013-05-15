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
