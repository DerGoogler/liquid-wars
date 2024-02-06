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

#include "state.hpp"

State* state;

State::State(int team, int mapId, int seed, int dotsPerTeam) {
    me = team;

    currentlyDrawing = false;

    timeSidebar = 0;

    this->dotsPerTeam = dotsPerTeam;

    for(int i = 0; i < 6; i++)
        players[i].score = dotsPerTeam;

    dots.resize(NUMBER_OF_TEAMS*dotsPerTeam);
    points.resize(NUMBER_OF_TEAMS*dotsPerTeam*3);
    colours.resize(NUMBER_OF_TEAMS*dotsPerTeam*4);

    for(int w = 0; w < WIDTH; w++)
        for(int h = 0; h < HEIGHT; h++)
            field[w][h] = NULL;

    for(int i = 0; i < NUMBER_OF_TEAMS*dotsPerTeam*4; i++)
        colours[i] = 1;

    moveRandom = new Random(seed);
    aiRandom = new Random(seed);

    if(mapId == -1)
        map = new Map(aiRandom->next() % NUMBER_OF_MAPS);
    else
        map = new Map(mapId);

    placeTeams();
}

State::~State() {
    for(int i = 0; i < NUMBER_OF_TEAMS*dotsPerTeam; i++)
        delete(dots[i]);
    delete(map);
    delete(moveRandom);
    delete(aiRandom);
}

void State::placeTeams() {
    for(int t = 0; t < NUMBER_OF_TEAMS; t++) {
        int sx = Info::playerStartPositionX(t);
        int sy = Info::playerStartPositionY(t);
        int x = sx;
        int y = sy;
        int c = 0;
        for(int i = 0; i < dotsPerTeam; i++) {
            while(map->isWall(x, y) || (field[x][y] != NULL))
                Spiral::getNextXY(sx, sy, ++c, &x, &y);
            Dot* dot = new Dot(x, y, t);
            dots[t*dotsPerTeam + i] = dot;
            field[x][y] = dot;
        }
    }

    for(int i = 0; i < NUMBER_OF_TEAMS; i++) {
        players[i].x[0] = Info::playerStartPositionX(i);
        players[i].y[0] = Info::playerStartPositionY(i);
    }
}
