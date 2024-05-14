#ifndef STATE_HPP
#define STATE_HPP

#include <aclib.hpp>
#include <vector>
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
        std::vector<Dot*> dots;
        Dot* field[WIDTH][HEIGHT];
        std::vector<float> points;
        std::vector<float> colours;
        Random* moveRandom;
        Random* aiRandom;
        int dotsPerTeam;
        State(int team, int mapId, int seed, int dotsPerTeam);
        ~State();
        void placeTeams();
};

extern State* state;

#endif
