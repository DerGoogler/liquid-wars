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

#include "graphics.hpp"

void setColour(int p);

void onSurfaceCreated() {
    if(state == NULL)
        return;

    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
    glShadeModel(GL_SMOOTH);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    state->map->loadTexture();
}

void onDrawFrame() {
    if(state == NULL)
        return;

    state->currentlyDrawing = true;

    glClear(GL_COLOR_BUFFER_BIT);
    glLoadIdentity();
    glColor4f(1, 1, 1, 1);

    glScalef(((float)WIDTH-14.0)/WIDTH, 1, 1);
    glTranslatef(2, 0, 0);

    glPushMatrix();
    glTranslatef((float)WIDTH/2.0, (float)HEIGHT/2.0, 0);
    glScalef((float)WIDTH/2.0, (float)HEIGHT/2.0, 1);
    state->map->draw();
    glPopMatrix();

    glTranslatef(1, 0, 0);

    for(int i = 0; i < NUMBER_OF_TEAMS*state->dotsPerTeam; i++) {
        state->points[i*3] = state->dots[i]->x;
        state->points[i*3+1] = state->dots[i]->y;
        state->colours[i*4] = state->dots[i]->getRed();
        state->colours[i*4+1] = state->dots[i]->getGreen();
        state->colours[i*4+2] = state->dots[i]->getBlue();
    }

    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_COLOR_ARRAY);
    glColorPointer(4, GL_FLOAT, 0, &state->colours[0]);
    glVertexPointer(3, GL_FLOAT, 0, &state->points[0]);
    glDrawArrays(GL_POINTS, 0, NUMBER_OF_TEAMS*state->dotsPerTeam);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_COLOR_ARRAY);

    const GLfloat line[] = { 0, 0, 0
                           , 0, 1, 0
                           , 1, 0, 0
                           , 1, 1, 0 };
    for(int p = 0; p < 6; p++) {
        glLoadIdentity();
        glTranslatef((float)WIDTH - ((5-p) * 2) - 2, 0, 0);
        float s = 1 - (float)state->players[p].score/(state->dotsPerTeam*NUMBER_OF_TEAMS);
        s = s*s*s;
        glScalef(2, (float)HEIGHT*(1-s), 1);

        setColour(p);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, &line);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    glLoadIdentity();
    glScalef(2, HEIGHT*(1.0 - state->timeSidebar), 1);

    setColour(state->me);

    glEnableClientState(GL_VERTEX_ARRAY);
    glVertexPointer(3, GL_FLOAT, 0, &line);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glDisableClientState(GL_VERTEX_ARRAY);

    state->currentlyDrawing = false;
}

void setColour(int p) {
    switch(p) {
        case 0:
            glColor4f(0, 1, 0, 1);
            break;
        case 1:
            glColor4f(0.15, 0.15, 1, 1);
            break;
        case 2:
            glColor4f(1, 0, 0, 1);
            break;
        case 3:
            glColor4f(0, 1, 1, 1);
            break;
        case 4:
            glColor4f(1, 1, 0, 1);
            break;
        case 5:
            glColor4f(1, 0, 1, 1);
            break;
    }
}

void onSurfaceChanged(int width, int height) {
    if(state == NULL)
        return;

    glViewport(0, 0, width, height);
    glClearColor(0, 0, 0, 1);
    state->displayWidth = width;
    state->displayHeight = height;
    acLib->glOrthogonal(0, WIDTH, 0, HEIGHT, -1, 1);
    float pointSize = (float)width/WIDTH;
    if((float)height/HEIGHT > pointSize)
        pointSize = (float)height/HEIGHT;
    glPointSize(pointSize+0.5);
}
