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
