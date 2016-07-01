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
			Object result = m.Invoke(obj, parameters);
			Console.WriteLine(result);
		}

		public object HelloWorld(String msg)
		{
			
			Console.WriteLine(msg);
			return 10;

		}
			

	}
}
