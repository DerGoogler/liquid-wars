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
