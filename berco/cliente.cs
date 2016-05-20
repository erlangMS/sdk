using System;
using System.Collections.Generic;
using System.Text;
using ErlangMS/lib;

class TestClass
{
    static void Main(string[] args)
    {
		System.Console.WriteLine("Cliente CSharp ErlangMS");
	
		OtpSelf cNode = new OtpSelf("clientnode", "aula");
		OtpPeer sNode = new OtpPeer("no1@puebla");

		OtpConnection connection = cNode.connect(sNode);

		Otp.Erlang.Object[] msg = new Otp.Erlang.Object[] { 
			new Otp.Erlang.Long(1), new Otp.Erlang.Long(4)
		}; 
		connection.sendRPC("mathserver", "multiply", msg);

		Otp.Erlang.Long sum = (Otp.Erlang.Long)connection.receiveRPC();
		Console.WriteLine("Return Value:" + sum.ToString());
            
                    
    }
}
