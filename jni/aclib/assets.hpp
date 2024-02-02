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

#ifndef ASSETS_HPP
#define ASSETS_HPP

#include <android/asset_manager_jni.h>
#include <stdlib.h>

class ACAssets {
    private:
        AAssetManager* assetManager;
    public:
        ACAssets(AAssetManager* am);
        unsigned char* getFile(const char* fileName, int* size);
        unsigned int getFileSize(const char* fileName);
        AAssetManager* getAssetManager();
};

extern ACAssets* acAssets;

#endif
