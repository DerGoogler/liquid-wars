#include "map.hpp"

Map::Map(int mapID) {
    path = "maps/";
    std::stringstream ss;
    ss << mapID;
    path += ss.str();

    int pngDataSize;
    unsigned char* pngData = (unsigned char*)acAssets->getFile((path + "-map.png").data(), &pngDataSize);
    if(pngData == NULL)
        pngData = (unsigned char*)acAssets->getFile("maps/blank-map.png", &pngDataSize);

    lodepng_decode32((unsigned char**)&imageMapData, &imageMapWidth, &imageMapHeight, pngData, pngDataSize);

    delete(pngData);

    imageMapWidthRatio = (float)imageMapWidth/WIDTH;
    imageMapHeightRatio = (float)imageMapHeight/HEIGHT;
}

Map::~Map() {
    delete(imageMapData);
    glDeleteTextures(1, &glTextureID);
}

void Map::loadTexture() {
    int pngDataSize;
    unsigned char* pngData = (unsigned char*)acAssets->getFile((path + "-image.png").data(), &pngDataSize);
    unsigned int* image;
    unsigned int width, height;
    if(pngData != NULL) {
        lodepng_decode32((unsigned char**)&image, &width, &height, pngData, pngDataSize);
        delete(pngData);
    }


    unsigned int* potImage;
    unsigned int potWidth, potHeight;
    if(pngData != NULL)
        createPOTImage(image, width, height, &potImage, &potWidth, &potHeight);
    else
        createPOTImage(imageMapData, imageMapWidth, imageMapHeight, &potImage, &potWidth, &potHeight);

    if(pngData != NULL)
        delete(image);

    glEnable(GL_TEXTURE_2D);
    glGenTextures(1, &glTextureID);
    glBindTexture(GL_TEXTURE_2D, glTextureID);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, potWidth, potHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, potImage);
    glDisable(GL_TEXTURE_2D);

    delete(potImage);
}

void Map::draw() {
    glColor4f(1, 1, 1, 1);

    static const GLfloat texCoords[] = { 0.0, 0.0
                                       , 1.0, 0.0
                                       , 0.0, 1.0
                                       , 1.0, 1.0 };

    static const GLfloat vertices[] = {-1.0, 1.0, 0.0
                                      , 1.0, 1.0, 0.0
                                      ,-1.0,-1.0, 0.0
                                      , 1.0,-1.0, 0.0 };

    glEnable(GL_TEXTURE_2D);
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    glBindTexture(GL_TEXTURE_2D, glTextureID);
    glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
    glVertexPointer(3, GL_FLOAT, 0, vertices);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisable(GL_TEXTURE_2D);
}

bool Map::isWall(int width, int height) {
    height = (HEIGHT-1) - height;

    if(width < 0)
        return true;
    if(width >= WIDTH)
        return true;
    if(height < 0)
        return true;
    if(height >= HEIGHT)
        return true;

    width *= imageMapWidthRatio;
    height *= imageMapHeightRatio;

    const int i = height*imageMapWidth + width;
    const unsigned int pixel = imageMapData[i];
    return (alpha(pixel) > 3);
}

unsigned int Map::alpha(unsigned int pixel) {
    return ((pixel >> 24) & 0xFF);
}

void Map::createPOTImage(unsigned int* image, unsigned int width, unsigned int height, unsigned int** outImage, unsigned int* outWidth, unsigned int* outHeight) {
    //scale image so that width and height are power of 2.
    //This needs to be done for opengl es 1.1.
    int width2 = 1;
    int height2 = 1;
    while(width2 < width)
        width2 *= 2;
    while(height2 < height)
        height2 *= 2;

    uint32_t* image2 = (uint32_t*)malloc(width2 * height2 * 4);
    float x_ratio = (float)width/(float)width2;
    float y_ratio = (float)height/(float)height2;
    float px, py;
    int i, j;
    for(i = 0; i < height2; i++) {
        for(j = 0; j < width2; j++) {
            px = (uint32_t)((float)j*x_ratio);
            py = (uint32_t)((float)i*y_ratio);
            image2[(i*width2)+j] = image[(int)((py*width)+px)];
        }
    }

    (*outImage) = image2;
    (*outWidth) = width2;
    (*outHeight) = height2;
}
