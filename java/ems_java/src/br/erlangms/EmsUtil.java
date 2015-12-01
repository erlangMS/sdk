/*********************************************************************
 * @title Módulo EmsUtil
 * @version 1.0.0
 * @doc Funções úteis   
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.ws.rs.client.ClientBuilder;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class EmsUtil {
	private static final OtpErlangAtom ok = new OtpErlangAtom("ok");
	private static final OtpErlangAtom service_atom = new OtpErlangAtom("servico");
	private static final OtpErlangAtom request_msg_atom = new OtpErlangAtom("request");
	private static final OtpErlangBinary result_null = new OtpErlangBinary("{\"ok\":\"null\"}".getBytes());
	private static final OtpErlangBinary erro_convert_json = new OtpErlangBinary("{\"erro\":\"service\", \"message\" : \"Falha na serialização do conteúdo em JSON\"}".getBytes());
	private static final OtpErlangBinary result_list_empty = new OtpErlangBinary("[]".getBytes());
	private static NumberFormat doubleFormatter = null;
	private static Gson gson = null;
	private static Gson gson2 = null;
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
			.registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>() {   
			    @SuppressWarnings("deprecation")
				@Override
			    public JsonElement serialize(java.util.Date value, Type typeOfSrc, JsonSerializationContext context) {
			    	if (value.getHours() == 0 && value.getMinutes() == 0){
			            return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy").format(value));
			        }else{
			        	if (value.getSeconds() == 0){
			        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(value));
			        	}else{
			        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value));
			        	}
			        }
			    }})					
			.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {   
			    @SuppressWarnings("deprecation")
				@Override
			    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
			    	if (value.getHours() == 0 && value.getMinutes() == 0){
			            return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy").format(value));
			        }else{
			        	if (value.getSeconds() == 0){
			        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(value));
			        	}else{
			        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value));
			        	}
			        }
			    }})					
		    .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                    public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    	String value = json.getAsString();                            	
						final String m_erro = "Não é uma data válida.";
                    	try {
                        	if (value.length() == 10){
								return new SimpleDateFormat("dd/MM/yyyy").parse(value);
    						}else if (value.length() == 16){
    							return new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(value);
    						}else if (value.length() == 19){
    							return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(value);
    						}else{
    							throw new EmsValidationException(m_erro);
    						}
						} catch (ParseException e) {
							throw new EmsValidationException(m_erro);
						}
					}
                })    
		    .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                    public java.sql.Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    	String value = json.getAsString();                            	
						final String m_erro = "Não é uma data válida";
                    	try {
                        	if (value.length() == 10){
								return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy").parse(value).getTime());
    						}else if (value.length() == 16){
    							return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(value).getTime());
    						}else if (value.length() == 19){
    							return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(value).getTime());
    						}else{
    							throw new EmsValidationException(m_erro);
    						}
						} catch (ParseException e) {
							throw new EmsValidationException(m_erro);
						}
					}
                })    
            .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)	
            .registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
                    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						String value = json.getAsString();
						if (value.equalsIgnoreCase("true")){
							return true;	
						}else if  (value.equalsIgnoreCase("false")){
							return false;
						}else if  (value.equalsIgnoreCase("1")){
							return true;
						}else if  (value.equalsIgnoreCase("0")){
							return false;
						}else if  (value.equalsIgnoreCase("sim")){
							return true; 
						}else if  (value.equalsIgnoreCase("1.0")){
							return true;
						}else if  (value.equalsIgnoreCase("yes")){
							return true;
						}else{
							return false;
						}
                   }
                })    
            .create();		

		gson2 = new GsonBuilder()
    	.setExclusionStrategies(new SerializeStrategy2())
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
		.registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>() {   
		    @SuppressWarnings("deprecation")
			@Override
		    public JsonElement serialize(java.util.Date value, Type typeOfSrc, JsonSerializationContext context) {
		    	if (value.getHours() == 0 && value.getMinutes() == 0){
		            return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy").format(value));
		        }else{
		        	if (value.getSeconds() == 0){
		        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(value));
		        	}else{
		        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value));
		        	}
		        }
		    }})					
		.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {   
		    @SuppressWarnings("deprecation")
			@Override
		    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
		    	if (value.getHours() == 0 && value.getMinutes() == 0){
		            return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy").format(value));
		        }else{
		        	if (value.getSeconds() == 0){
		        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(value));
		        	}else{
		        		return new JsonPrimitive(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value));
		        	}
		        }
		    }})					
	    .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                	String value = json.getAsString();                            	
					final String m_erro = "Não é uma data válida.";
                	try {
                    	if (value.length() == 10){
							return new SimpleDateFormat("dd/MM/yyyy").parse(value);
						}else if (value.length() == 16){
							return new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(value);
						}else if (value.length() == 19){
							return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(value);
						}else{
							throw new EmsValidationException(m_erro);
						}
					} catch (ParseException e) {
						throw new EmsValidationException(m_erro);
					}
				}
            })    
	    .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                public java.sql.Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                	String value = json.getAsString();                            	
					final String m_erro = "Não é uma data válida.";
                	try {
                    	if (value.length() == 10){
							return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy").parse(value).getTime());
						}else if (value.length() == 16){
							return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(value).getTime());
						}else if (value.length() == 19){
							return new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(value).getTime());
						}else{
							throw new EmsValidationException(m_erro);
						}
					} catch (ParseException e) {
						throw new EmsValidationException(m_erro);
					}
				}
            })    
        .registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
                public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					String value = json.getAsString();
					if (value.equalsIgnoreCase("true")){
						return true;	
					}else if  (value.equalsIgnoreCase("false")){
						return false;
					}else if  (value.equalsIgnoreCase("1")){
						return true;
					}else if  (value.equalsIgnoreCase("0")){
						return false;
					}else if  (value.equalsIgnoreCase("sim")){
						return true; 
					}else if  (value.equalsIgnoreCase("1.0")){
						return true;
					}else if  (value.equalsIgnoreCase("yes")){
						return true;
					}else{
						return false;
					}
               }
            })    
        .create();		
	
	}
	
	private static class SerializeStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> c) {
        	return false;
        }
        public boolean shouldSkipField(FieldAttributes f) {
        	return f.getAnnotation(OneToMany.class)  != null;
        			//f.getAnnotation(JoinColumn.class) != null ||
        			//f.getAnnotation(OneToOne.class)   != null);
        }
    }
	
	private static class SerializeStrategy2 implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> c) {
        	return false;
        }
        public boolean shouldSkipField(FieldAttributes f) {
        	return (f.getAnnotation(OneToMany.class) != null);
        }
    }

	/**
	 * This TypeAdapter unproxies Hibernate proxied objects, and serializes them
	 * through the registered (or default) TypeAdapter of the base class.
	 */
	public static class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {

	    public final static TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
	        @Override
	        @SuppressWarnings("unchecked")
	        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
	            return (HibernateProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
	        }
	    };
	    private final Gson context;

	    private HibernateProxyTypeAdapter(Gson context) {
	        this.context = context;
	    }

	    @Override
	    public HibernateProxy read(JsonReader in) throws IOException {
	        throw new UnsupportedOperationException("Not supported");
	    }

	    @SuppressWarnings({"rawtypes", "unchecked"})
	    @Override
	    public void write(JsonWriter out, HibernateProxy value) throws IOException {
	        if (value == null) {
	            out.nullValue();
	            return;
	        }
	        try{
		        // Retrieve the original (not proxy) class
		        Class<?> baseType = Hibernate.getClass(value);
		        // Get the TypeAdapter of the original class, to delegate the serialization
		        TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
		        // Get a filled instance of the original class
		        Object unproxiedValue = ((HibernateProxy) value).getHibernateLazyInitializer()
		                .getImplementation();
		        // Serialize the value
		        delegate.write(out, unproxiedValue);
	        }catch (Exception e){
	        	out.nullValue();
	        }
	    }
	}
	
	
	/**
	 * Serializa um objeto para json
	 * @param obj Objeto que será serializado para json
	 * @return string json da serialização
	 * @author Everton de Vargas Agilar
	 */
	public static String toJson(final Object obj){
		return toJson(obj, false);
	}

	/**
	 * Serializa um objeto para json
	 * @param obj Objeto que será serializado para json
	 * @param serializeFullObject Se true, serializa atributos de classe também
	 * @return string json da serialização
	 * @author Everton de Vargas Agilar
	 */
	public static String toJson(final Object obj, boolean serializeFullObject){
		if (obj != null){
			String result = null;
			if (serializeFullObject){
				result = gson2.toJson(obj);
			}else{
				result = gson.toJson(obj);
			}
			return result;
		}else{
			return null;
		}
	}

	/**
	 * Serializa um objeto a partir de uma string json
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @author Everton de Vargas Agilar
	 */
	public static <T> T fromJson(final String jsonString, Class<T> classOfObj) {
		return (T) fromJson(jsonString, classOfObj, null);
	}

	/**
	 * Serializa um objeto a partir de uma string json
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(final String jsonString, Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter) {
		if (jsonString != null && !jsonString.isEmpty() && classOfObj != null){
			if (classOfObj == List.class || classOfObj == ArrayList.class){
				List<Object> values = gson.fromJson(jsonString, List.class);
				return (T) values;
			}else if (classOfObj == java.util.HashMap.class || classOfObj == Map.class){
				Map<String, Object> values = gson.fromJson(jsonString, Map.class);
				return (T) values;
			}else{
				Map<String, Object> values = gson.fromJson(jsonString, Map.class);
				T obj = null;
				try {
					obj = classOfObj.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException
						| EmsValidationException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new EmsValidationException("Não suporta conversão do json para "+ classOfObj.getSimpleName() + ". Json: "+ jsonString);
				}
				setValuesFromMap(obj, values, jsonModelAdapter);
				return obj;
			}
		}
		return null;
	}

	/**
	 * Obtém uma lista a partir de um json
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @author Everton de Vargas Agilar
	 */
	public static <T> List<T> fromListJson(String jsonString, Class<T> classOfObj) {
		return fromListJson(jsonString, classOfObj, null);
	}
	
	/**
	 * Obtém uma lista a partir de um json
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
 	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromListJson(String jsonString, Class<T> classOfObj, EmsJsonModelAdapter jsonModelAdapter) {
		if (jsonString != null && classOfObj != null){
			List<Object> values = gson.fromJson(jsonString, List.class);
			ArrayList<T> newList = new ArrayList<T>();
			for (Object value : values){
				try {
					T obj = classOfObj.getConstructor().newInstance();
					setValuesFromMap(obj, (Map<String, Object>) value, jsonModelAdapter);
					newList.add(obj);
				} catch (Exception e) {
					throw new EmsValidationException("Não suporta conversão do json para "+ classOfObj.getSimpleName() + ". Json: "+ jsonString);
				}
			}
			return newList;
		}
		return null;
	}
	
	/**
	 * Seta os valores nos parâmetros de um query a partir de um map
	 * @param query Instância da query com parâmetros a setar
	 * @param values	Map com chave/valor dos dados que serão aplicados na query
	 * @author Everton de Vargas Agilar
	 */
	public static void setQueryParameterFromMap(Query query, Map<String, Object> values){
		if (query != null && values != null && values.size() > 0){
			int p = 1;
			for (String field_name : values.keySet()){
				try{
					Object value_field = values.get(field_name);
					Class<?> paramType = query.getParameter(p).getParameterType();
					if (paramType == Integer.class){
						if (value_field instanceof String){
							query.setParameter(p++, Integer.parseInt((String) value_field));
						}else if (value_field instanceof Double){
							query.setParameter(p++, ((Double)value_field).intValue() );
						}else{
							query.setParameter(p++, value_field);
						}
					}else if (paramType == BigDecimal.class){
						if (value_field instanceof String){
							query.setParameter(p++, BigDecimal.valueOf(Double.parseDouble((String) value_field)));
						}else{
							query.setParameter(p++,  BigDecimal.valueOf((double) value_field));
						}
					}else if (paramType == String.class){
						if (value_field instanceof String){
							query.setParameter(p++, value_field);
						}else if (value_field instanceof Double){
							// Parece um inteiro? (termina com .0)
							if (value_field.toString().endsWith(".0")){
								query.setParameter(p++, Integer.toString(((Double)value_field).intValue()));
							}else{
								query.setParameter(p++, value_field.toString());	
							}
						}else{
							query.setParameter(p++, value_field.toString());
						}
					}else if (paramType == Boolean.class){
						if (value_field instanceof String){
							if (((String) value_field).equalsIgnoreCase("true")){
								query.setParameter(p++, true);	
							}else if  (((String) value_field).equalsIgnoreCase("false")){
								query.setParameter(p++, false);
							}else if  (((String) value_field).equalsIgnoreCase("1")){
								query.setParameter(p++, true);
							}else if  (((String) value_field).equalsIgnoreCase("0")){
								query.setParameter(p++, false);
							}else if  (((String) value_field).equalsIgnoreCase("sim")){
								query.setParameter(p++, true);
							}else if  (((String) value_field).equalsIgnoreCase("1.0")){
								query.setParameter(p++, true);
							}else if  (((String) value_field).equalsIgnoreCase("yes")){
								query.setParameter(p++, true);
							}else{
								query.setParameter(p++, false);
							}
						}else if (value_field instanceof Double){
							if (value_field.toString().equals("1.0")){
								query.setParameter(p++, true);
							}else{
								query.setParameter(p++, false);
							}
						}else if (value_field instanceof Boolean){
							query.setParameter(p++, value_field);
						}else{
							query.setParameter(p++, false);
						}
					}else if (paramType == java.util.Date.class){
						final String m_erro = field_name + " não é uma data válida.";
						if (value_field instanceof String){
							int len_value = ((String) value_field).length();
							try {
	                        	if (len_value == 10){
	                        		query.setParameter(p++, new SimpleDateFormat("dd/MM/yyyy").parse((String) value_field));
	    						}else if (len_value == 16){
	    							query.setParameter(p++, new SimpleDateFormat("dd/MM/yyyy HH:mm").parse((String) value_field));
	    						}else if (len_value == 19){
	    							query.setParameter(p++, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse((String) value_field));	    							
	    						}else{
	    							throw new EmsValidationException(m_erro);
	    						}
							} catch (ParseException e) {
								throw new EmsValidationException(m_erro);
							}
						}else{
							throw new EmsValidationException(m_erro);
						}
					}else{
						throw new EmsValidationException("Não suporta o tipo de dado para o campo "+ field_name + ".");
					}
				}catch (Exception e){
					throw new EmsValidationException("Erro ao setar parâmetros da query. Erro interno: "+ e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Seta os valores no objeto a partir de um map
	 * @param obj Instância de um objeto
	 * @param values	Map com chave/valor dos dados que serão aplicados no objeto
	 * @author Everton de Vargas Agilar
	 */
	public static void setValuesFromMap(Object obj, Map<String, Object> values, EmsJsonModelAdapter jsonModelAdapter){
		if (obj != null && values != null && values.size() > 0){
			Class<? extends Object> class_obj = obj.getClass();
			for (String field_name : values.keySet()){
				try{
					Field field = null;
					try{
						field = class_obj.getDeclaredField(field_name);
					}catch (NoSuchFieldException e){
						// Ignora o campo quando ele não existe
						continue;
					}
					field.setAccessible(true);
					Object new_value = values.get(field_name);
					Class<?> tipo_field = field.getType(); 
					if (tipo_field == Integer.class || tipo_field == int.class){
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
							}else if  (((String) new_value).equalsIgnoreCase("1.0")){
								field.set(obj, true);
							}else if  (((String) new_value).equalsIgnoreCase("yes")){
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
						final String m_erro = field_name + " não é uma data válida.";
						if (new_value instanceof String){
							int len_value = ((String) new_value).length();
							try {
	                        	if (len_value == 10){
	                        		field.set(obj, new SimpleDateFormat("dd/MM/yyyy").parse((String) new_value));
	    						}else if (len_value == 16){
	                        		field.set(obj, new SimpleDateFormat("dd/MM/yyyy HH:mm").parse((String) new_value));
	    						}else if (len_value == 19){
	    							field.set(obj, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse((String) new_value));
	    						}else{
	    							throw new EmsValidationException(m_erro);
	    						}
							} catch (ParseException e) {
								throw new EmsValidationException(m_erro);
							}
						}else{
							throw new EmsValidationException(m_erro);
						}
					}else if (tipo_field == java.sql.Date.class){
						final String m_erro = field_name + " não é uma data válida.";
						if (new_value instanceof String){
							int len_value = ((String) new_value).length();
							try {
	                        	if (len_value == 10){
	                        		field.set(obj, new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy").parse((String) new_value).getTime()));
	    						}else if (len_value == 16){
	                        		field.set(obj, new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse((String) new_value).getTime()));
	    						}else if (len_value == 19){
	    							field.set(obj, new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse((String) new_value).getTime()));
	    						}else{
	    							throw new EmsValidationException(m_erro);
	    						}
							} catch (ParseException e) {
								throw new EmsValidationException(m_erro);
							}
						}else{
							throw new EmsValidationException(m_erro);
						}
					}else if (tipo_field == java.sql.Timestamp.class){
						final String m_erro = field_name + " não é uma data válida.";
						java.sql.Timestamp new_time = null;
						if (new_value instanceof String){
							int len_value = ((String) new_value).length();
							try {
	                        	if (len_value == 10){
	    							new_time = new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy").parse((String) new_value).getTime());
	    						}else if (len_value == 16){
	    							new_time = new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse((String) new_value).getTime());
	    						}else if (len_value == 19){
	    							new_time = new java.sql.Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse((String) new_value).getTime());
	    						}else{
	    							throw new EmsValidationException(m_erro);
	    						}
							} catch (ParseException e) {
								throw new EmsValidationException(m_erro);
							}
							field.set(obj, new_time);
						}else{
							throw new EmsValidationException(m_erro);
						}
					}else if (tipo_field.isEnum()){
						try{
							Integer idValue = null;
							if (new_value instanceof String){
								idValue = Integer.parseInt((String)new_value);	
							}else{
								idValue = ((Double)new_value).intValue();
							}
							@SuppressWarnings({ "unchecked", "rawtypes" })
							Enum<?> value = intToEnum(idValue, (Class<Enum>) tipo_field);
							field.set(obj, value);
						}catch (Exception e){
							throw new EmsValidationException(field_name + " não é válido.");
						}
					}else if (tipo_field instanceof Object && 
							  findFieldByAnnotation(tipo_field, Id.class) != null){
						try{
							Integer idValue = null;
							if (new_value instanceof String){
								idValue = Integer.parseInt((String)new_value);	
							}else{
								idValue = ((Double)new_value).intValue();
							}
							if (idValue > 0){
								Object model = jsonModelAdapter.findById(tipo_field, idValue);
								field.set(obj, model);
							}
						}
						catch (EmsNotFoundException e){
							throw new EmsValidationException(field_name + " não existe.");
						}catch (Exception e){
							throw new EmsValidationException(field_name + " inválido.");
						}
					}else{
						throw new EmsValidationException("Não suporta o tipo de dado do campo "+ field_name + ".");
					}
				}catch (EmsValidationException e){
					throw e;
				}catch (Exception e){
					throw new EmsValidationException("Campo "+ field_name + " inválido. Erro interno: "+ e.getMessage());
				}
			}
		}
	}
	

	/**
	 * Retorna o primeiro campo que encontrar a anotação passada como argumento.
	 * @param clazz Classe pojo. Ex.: OrgaoInterno.class
	 * @param ann	anotação que será pesquisada. Ex.: Id.class
	 * @return campo
	 * @author Everton de Vargas Agilar
	 */
	public static Field findFieldByAnnotation(Class<?> clazz, Class<? extends Annotation> ann) {
	    if (clazz != null && ann != null){
			Class<?> c = clazz;
		    while (c != null) {
		        for (Field field : c.getDeclaredFields()) {
		            if (field.isAnnotationPresent(ann)) {
		                return field;
		            }
		        }
		        c = c.getSuperclass();
		    }
	    }
	    return null;
	}
	
	/**
	 * Obtém o id de um objeto
	 * @param clazz Classe pojo. Ex.: OrgaoInterno.class
	 * @param ann	anotação que será pesquisada. Ex.: Id.class
	 * @return campo
	 * @author Everton de Vargas Agilar
	 */
	public static Integer getIdFromObject(Object obj) {
	    if (obj != null){
	    	Field idField = findFieldByAnnotation(obj.getClass(), Id.class);
			if (idField != null){
				try {
					idField.setAccessible(true);
					Object result = idField.get(obj); 
					if (result != null){
						return (int) result;
					}else{
						return null;
					}
				} catch (Exception e) {
					return null;
				}
			}else{
				throw new EmsValidationException("Objeto não tem id.");
			}
	    }else{
	    	throw new EmsValidationException("Obj não pode ser null em EmsUtil.getIdFromObject.");
	    }
	}	

	/**
	 * Converte um inteiro para a enumeração de acordo com clazz
	 * @param code código da enumeração
	 * @param clazz	classe da enumeração
	 * @return enumeração
	 * @author Everton de Vargas Agilar
	 */
	public static Enum<?> intToEnum(int code, @SuppressWarnings("rawtypes") Class<Enum> clazz) {
		if (code >= 0 && clazz != null){
			for(Enum<?> t : clazz.getEnumConstants()) {
		        if(t.ordinal() == code) {
		            return t;
		        }
		    }
		}
	    throw new EmsValidationException("Valor inválido para o campo "+ clazz.getSimpleName());
	}

	/**
	 * Converte um inteiro para a enumeração de acordo com clazz
	 * @param code código da enumeração
	 * @param clazz	classe da enumeração
	 * @return enumeração
	 * @author Everton de Vargas Agilar
	 */
	public static javax.ws.rs.client.Client getRestStream(){
		javax.ws.rs.client.Client client = ClientBuilder.newClient();
		return client;
	}	

	/**
	 * Converte um objeto Java para um objeto de response Erlang
	 * @param ret objeto
	 * @param rid Request Id da requisição
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
	public static OtpErlangTuple serializeObjectToErlangResponse(Object ret, long rid){
	    OtpErlangObject[] otp_result = new OtpErlangObject[3];
		OtpErlangObject[] reply = new OtpErlangObject[2];
	    reply[0] = ok;
	    if (ret != null){
	    	try{
	        	String m_json = null;
	        	if (ret instanceof OtpErlangBinary){
	        		reply[1] = (OtpErlangBinary) ret;
	        	}else if (ret instanceof OtpErlangAtom){
	        		reply[1] = (OtpErlangObject) ret;
	        	}else if (ret instanceof Integer || ret instanceof Boolean){
	        		m_json = "{\"ok\":"+ ret.toString() + "}";
	        		reply[1] = new OtpErlangBinary(m_json.getBytes());
		    	}else if (ret instanceof java.util.Date || 
		    			  	  ret instanceof java.sql.Timestamp ||
		    			  	  ret instanceof Double){
		    		m_json = "{\"ok\":"+ EmsUtil.toJson(ret) + "}";
		    		reply[1] = new OtpErlangBinary(m_json.getBytes());
	        	}else if (ret instanceof String){
	            	reply[1] = new OtpErlangBinary(((String) ret).getBytes());
	        	}else if (ret instanceof List && ((List<?>) ret).isEmpty()){
	        		reply[1] = result_list_empty; 
	        	}else if (ret instanceof Object){
	            	reply[1] = new OtpErlangBinary(EmsUtil.toJson(ret).getBytes());
	            }else if (ret.getClass().getName().equals(ArrayList.class.getName())){
	            	List<?> lista = (List<?>) ret;
	            	OtpErlangObject[] otp_items = new OtpErlangObject[lista.size()];
	            	for(int i = 0; i < lista.size(); i++){
	            		otp_items[i] = new OtpErlangString((String) lista.get(i));
	            	}
	            	OtpErlangList otp_list = new OtpErlangList(otp_items);
	            	reply[1] = otp_list;
	            }
	    	}catch (Exception e){
	    		reply[1] = erro_convert_json;
	    	}
	    }else{
			reply[1] = result_null;
	    }
	    otp_result[0] = service_atom;
	    otp_result[1] = new OtpErlangLong(rid);
	    otp_result[2] = new OtpErlangTuple(reply);
	    OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
	    return myTuple;
	}

	/**
	 * Converte um objeto Java para um objeto de requisição Erlang
	 * @param ret objeto
	 * @param from pid do agente
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
	public static OtpErlangTuple serializeObjectToErlangRequest(Object ret, OtpErlangPid from){
	    OtpErlangObject[] otp_result = new OtpErlangObject[3];
		OtpErlangObject reply = null;
	    if (ret != null){
	    	try{
	        	String m_json = null;
	        	if (ret instanceof OtpErlangBinary){
	        		reply = (OtpErlangBinary) ret;
	        	}else if (ret instanceof OtpErlangAtom){
	        		reply = (OtpErlangObject) ret;
	        	}else if (ret instanceof Integer || ret instanceof Boolean){
	        		m_json = "{\"ok\":"+ ret.toString() + "}";
	        		reply = new OtpErlangBinary(m_json.getBytes());
		    	}else if (ret instanceof java.util.Date || 
		    			  	  ret instanceof java.sql.Timestamp ||
		    			  	  ret instanceof Double){
		    		m_json = "{\"ok\":"+ EmsUtil.toJson(ret) + "}";
		    		reply = new OtpErlangBinary(m_json.getBytes());
	        	}else if (ret instanceof String){
	            	reply = new OtpErlangBinary(((String) ret).getBytes());
	        	}else if (ret instanceof List && ((List<?>) ret).isEmpty()){
	        		reply = result_list_empty; 
	        	}else if (ret instanceof Object){
	            	reply = new OtpErlangBinary(EmsUtil.toJson(ret).getBytes());
	            }else if (ret.getClass().getName().equals(ArrayList.class.getName())){
	            	List<?> lista = (List<?>) ret;
	            	OtpErlangObject[] otp_items = new OtpErlangObject[lista.size()];
	            	for(int i = 0; i < lista.size(); i++){
	            		otp_items[i] = new OtpErlangString((String) lista.get(i));
	            	}
	            	OtpErlangList otp_list = new OtpErlangList(otp_items);
	            	reply = otp_list;
	            }
	    	}catch (Exception e){
	    		reply = erro_convert_json;
	    	}
	    }else{
			reply = result_null;
	    }
	    otp_result[0] = request_msg_atom;
	    otp_result[1] = new OtpErlangTuple(reply);
	    otp_result[2] = from;
	    OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
	    return myTuple;
	}

	public static Properties getConfig() throws FileNotFoundException, IOException{
		Properties props = new Properties();
		props.load(new FileInputStream("erlangms.conf"));
		return props;
	}
	
}
