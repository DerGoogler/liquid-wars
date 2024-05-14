#include "player.hpp"

#include "state.hpp"

Player::Player() {
    for(int i = 0; i < 5; i++)
        x[i] = y[i] = -1;
    score = 0;
}
