using System;
namespace br.erlangms
{
	public class EmsValidationException : SystemException
	{
		private List<String> errors;

		public EmsValidationException() : base()
		{
			errors = new ArrayList<>();
		}

		public EmsValidationException(String e) : base(e)
		{
			errors = new ArrayList<>();
			errors.add(e);
		}


		public EmsValidationException(List<String> l)
		{
			errors = l;
		}

		public void addError(String error)
		{
			errors.add(error);
		}

		public List<String> getErrors()
		{
			return errors;
		}
	}
}

