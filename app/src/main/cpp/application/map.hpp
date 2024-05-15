#ifndef MAP_HPP
#define MAP_HPP

#include <aclib.hpp>
#include <GLES/gl.h>
#include <string>
#include <sstream>
#include "info.hpp"

class Map {
    private:
        unsigned int* imageMapData;
        unsigned int imageMapWidth;
        unsigned int imageMapHeight;
        float imageMapWidthRatio;
        float imageMapHeightRatio;
        GLuint glTextureID;
        std::string path;
    public:
        Map(int mapID);
        ~Map();
        void loadTexture();
        void draw();
        bool isWall(int width, int height);
        unsigned int alpha(unsigned int pixel);
        void createPOTImage(unsigned int* image, unsigned int width, unsigned int height, unsigned int** outImage, unsigned int* outWidth, unsigned int* outHeight);
};

#endif
