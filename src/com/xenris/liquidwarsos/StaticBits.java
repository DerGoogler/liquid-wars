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

package com.xenris.liquidwarsos;

public class StaticBits {
    public static ClientGameSetupActivity clientGameSetupActivity;
    public static MultiplayerGameSetupActivity multiplayerGameSetupActivity;
    public static int team;
    public static int map;
    public static int seed;
    public static int dotsPerTeam = 400;
    public static long startTimestamp;
    public static int timeLimit = 4*60;
    public static Client client;
    public static Server server;
    public static String publicName = "Liquid Wars Game";
    public static int[] teams = new int[6];
    public static boolean gameWasDisconnected = false;
    public static final int VERSION_CODE = 9;
    public static final int NUMBER_OF_TEAMS = 6;
    public static final int AI_PLAYER = -1;
    public static final int NUMBER_OF_MAPS = 46;
    public static final int PORT_NUMBER = 51055;
    public static final int GAME_SPEED = 7000;
    public static final int RESEND_STEPS = 0x70;
    public static final int PLAYER_POSITION_DATA = 0x71;
    public static final int STEP_GAME = 0x72;
    public static final int REGULATED_STEP = 0x73;
    public static final int FAST_STEP = 0x74;
    public static final int CLIENT_CURRENT_GAMESTEP = 0x75;
    public static final int CLIENT_READY = 0x76;
    public static final int CLIENT_READY_QUERY = 0x77;
    public static final int KILL_GAME = 0x78;
    public static final int CLIENT_EXIT = 0x79;
    public static final int BACK_TO_MENU = 0x7A;
    public static final int OUT_OF_TIME = 0x7B;
    public static final int TIME_DIFF = 0x7C;
    public static final int UPDATE_SERVER_NAME = 0x7D;
    public static final int SET_TEAM = 0x7E;
    public static final int SET_TIME_LIMIT = 0x7F;
    public static final int SET_MAP = 0x80;
    public static final int START_GAME = 0x81;
    public static final int SEND_VERSION_CODE = 0x82;
    public static final int SET_TEAM_SIZE = 0x83;

    public static void init() {
        team = 0;
        map = -1;
        newSeed();
        client = null;
        server = null;
        for(int i = 0; i < StaticBits.teams.length; i++)
            StaticBits.teams[i] = AI_PLAYER;
        teams[team] = 0;
    }

    public static void newSeed() {
        seed = Math.abs((int)System.currentTimeMillis());
    }
}
