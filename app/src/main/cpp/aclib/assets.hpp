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
