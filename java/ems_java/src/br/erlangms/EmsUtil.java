package br.erlangms;

import javax.persistence.OneToMany;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class EmsUtil {

	private static class SerializeStrategy implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
        	return (f.getAnnotation(OneToMany.class) != null);
        }
    }
	
	public static String toJson(final Object object){
		Gson gson = new GsonBuilder()
        	.setExclusionStrategies(new SerializeStrategy())
        	//.serializeNulls() <-- uncomment to serialize NULL fields as well
        	.create();		
		String result = gson.toJson(object);
		return result;
	}

	public static <clazz> Object fromJson(final String jsonString, Class<?> clazz) {
		Gson gson = new Gson();
	    clazz obj = gson.fromJson(jsonString, clazz);
	    return obj;
	}

}
