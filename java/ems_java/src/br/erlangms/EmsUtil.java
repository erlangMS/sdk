package br.erlangms;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.persistence.OneToMany;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public final class EmsUtil {
	private static NumberFormat doubleFormatter = null;
	private static Gson gson = null;
	static{
		doubleFormatter = NumberFormat.getInstance(Locale.US);
		doubleFormatter.setMaximumFractionDigits(2); 
		doubleFormatter.setMinimumFractionDigits(2);
		gson = new GsonBuilder()
	    	.setExclusionStrategies(new SerializeStrategy())
	    	.setDateFormat("dd/MM/yyyy")
	    	//.serializeNulls() <-- uncomment to serialize NULL fields as well
	    	.registerTypeAdapter(BigDecimal.class, new JsonSerializer<BigDecimal>()  { 
						@Override
						public JsonElement serialize(BigDecimal value, Type arg1, com.google.gson.JsonSerializationContext arg2) {
	                		String result;
	                        result = EmsUtil.doubleFormatter.format(value); 
	                        return new JsonPrimitive(result); 
						}})
	    	.create();		
	}
	
	private static class SerializeStrategy implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
        	return (f.getAnnotation(OneToMany.class) != null);
        }
    }
	
	public static String toJson(final Object object){
		String result = gson.toJson(object);
		return result;
	}

	public static <clazz> Object fromJson(final String jsonString, Class<?> clazz) {
	    @SuppressWarnings("unchecked")
		clazz obj = (clazz) gson.fromJson(jsonString, clazz);
	    return obj;
	}

}
