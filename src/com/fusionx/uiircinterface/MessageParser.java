/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.uiircinterface;

import android.content.Context;

import com.fusionx.Utils;
import com.fusionx.irc.Server;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.uiircinterface.ServerCommandSender;

import java.util.ArrayList;

/**
 * This entire class needs input validation and full parsing
 */
public class MessageParser {
    public static void channelMessageToParse(final CommonCallbacks callbacks,
                                             final String channelName, final String message) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(message);
        final String command = parsedArray.remove(0);
        final Server server = callbacks.getServer(false);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToChannel(server, channelName, action);
                    break;
                case "/part":
                case "/p":
                    ServerCommandSender.sendPart(server, channelName,
                            callbacks.getApplicationContext());
                    break;
                default:
                    serverCommandToParse(callbacks, message);
                    break;
            }
        } else {
            ServerCommandSender.sendMessageToChannel(server, channelName, message);
        }
    }

    public static void userMessageToParse(final CommonCallbacks callbacks, final String userNick,
                                          final String message) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(message);
        final String command = parsedArray.remove(0);
        final Server server = callbacks.getServer(false);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToUser(server, userNick, action);
                    break;
                case "/close":
                case "/c":
                    ServerCommandSender.sendClosePrivateMessage(server,
                            server.getUserChannelInterface().getUser(userNick));
                    break;
                default:
                    serverCommandToParse(callbacks, message);
                    break;
            }
        } else {
            ServerCommandSender.sendMessageToUser(server, userNick, message);
        }
    }

    public static void serverMessageToParse(final CommonCallbacks callbacks,
                                            final String message) {
        final Server server = callbacks.getServer(false);
        if (message.startsWith("/")) {
            serverCommandToParse(callbacks, message);
        } else {
            ServerCommandSender.sendUnknownEvent(server, message);
        }
    }

    private static void serverCommandToParse(final CommonCallbacks callbacks,
                                             final String rawLine) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(rawLine);
        final String command = parsedArray.remove(0);
        final Server server = callbacks.getServer(false);

        switch (command) {
            case "/join":
            case "/j":
                final String channelName = parsedArray.get(0);
                ServerCommandSender.sendJoin(server, channelName);
                break;
            case "/msg":
                if (parsedArray.size() > 1) {
                    final String nick = parsedArray.remove(0);
                    final String message = parsedArray.size() >= 1 ?
                            Utils.convertArrayListToString(parsedArray) : "";
                    ServerCommandSender.sendMessageToUser(server, nick, message);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            case "/nick":
                final String newNick = parsedArray.get(0);
                ServerCommandSender.sendNickChange(server, newNick);
                break;
            case "/quit":
                callbacks.disconnect();
                break;
            default:
                ServerCommandSender.sendUnknownEvent(server, rawLine);
        }
    }
}