//    This file is part of Liquid Wars.
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

#include "spiral.hpp"

void Spiral::getNextXY(int sx, int sy, int c, int* outx, int* outy) {
    const float a = 0.5;
    *outx = (int)(a*((float)c/50.0)*cos((float)c/50.0));
    *outy = (int)(a*((float)c/50.0)*sin((float)c/50.0));
    *outx += sx;
    *outy += sy;
}
