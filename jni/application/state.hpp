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

#ifndef STATE_HPP
#define STATE_HPP

#include <aclib.hpp>
#include "map.hpp"
#include "dot.hpp"
#include "player.hpp"
#include "info.hpp"
#include "random.hpp"
#include "spiral.hpp"

class State {
    public:
        int me;
        bool currentlyDrawing;
        float timeSidebar;
        int displayWidth;
        int displayHeight;
        Player players[NUMBER_OF_TEAMS];
        Map* map;
        Dot* dots[NUMBER_OF_TEAMS*DOTS_PER_TEAM];
        Dot* field[WIDTH][HEIGHT];
        float points[NUMBER_OF_TEAMS*DOTS_PER_TEAM*3];
        float colours[NUMBER_OF_TEAMS*DOTS_PER_TEAM*4];
        Random* moveRandom;
        Random* aiRandom;
        State(int team, int mapId, int seed);
        ~State();
        void placeTeams();
};

extern State* state;

#endif
