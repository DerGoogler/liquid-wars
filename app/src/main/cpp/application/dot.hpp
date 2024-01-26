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
