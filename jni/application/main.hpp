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

#ifndef MAIN_HPP
#define MAIN_HPP

#include <GLES/gl.h>
#include <aclib.hpp>
#include <unistd.h>
#include <aclib.hpp>
#include <string>
#include "state.hpp"
#include "ai.hpp"
#include "move.hpp"

void createGame(int team, int map, int seed, int dotsPerTeam);
void destroyGame();
void stepDots();
void setPlayerPosition(int team, short* x, short* y);
int getNearestDot(int p, short px, short py);
int teamScore(int p);
void setTimeSidebar(float t);

#endif
