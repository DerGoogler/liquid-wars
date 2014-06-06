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

#ifndef INFO_HPP
#define INFO_HPP

#define WIDTH 800
#define HEIGHT 480
#define NUMBER_OF_TEAMS 6
#define TIMESTEP 8000
#define AI_UPDATE_SPEED 30
#define NUMBER_OF_MAPS 50

class Info {
    public:
        static short playerStartPositionX(unsigned char t);
        static short playerStartPositionY(unsigned char t);
};

#endif
