/*********************************************************************
 * @title Módulo EmsUtil
 * @version 1.0.0
 * @doc Funções úteis   
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import javax.persistence.OneToMany;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
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
			.registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {   
			    @Override
			    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
			        if(src == src.longValue())
			            return new JsonPrimitive(src.longValue());          
			        return new JsonPrimitive(src);
			    }})
			.registerTypeAdapter(Integer.class,  new JsonSerializer<Integer>() {   
			    @Override
			    public JsonElement serialize(Integer value, Type typeOfSrc, JsonSerializationContext context) {
			        return new JsonPrimitive(value);
			    }})
			.registerTypeAdapter(Float.class,  new JsonSerializer<Float>() {   
			    @Override
			    public JsonElement serialize(Float value, Type typeOfSrc, JsonSerializationContext context) {
			        return new JsonPrimitive(value);
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

	public static <T> Object fromJson(final String jsonString, Class<T> classOfObj) {
		T obj = (T) gson.fromJson(jsonString, classOfObj);
	    return obj;
	}

	public static void setValuesFromJson(Object obj, Map<String, Object> update_values){
		Class<? extends Object> class_obj = obj.getClass();
		for (String field_name : update_values.keySet()){
			try{
				Field field = class_obj.getDeclaredField(field_name); 
				field.setAccessible(true);
				Object new_value = update_values.get(field_name);
				Class<?> tipo_field = field.getType(); 
				if (tipo_field == Integer.class){
					if (new_value instanceof String){
						field.set(obj, Integer.parseInt((String) new_value));
					}else if (new_value instanceof Double){
						field.set(obj, ((Double)new_value).intValue());
					}else{
						field.set(obj,  (int) new_value);
					}
				}else if (tipo_field == BigDecimal.class){
					if (new_value instanceof String){
						field.set(obj, BigDecimal.valueOf(Double.parseDouble((String) new_value)));
					}else{
						field.set(obj,  BigDecimal.valueOf((double) new_value));
					}
				}else if (tipo_field == String.class){
					if (new_value instanceof String){
						field.set(obj, new_value);
					}else if (new_value instanceof Double){
						// Parece um inteiro? (termina com .0)
						if (new_value.toString().endsWith(".0")){
							field.set(obj, Integer.toString(((Double)new_value).intValue()));
						}else{
							field.set(obj, new_value.toString());	
						}
					}else{
						field.set(obj, new_value.toString());
					}
				}else if (tipo_field == Boolean.class){
					if (new_value instanceof String){
						if (((String) new_value).equalsIgnoreCase("true")){
							field.set(obj, true);	
						}else if  (((String) new_value).equalsIgnoreCase("false")){
							field.set(obj, false);
						}else if  (((String) new_value).equalsIgnoreCase("1")){
							field.set(obj, true);
						}else if  (((String) new_value).equalsIgnoreCase("0")){
							field.set(obj, false);
						}else if  (((String) new_value).equalsIgnoreCase("sim")){
							field.set(obj, true);
						}else{
							field.set(obj, false);
						}
					}else if (new_value instanceof Double){
						if (new_value.toString().equals("1.0")){
							field.set(obj, true);
						}else{
							field.set(obj, false);
						}
					}else if (new_value instanceof Boolean){
						field.set(obj, (boolean) new_value);
					}else{
						field.set(obj, false);
					}
				}else if (tipo_field == java.util.Date.class){
					if (new_value instanceof String && ((String)new_value).length() == 10){
						field.set(obj, new SimpleDateFormat("dd/MM/yyyy").parse((String) new_value));
					}else{
						throw new IllegalArgumentException("Não é uma data válida");
					}
				}else{
					throw new IllegalArgumentException("Não suporta o tipo de dado para pesquisa.");
				}
			}catch (Exception e){
				throw new IllegalArgumentException("Campo "+ field_name + " inválido. Erro interno: "+ e.getMessage());
			}
		}
	}
	
}
