using System;
using System.Reflection;

namespace DynamicMethod
{
	class MainClass
	{

		public static void Main(string[] args)
		{
			Type tipo = typeof(MainClass);
			MethodInfo m = tipo.GetMethod("HelloWorld");
			Object obj = new MainClass();

			Object[] parameters = { "teste" }; 
	
			m.Invoke(obj, parameters);

		}

		public void HelloWorld(String msg)
		{
			
			Console.WriteLine(msg);

		}
			

	}
}
