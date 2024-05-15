#include "random.hpp"

Random::Random(int seed) {
    this->seed = seed;
}

int Random::next() {
    seed = (214013*seed + 2531011);
    return (seed >> 16) & 0x7FFF;
}

void Random::setSeed(unsigned int s) {
    seed = s;
}

unsigned int Random::getSeed() {
    return seed;
}
