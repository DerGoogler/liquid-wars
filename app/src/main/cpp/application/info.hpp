#ifndef INFO_HPP
#define INFO_HPP

#define WIDTH 800
#define HEIGHT 480
#define NUMBER_OF_TEAMS 6
#define TIMESTEP 8000
#define AI_UPDATE_SPEED 30
#define NUMBER_OF_MAPS 46

class Info {
    public:
        static short playerStartPositionX(unsigned char t);
        static short playerStartPositionY(unsigned char t);
};

#endif
