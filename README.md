# JavaGameBC

This project originates from the final assignment of NJU-java2022. It has developed a simple multi-player game by Java 17. The project is constructed by Maven. GUI is based on Java Swing. Network communicating is based on Java NIO.

### Start the Game

Download the release and you will see two jar files.



First, start the game server. 

```
java -jar GameServer.jar <saveName> <newSaveName> <port>
```

The server will load `saves/<saveName>.txt` as the initial game state and will write the game state into `saves/<newSaveName>.txt` when saving. Your game server will run on port `<port>` .



Or you can start the server loading a default save file without a given `saveName` .

```
java -jar GameServer.jar <newSaveName> <port>
```

Then you can see the IP and the port of the game server in the terminal.



Next, start game clients.

```
java -jar GameClient.jar <ip> <port>
```

Game clients and the game server should be in a same LAN. `<ip>` and `<port>` represent the IP and the port respectively where the game server is running.

At most 4 game clients are permitted to link to a single game server.



If you are running the project with an ide, `GameServer.java` and `GameClient.java` are program entrances and arguments should be set as well.

### Control

Keys WASD or direction keys for changing players' direction. Key I for save the game. Key P for pausing or resuming game.

### Doc

If you are able to read Chinese, you can look up *Developing a Java Game from Scratch* for details of the project.