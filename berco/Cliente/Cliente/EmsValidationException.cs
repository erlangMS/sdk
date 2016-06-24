using System;
using System.Collections;
using System.Collections.Generic;

namespace br.erlangms
{
	public class EmsValidationException : SystemException
	{
		private List<String> errors;

		public EmsValidationException() : base()
		{
			
			errors = new List<string>();
		}

		public EmsValidationException(String e) : base(e)
		{
			errors = new List<string>();
			errors.Add(e);
		}


		public EmsValidationException(List<String> l)
		{
			errors = l;
		}

		public void addError(String error)
		{
			errors.Add(error);
		}

		public List<String> getErrors()
		{
			return errors;
		}
	}
}

