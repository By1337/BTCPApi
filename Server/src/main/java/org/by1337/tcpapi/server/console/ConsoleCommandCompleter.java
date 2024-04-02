package org.by1337.tcpapi.server.console;


import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.command.Command;
import org.by1337.tcpapi.server.network.Server;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class ConsoleCommandCompleter implements Completer {
    private final Command<Server> command;

    public ConsoleCommandCompleter(Command<Server> command) {
        this.command = command;
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        String[] args = parsedLine.line().split(" ");
        for (String s : command.getTabCompleter(ServerManager.getServer(), args)) {
            list.add(new Candidate(s));
        }
    }
}
