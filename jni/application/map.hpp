//    This file is part of Liquid Wars.
//
//    Copyright (C) 2013 Henry Shepperd (hshepperd@gmail.com)
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
