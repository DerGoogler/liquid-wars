#ifndef RANDOM_HPP
#define RANDOM_HPP

class Random {
    private:
        unsigned int seed;

    public:
        Random(int seed);
        int next();
        void setSeed(unsigned int s);
        unsigned int getSeed();
};

#endif
