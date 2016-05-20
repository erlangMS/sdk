-module(mathserver).
-compile(export_all).
 
multiply(First, Second) ->
	io:format("passei aqui: ~p :  ~p!\n\n", [First, Second]),
	First * Second.
