#ifndef DOT_HPP
#define DOT_HPP

#define MAX_HEALTH 10

class Dot {
    public:
        unsigned short x;
        unsigned short y;
        unsigned char team;
        unsigned char health;
        Dot(unsigned short x, unsigned short y, unsigned char team);
        float getRed();
        float getGreen();
        float getBlue();
};

#endif
