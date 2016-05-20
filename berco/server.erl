-module(server).

-export([ping/0, start/0, loop/0]).

ping() -> pong.

start() -> spawn(server, loop, []).

loop() ->
	receive
		{ClientPid, Msg} ->

			io:format("Chegou mensagem de ~p: ~p\n", [ClientPid, Msg]),
			ClientPid ! pong;
		M -> 
			io:format("nao entendi: ~p\n", [M])

	end,
	loop().

