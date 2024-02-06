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

#include "assets.hpp"

ACAssets* acAssets;

ACAssets::ACAssets(AAssetManager* am) {
    assetManager = am;
}

unsigned char* ACAssets::getFile(const char* fileName, int* size) {
    AAsset* assetFile = AAssetManager_open(assetManager, fileName, AASSET_MODE_BUFFER);
    if(assetFile == 0)
        return NULL;

    *size = AAsset_getLength(assetFile);
    unsigned char* buffer = (unsigned char*)malloc(*size);

    if(AAsset_read(assetFile, buffer, *size) != *size) {
        AAsset_close(assetFile);
        return NULL;
    }

    AAsset_close(assetFile);
    return buffer;
}

unsigned int ACAssets::getFileSize(const char* fileName) {
    AAsset* assetFile = AAssetManager_open(assetManager, fileName, AASSET_MODE_BUFFER);
    if(assetFile == 0)
        return -1;

    const unsigned int size = AAsset_getLength(assetFile);
    AAsset_close(assetFile);
    return size;
}

AAssetManager* ACAssets::getAssetManager() {
    return assetManager;
}
