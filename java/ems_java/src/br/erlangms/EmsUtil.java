/*********************************************************************
 * @title Módulo EmsUtil
 * @version 1.0.0
 * @doc Funções úteis   
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.ws.rs.client.ClientBuilder;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangInt;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;



public final class EmsUtil {
	public static final OtpErlangAtom ok_atom = new OtpErlangAtom("ok");
	public static final OtpErlangAtom error_atom = new OtpErlangAtom("error");
	public static final OtpErlangAtom request_msg_atom = new OtpErlangAtom("request");
	public static final OtpErlangBinary result_null = new OtpErlangBinary("{\"ok\":\"null\"}".getBytes());
	public static final OtpErlangBinary erro_convert_json = new OtpErlangBinary("{\"erro\":\"service\", \"message\" : \"Falha na serialização do conteúdo em JSON\"}".getBytes());
	public static final OtpErlangBinary result_list_empty = new OtpErlangBinary("[]".getBytes());
	public static final OtpErlangBinary result_ok = new OtpErlangBinary("{\"ok\":\"ok\"}".getBytes());
	public static final Logger logger = Logger.getLogger("erlangms");
	private static NumberFormat doubleFormatter = null;
	private static Gson gson = null;
	private static Gson gson2 = null;
	public static EmsProperties properties = null;
	private static final SimpleDateFormat dateFormatDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat dateFormatDDMMYYYY_HHmm = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final SimpleDateFormat dateFormatDDMMYYYY_HHmmss = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static MessageDigest messageDigestSHA1 = null;
	private static java.util.Base64.Encoder base64Encoder = null;
	static{
		doubleFormatter = NumberFormat.getInstance(Locale.US);
		doubleFormatter.setMaximumFractionDigits(2); 
		doubleFormatter.setMinimumFractionDigits(2);
		try {
			messageDigestSHA1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		base64Encoder = java.util.Base64.getEncoder(); 
		properties = getProperties();
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
			.registerTypeAdapter(String.class,  new JsonSerializer<String>() {   
			    @Override
			    public JsonElement serialize(String value, Type typeOfSrc, JsonSerializationContext context) {
			        return new JsonPrimitive(value.trim());
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
			            return new JsonPrimitive(dateFormatDDMMYYYY.format(value));
			        }else{
			        	if (value.getSeconds() == 0){
			        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmm.format(value));
			        	}else{
			        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmmss.format(value));
			        	}
			        }
			    }})					
			.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {   
			    @SuppressWarnings("deprecation")
				@Override
			    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
			    	if (value.getHours() == 0 && value.getMinutes() == 0){
			            return new JsonPrimitive(dateFormatDDMMYYYY.format(value));
			        }else{
			        	if (value.getSeconds() == 0){
			        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmm.format(value));
			        	}else{
			        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmmss.format(value));
			        	}
			        }
			    }})					
		    .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                    public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    	String value = json.getAsString();                            	
						final String m_erro = "Não é uma data válida.";
                    	try {
                    		int len_value = value.length();
                    		if (len_value >= 6 && len_value <= 10){
								return dateFormatDDMMYYYY.parse(value);
    						}else if (len_value == 16){
    							return dateFormatDDMMYYYY_HHmm.parse(value);
    						}else if (len_value == 19){
    							return dateFormatDDMMYYYY_HHmmss.parse(value);
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
                    		int len_value = value.length();
                    		if (len_value >= 6 && len_value <= 10){
								return new java.sql.Timestamp(dateFormatDDMMYYYY.parse(value).getTime());
    						}else if (len_value == 16){
    							return new java.sql.Timestamp(dateFormatDDMMYYYY_HHmm.parse(value).getTime());
    						}else if (len_value == 19){
    							return new java.sql.Timestamp(dateFormatDDMMYYYY_HHmmss.parse(value).getTime());
    						}else{
    							throw new EmsValidationException(m_erro);
    						}
						} catch (final ParseException e) {
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
		.registerTypeAdapter(String.class,  new JsonSerializer<String>() {   
		    @Override
		    public JsonElement serialize(String value, Type typeOfSrc, JsonSerializationContext context) {
		        return new JsonPrimitive(value.trim());
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
		            return new JsonPrimitive(dateFormatDDMMYYYY.format(value));
		        }else{
		        	if (value.getSeconds() == 0){
		        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmm.format(value));
		        	}else{
		        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmmss.format(value));
		        	}
		        }
		    }})					
		.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {   
		    @SuppressWarnings("deprecation")
			@Override
		    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
		    	if (value.getHours() == 0 && value.getMinutes() == 0){
		            return new JsonPrimitive(dateFormatDDMMYYYY.format(value));
		        }else{
		        	if (value.getSeconds() == 0){
		        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmm.format(value));
		        	}else{
		        		return new JsonPrimitive(dateFormatDDMMYYYY_HHmmss.format(value));
		        	}
		        }
		    }})					
	    .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                	String value = json.getAsString();                            	
					final String m_erro = "Não é uma data válida.";
                	try {
                		int len_value = value.length();
                		if (len_value >= 6 && len_value <= 10){
							return dateFormatDDMMYYYY.parse(value);
						}else if (len_value == 16){
							return dateFormatDDMMYYYY_HHmm.parse(value);
						}else if (len_value == 19){
							return dateFormatDDMMYYYY_HHmmss.parse(value);
						}else{
							throw new EmsValidationException(m_erro);
						}
					} catch (final ParseException e) {
						throw new EmsValidationException(m_erro);
					}
				}
            })    
	    .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                public java.sql.Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                	String value = json.getAsString();                            	
					final String m_erro = "Não é uma data válida.";
                	try {
                		int len_value = value.length();
                		if (len_value >= 6 && len_value <= 10){
							return new java.sql.Timestamp(dateFormatDDMMYYYY.parse(value).getTime());
						}else if (len_value == 16){
							return new java.sql.Timestamp(dateFormatDDMMYYYY_HHmm.parse(value).getTime());
						}else if (len_value == 19){
							return new java.sql.Timestamp(dateFormatDDMMYYYY_HHmmss.parse(value).getTime());
						}else{
							throw new EmsValidationException(m_erro);
						}
					} catch (final ParseException e) {
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
	
	public static boolean isAnyParameterAnnotated(Method method, Class<?> annotationType) {
	    final Annotation[][] paramAnnotations = method.getParameterAnnotations();    
	    for (Annotation[] annotations : paramAnnotations) {
	        for (Annotation an : annotations) {
	            if(an.annotationType().equals(annotationType)) {
	                return true;
	            }
	        }
	    }
	    return false;
	}	
	
	public static String getClassAnnotationValue(@SuppressWarnings("rawtypes") Class classType, @SuppressWarnings("rawtypes") Class annotationType, String attributeName) {
        String value = null;
        @SuppressWarnings("unchecked")
		Annotation annotation = classType.getAnnotation(annotationType);
        if (annotation != null) {
            try {
                value = (String) annotation.annotationType().getMethod(attributeName).invoke(annotation);
            } catch (Exception ex) {
            }
        }
        return value;
    }
	
	private static class SerializeStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> c) {
        	return false;
        }
        public boolean shouldSkipField(FieldAttributes f) {
        	OneToOne oneToOne = f.getAnnotation(OneToOne.class);
        	if (oneToOne != null && oneToOne.fetch() == FetchType.EAGER) {
        		return false;
        	}
        	
        	//OneToMany oneToMany = f.getAnnotation(OneToMany.class);
        	//if (oneToMany != null && oneToMany.fetch() == FetchType.EAGER) {
        	//	return true;
        	//}

        	return (f.getDeclaredType() == List.class ||
        			f.getAnnotation(OneToMany.class)   != null ||
        			f.getAnnotation(JoinTable.class)   != null ||
        			f.getAnnotation(ManyToMany.class)  != null);
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
	        }catch (final Exception e){
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
	public static <T> T fromJson(final String jsonString, final Class<T> classOfObj) {
		return (T) fromJson(jsonString, classOfObj, null);
	}

	/**
	 * Serializa um objeto a partir de uma string json.
	 * Quando a string json é vazio, apenas instância um objeto da classe.
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(final String jsonString, final Class<T> classOfObj, final EmsJsonModelAdapter jsonModelAdapter) {
		if (classOfObj != null){
			try {
				if (jsonString != null && !jsonString.isEmpty()){
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
							throw new EmsValidationException("Não suporta conversão do json da classe "+ classOfObj.getSimpleName() + ". Json: "+ jsonString);
						}
						setValuesFromMap(obj, values, jsonModelAdapter);
						return obj;
					}
				}
				return classOfObj.getConstructor().newInstance();
			}catch (JsonSyntaxException e) {
				throw new EmsValidationException("Sintáxe do JSON inválida.");
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new EmsValidationException("Não suporta instânciar objeto para a classe "+ classOfObj.getSimpleName());
			}
		}else{
			throw new EmsValidationException("Parâmetro classOfObj do método EmsUtil.fromJson não deve ser null.");
		}
	}

	/**
	 * Obtém uma lista a partir de um json
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @author Everton de Vargas Agilar
	 */
	public static <T> List<T> fromListJson(final String jsonString, final Class<T> classOfObj) {
		return fromListJson(jsonString, classOfObj, null);
	}
	 
	/**
	 * Obtém uma lista a partir de um json. Se o json estiver vazio, retorna apenas uma lista vazia.
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
 	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo 
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromListJson(final String jsonString, final Class<T> classOfObj, final EmsJsonModelAdapter jsonModelAdapter) {
		if (classOfObj != null) {
			ArrayList<T> newList = new ArrayList<T>();
			if (jsonString != null && !jsonString.isEmpty()){
				List<Object> values;
				try{
					values = gson.fromJson(jsonString, List.class);
				}catch (JsonSyntaxException e) {
					throw new EmsValidationException("Sintáxe do JSON inválida.");
				} 
				for (Object value : values){
					try {
						T obj = classOfObj.getConstructor().newInstance();
						setValuesFromMap(obj, (Map<String, Object>) value, jsonModelAdapter);
						newList.add(obj);
					} catch (Exception e) {
						throw new EmsValidationException("Não suporta conversão do json para "+ classOfObj.getSimpleName() + ". Json: "+ jsonString);
					}
				}
			}
			return newList;
		}else{
			throw new EmsValidationException("Parâmetro classOfObj do método EmsUtil.fromListJson não deve ser null.");
		}
	}
	
	/**
	 * Seta os valores nos parâmetros de um query a partir de um map
	 * @param query Instância da query com parâmetros a setar
	 * @param values	Map com chave/valor dos dados que serão aplicados na query
	 * @author Everton de Vargas Agilar
	 */
	public static void setQueryParameterFromMap(final Query query, final Map<String, Object> values){
		if (query != null && values != null && values.size() > 0){
			int p = 1;
			for (String field : values.keySet()){
				try{
					String[] field_defs = field.split("__");
					String field_name;
					String field_op;
					int field_len = field_defs.length;
					if (field_len == 1){
						field_name = field_defs[0];
						field_op = "=";
					}else if (field_len == 2){
						field_name = field_defs[0];
						field_op = field_defs[1];
						if (field_op.equals("isnull")){
							continue;
						}
					}else{
						throw new EmsValidationException("Campo de pesquisa "+ field + " inválido");
					}
					Object value_field = values.get(field);
					Class<?> paramType = query.getParameter(p).getParameterType();
					if (paramType == null){
						paramType = value_field.getClass();
					}
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
					}else if (paramType == Double.class || paramType == double.class){
						double valueDouble = parseAsDouble(value_field);
						query.setParameter(p++, Double.valueOf(valueDouble));
					}else if (paramType == String.class){
						String valueString;
						if (value_field instanceof Double){
							// Parece um inteiro? (termina com .0)
							if (value_field.toString().endsWith(".0")){
								valueString = Integer.toString(((Double)value_field).intValue());
							}else{
								valueString = value_field.toString();
							}
						}else{
							valueString = value_field.toString();
						}
						if (field_op.equals("contains")){
							valueString = "%"+ valueString + "%";
						}else if (field_op.equals("icontains")){
							valueString = "%"+ valueString.toLowerCase() + "%";
						}else if (field_op.equals("like")){
							valueString = valueString.toLowerCase() + "%";
						}else if (field_op.equals("ilike")){
							valueString = valueString.toLowerCase() + "%";
						}

						query.setParameter(p++, valueString);	
					}else if (paramType == Boolean.class){
						boolean value_boolean = parseAsBoolean(value_field);
						query.setParameter(p++, value_boolean);
					}else if (paramType == java.util.Date.class){
						final String m_erro = field_name + " não é uma data válida.";
						if (value_field instanceof String){
							int len_value = ((String) value_field).length();
							try {
								if (len_value >= 6 && len_value <= 10){
	                        		query.setParameter(p++, dateFormatDDMMYYYY.parse((String) value_field));
	    						}else if (len_value == 16){
	    							query.setParameter(p++, dateFormatDDMMYYYY_HHmm.parse((String) value_field));
	    						}else if (len_value == 19){
	    							query.setParameter(p++, dateFormatDDMMYYYY_HHmmss.parse((String) value_field));	    							
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
	 * Passando um objeto, retorna um Map<String, Object> com os campos do objeto.
	 * Se o obj já é um map não faz nada e apenas o retorna.
	 * @param obj Instância de um objeto
	 * @param values	Map com chave/valor dos campos do objeto
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> ObjectFieldsToMap(final Object obj){
		if (obj != null){
			if (obj instanceof Map){
				return (Map<String, Object>) obj;
			}else{
				Map<String, Object> map = new HashMap<String, Object>();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (Field field : fields){
					try {
						map.put(field.getName(), field.get(obj));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						field.setAccessible(true);
						try {
							map.put(field.getName(), field.get(obj));
						} catch (IllegalArgumentException | IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
				}
				return map;
			}
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsUtil.ObjectFieldsToMap.");
		}
	}
	
		
	
	/**
	 * Seta os valores no objeto a partir de um map.
	 * @param obj Instância de um objeto
	 * @param values	Map com chave/valor dos dados que serão aplicados no objeto
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object setValuesFromMap(final Object obj, final Map<String, Object> values, final EmsJsonModelAdapter jsonModelAdapter){
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
					}else if (tipo_field == Double.class || tipo_field == double.class){
						if (new_value instanceof String){
							field.set(obj, Double.parseDouble((String) new_value));
						}else if (new_value instanceof Double){
							field.set(obj, ((Double) new_value).doubleValue());
						}else{
							field.set(obj, ((Float) new_value));
						}
					}else if (tipo_field == Float.class || tipo_field == float.class){
						if (new_value instanceof String){
							field.set(obj, Float.parseFloat((String) new_value));
						}else if (new_value instanceof Double){
							field.set(obj, ((Double) new_value).floatValue());
						}else{
							field.set(obj, ((Float) new_value));
						}
					}else if (tipo_field == Long.class || tipo_field == long.class){
						if (new_value instanceof String){
							field.set(obj, Double.parseDouble((String) new_value));
						}else{
							field.set(obj, ((Double) new_value).longValue());
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
					}else if (tipo_field == Boolean.class || tipo_field == boolean.class){
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
								if (len_value == 0){
									field.set(obj, null);
								}
								else if (len_value >= 6 && len_value <= 10){
	                        		field.set(obj, dateFormatDDMMYYYY.parse((String) new_value));
	    						}else if (len_value == 16){
	                        		field.set(obj, dateFormatDDMMYYYY_HHmm.parse((String) new_value));
	    						}else if (len_value == 19){
	    							field.set(obj, dateFormatDDMMYYYY_HHmmss.parse((String) new_value));
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
								if (len_value == 0){
									field.set(obj, null);
								}
								else if (len_value >= 6 && len_value <= 10){
	                        		field.set(obj, new java.sql.Date(dateFormatDDMMYYYY.parse((String) new_value).getTime()));
	    						}else if (len_value == 16){
	                        		field.set(obj, new java.sql.Date(dateFormatDDMMYYYY_HHmm.parse((String) new_value).getTime()));
	    						}else if (len_value == 19){
	    							field.set(obj, new java.sql.Date(dateFormatDDMMYYYY_HHmmss.parse((String) new_value).getTime()));
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
								if (len_value == 0){
									new_time = null;
								}
								else if (len_value >= 6 && len_value <= 10){
	    							new_time = new java.sql.Timestamp(dateFormatDDMMYYYY.parse((String) new_value).getTime());
	    						}else if (len_value == 16){
	    							new_time = new java.sql.Timestamp(dateFormatDDMMYYYY_HHmm.parse((String) new_value).getTime());
	    						}else if (len_value == 19){
	    							new_time = new java.sql.Timestamp(dateFormatDDMMYYYY_HHmmss.parse((String) new_value).getTime());
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
							Enum<?> value = null;
							if (new_value instanceof String){
								try{
									idValue = Integer.parseInt((String) new_value);	
									value = intToEnum(idValue, (Class<Enum>) tipo_field);
								}catch (NumberFormatException e){
									value = StrToEnum((String) new_value, (Class<Enum>) tipo_field);
								}
							}else{
								idValue = ((Double) new_value).intValue();
								value = intToEnum(idValue, (Class<Enum>) tipo_field);
							}
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
		return obj;
	}
	

	/**
	 * Retorna o primeiro campo que encontrar a anotação passada como argumento.
	 * @param clazz Classe pojo. Ex.: OrgaoInterno.class
	 * @param ann	anotação que será pesquisada. Ex.: Id.class
	 * @return Field ou null se não encontrado
	 * @author Everton de Vargas Agilar
	 */
	public static Field findFieldByAnnotation(final Class<?> clazz, final Class<? extends Annotation> ann) {
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
	 * Retorna o id de um objeto. O id é um campo que tenha a anotação @Id
	 * @param clazz Classe pojo. Ex.: OrgaoInterno.class
	 * @param ann	anotação que será pesquisada. Ex.: Id.class
	 * @return Id ou null se não encontrado
	 * @author Everton de Vargas Agilar
	 */
	public static Integer getIdFromObject(final Object obj) {
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
	    	throw new EmsValidationException("Parâmetro Obj do método EmsUtil.getIdFromObject não deve ser null.");
	    }
	}	

	/**
	 * Converte um inteiro para a enumeração de acordo com clazz
	 * @param value código da enumeração
	 * @param clazz	classe da enumeração
	 * @return enumeração
	 * @author Everton de Vargas Agilar
	 */
	public static Enum<?> intToEnum(int value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
		if (clazz != null){
			if (value >= 0 && clazz != null){
				for(Enum<?> t : clazz.getEnumConstants()) {
			        if(t.ordinal() == value) {
			            return t;
			        }
			    }
			}
			throw new EmsValidationException("Valor inválido para o campo "+ clazz.getSimpleName());
		}else{
			throw new IllegalArgumentException("Parâmetro clazz do método EmsUtil.intToEnum não deve ser null.");
		}
	}

	/**
	 * Converte a descrição da enumeração para a enumeração de acordo com clazz
	 * @param value descrição da enumeração
	 * @param clazz	classe da enumeração
	 * @return enumeração
	 * @author Everton de Vargas Agilar
	 */
	public static Enum<?> StrToEnum(final String value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
		if (value != null && !value.isEmpty() && clazz != null){
			for(Enum<?> t : clazz.getEnumConstants()) {
		        if(t.name().equalsIgnoreCase(value)) {
		            return t;
		        }
		    }
			throw new EmsValidationException("Valor inválido para o campo "+ clazz.getSimpleName());
		}else{
			throw new IllegalArgumentException("Parâmetros clazz e value do método EmsUtil.StrToEnum não devem ser null.");
		}
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
	 * Converte um objeto Java para um oobjeto de response.
	 * Isso é utilizado para enviar a resposta para o barramento no formato nativo Erlang.
	 * @param ret objeto
	 * @param rid Request Id da requisição
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
	public static OtpErlangTuple serializeObjectToErlangResponse(final Object ret, final IEmsRequest request){
	    OtpErlangObject[] otp_result = new OtpErlangObject[3];
		OtpErlangObject[] reply = new OtpErlangObject[2];
	    boolean isEmsResponse = ret instanceof EmsResponse;
	    if (ret != null){
	    	try{
	        	String m_json = null;
	        	if (isEmsResponse){
	            	reply[1] = new OtpErlangBinary((((EmsResponse) ret).content).getBytes());
	        	}else if (ret instanceof OtpErlangBinary){
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
	        	}else if (ret instanceof byte[]){
	        		reply[1] = new OtpErlangBinary(ret);
	        	}else if (ret instanceof Object){
	            	m_json = EmsUtil.toJson(ret);
	        		reply[1] = new OtpErlangBinary(m_json.getBytes());
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
	    if (isEmsResponse){
	    	int code = ((EmsResponse) ret).code;
	    	reply[0] = code >= 400 ? error_atom : ok_atom;
	    	otp_result[0] = new OtpErlangInt(code);	
	    }else{
			reply[0] = ok_atom;
	    	if (request.getMetodo().equals("POST")){
	    		otp_result[0] = new OtpErlangInt(201);
	    	}else{
	    		otp_result[0] = new OtpErlangInt(200);
	    	}
	    }
	    otp_result[1] = new OtpErlangLong(request.getRID());
	    otp_result[2] = new OtpErlangTuple(reply);
	    OtpErlangTuple myTuple = new OtpErlangTuple(otp_result);
	    return myTuple;
	}

	/**
	 * Converte um objeto Java para um objeto de requisição no formato Erlang.
	 * @param ret objeto
	 * @param from pid de quem enviou mensagem
	 * @return OtpErlangTuple
	 * @author Everton de Vargas Agilar
	 */
	public static OtpErlangTuple serializeObjectToErlangRequest(final Object ret, final OtpErlangPid from){
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

	public static boolean isDateValid(final Date field){
		return (field != null ? true :  false);
	}

	public static boolean isDateFinalAfterOrEqualDateInitial(final Date dataIni, final Date dataFinal){
		return (dataFinal != null && dataIni != null && (dataFinal.equals(dataIni) || dataFinal.after(dataIni)) ? true :  false);
	}
	
	public static boolean isDateFinalAfterDateInitial(final Date dataIni, final Date dataFinal){
		return (dataFinal != null && dataIni != null && dataFinal.after(dataIni) ? true :  false);
	}

	public static boolean isFieldStrValid(final String field){
		return (field != null && !field.isEmpty() ? true : false);
	}
	
	public static boolean isFieldStrValid(final String field, int maxLength){
		return (field != null && !field.isEmpty() && field.length() <= maxLength ? true : false);
	}

	public static boolean isFieldObjectValid(final Object obj){
		return (obj != null  ? true : false);
	}

	public static Object mergeObjects(final Object obj1, final Object obj2){
		return mergeObjects(obj1, obj2, null);
	}

	public static Object mergeObjects(final Object obj1, final Object obj2, final EmsJsonModelAdapter jsonModelAdapter){
		Map<String, Object> values = ObjectFieldsToMap(obj2);
		return setValuesFromMap(obj1, values, jsonModelAdapter);
	}

	public static String fieldOperatorToSqlOperator(final String fieldOperator){
		switch (fieldOperator){
			case "contains": return " like ";  
			case "icontains": return " like ";
			case "like": return " like "; 
			case "ilike": return " like "; 
			case "gt": return " > "; 
			case "gte": return " >= ";
			case "lt": return " < "; 
			case "lte": return " <= ";  
			case "e": return " = ";
			case "ne": return " != ";
			case "isnull": return " is null ";
			case "equal": return " = ";
		}
		throw new EmsValidationException("Operador do campo de pesquisa "+ fieldOperator + " inválido.");
	}
	
	public static String listFunctionToSqlFunction(final List<String> listFunction){
		if (listFunction != null){
			if (listFunction.isEmpty() || (listFunction.size() != 2)){
				throw new EmsValidationException("Função SQL precisa de um operador e de uma coluna para EmsUtil.listFunctionToSqlFunction.");
			}
			String function = listFunction.get(0);
			switch (function){
				case "avg": return " avg (" + listFunction.get(1) + ") ";  
				case "count": return " count (" + listFunction.get(1) + ") ";
				case "first": return " first (" + listFunction.get(1) + ") ";
				case "last": return " last (" + listFunction.get(1) + ") "; 
				case "max": return " max (" + listFunction.get(1) + ") "; 
				case "min": return " min (" + listFunction.get(1) + ") ";
				case "sum": return " sum (" + listFunction.get(1) + ") ";
			}
			throw new EmsValidationException("Função SQL "+ function + " inválido para EmsUtil.listFunctionToSqlFunction.");
		}else{
			throw new EmsValidationException("Parâmetro listFunction não pode ser null para EmsUtil.listFunctionToSqlFunction.");
		}
	}
	
	/**
	 * Parse um objeto String, Double ou Boolean em um valor boolean.
	 * @param value_field valor String, Double ou Boolean.
	 * @return boolean 
	 * @author Everton de Vargas Agilar (revisão)
	 */
	public static boolean parseAsBoolean(final Object value_field){
		if (value_field == null){
			return false;
		}else if (value_field instanceof String){
			if (((String) value_field).equalsIgnoreCase("true")){
				return true;	
			}else if  (((String) value_field).equalsIgnoreCase("false")){
				return false;
			}else if  (((String) value_field).equalsIgnoreCase("1")){
				return true;
			}else if  (((String) value_field).equalsIgnoreCase("0")){
				return false;
			}else if  (((String) value_field).equalsIgnoreCase("sim")){
				return true;
			}else if  (((String) value_field).equalsIgnoreCase("1.0")){
				return true;
			}else if  (((String) value_field).equalsIgnoreCase("yes")){
				return true;
			}else{
				return false;
			}
		}else if (value_field instanceof Double){
			if (value_field.toString().equals("1.0")){
				return true;
			}else{
				return false;
			}
		}else if (value_field instanceof Boolean){
			return ((Boolean) value_field).booleanValue();
		}else{
			return false;
		}
	}

	/**
	 * Parse um objeto String, Double ou Float em um valor Double.
	 * @param value_field valor String, Double ou Float.
	 * @return Double ou null 
	 * @author Everton de Vargas Agilar (revisão)
	 */
	public static Double parseAsDouble(final Object value_field){
		if (value_field != null){
			if (value_field instanceof String){
				return Double.parseDouble((String) value_field);
			}else if (value_field instanceof Double){
				return ((Double) value_field).doubleValue();
			}else{
				return ((Float) value_field).doubleValue();
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Gera um relatório no formato pdf a partir de um template jasper. 
	 * É importante o contrato do serviço declarar "content_type" : "application/pdf" para que o browser exiba o pdf corretamente.
	 * @param params parâmetros do relatório. Pode ser um model ou Map<String, Object> ou null. 
	 * @param datasource é o objeto ou lista de objetos do relatório. Pode ser null.
	 * @param templateJasper arquivo .jasper do template criado no Ireport. É obrigatório. Ex.: "/relatorios/DeclaracaoAlunoRegular.jasper"
	 * @param owner referência para objeto quem invoca esse metodo. Utilizado para owner.getClass().getResourceAsStream() 	
	 * @return byte[] retorna um fluxo de bytes que deve ser tratado pelo frontend para geração do pdf
	 * @author Fabiano Rodrigues de Paiva
	 * @author Everton de Vargas Agilar (revisão)
	 */
	@SuppressWarnings("unchecked")
	public static byte[] printPdf(final Object params, 
								  final Object datasource, 
								  final String templateJasper, 
								  final Object owner){
		if ((templateJasper != null && !templateJasper.isEmpty()) && owner != null) {
			try {		
				Map<String, Object> paramsMap = null;
				List<Object> objectList = null;
				InputStream streamTemplateJasper = owner.getClass().getResourceAsStream(templateJasper);	//com ajuda do owner recupero o caminho onde está o relatório no projeto de quem invocou esse método	
				if (streamTemplateJasper != null){
					JasperReport jr = (JasperReport)JRLoader.loadObject(streamTemplateJasper);						
					
					if (params != null){
						// É esperado um objeto ou Map. Converte para Map se for necessário
						if (params instanceof Map){
							paramsMap = (Map<String, Object>) params;
						}else{
							paramsMap = EmsUtil.ObjectFieldsToMap(params);
						}
					}

					if (datasource != null){
						// É esperado um objeto ou lista de objetos
						if (datasource instanceof List){
							objectList = (List<Object>) datasource;
						}else{
							objectList = new ArrayList<Object>();
							objectList.add(datasource);
						}
					}

					JRDataSource datasourceList = new JRBeanCollectionDataSource(objectList);			
					JasperPrint jasperPrint = JasperFillManager.fillReport(jr, paramsMap, datasourceList);
					return JasperExportManager.exportReportToPdf(jasperPrint);
				}else{
					throw new EmsValidationException("Não foi possível encontrar o templateJasper "+ templateJasper);	
				}
			}catch (net.sf.jasperreports.engine.util.JRFontNotFoundException e){
				throw new EmsValidationException("Não foi possível gerar o pdf pois as fontes utilizadas não foram encontradas no servidor. Erro interno: "+ e.getLocalizedMessage());
			}catch (Exception e) {
				e.printStackTrace();
				throw new EmsValidationException("Não foi possível gerar o pdf pois um erro interno ocorreu: "+ e.getLocalizedMessage());
			}
		}else{
			throw new EmsValidationException("Parâmetros params ou listaObj, templateJasper e owner devem ser informados para EmsUtil.printPdf");
		}
	}

	/**
	 * Obter o array de unique constraints de um model  
	 * @return array of UniqueConstraint[]
	 * @author Everton de Vargas Agilar
	 */
	public static UniqueConstraint[] getTableUniqueConstraints(final Class<?> classOfModel){
		if (classOfModel != null){
			Table tableAnnotation = classOfModel.getAnnotation(Table.class);
			return  tableAnnotation.uniqueConstraints();
		}else{
			throw new EmsValidationException("Parâmetro classOfModel não pode ser null para EmsUtil.getTableUniqueConstraints.");
		}
	}

	/**
	 * Obter a lista de fields com unique constraint de um model.
	 * Obs.: Id não é retornado embora tenha a constraint unique.  
	 * @return List<Field> 
	 * @author Everton de Vargas Agilar
	 */
	public static List<Field> getFieldsWithUniqueConstraint(final Class<?> classOfModel){
		if (classOfModel != null){
			Field[] fields = classOfModel.getDeclaredFields();
			List<Field> result = new ArrayList<>();
			for (int i = 0; i < fields.length; i++){
				Field field = fields[i];
				if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique() && !field.isAnnotationPresent(Id.class)){
					result.add(field);
				}
			}
			return result;
		}else{
			throw new EmsValidationException("Parâmetro classOfModel não pode ser null para EmsUtil.getFieldsWithUniqueConstraint.");
		}
	}


	/**
	 * Obter a lista de fields de um model.
	 * Obs.: somente fields com a anotação Column são retornados.  
	 * @return List<Field> 
	 * @author Everton de Vargas Agilar
	 */
	public static List<Field> getFieldsFromModel(final Class<?> classOfModel){
		if (classOfModel != null){
			Field[] fields = classOfModel.getDeclaredFields();
			List<Field> result = new ArrayList<>();
			for (int i = 0; i < fields.length; i++){
				Field field = fields[i];
				if (field.isAnnotationPresent(Column.class)){
					result.add(field);
				}
			}
			return result;
		}else{
			throw new EmsValidationException("Parâmetro classOfModel não pode ser null para EmsUtil.getFieldsFromModel.");
		}
	}

	
	/**
	 * Realiza uma pesquisa LDAP v3 para localizar um objeto pelo seu login.
	 * 
	 * O processo ems_ldap_server precisa estar habilitado no barramento, caso ele seja o servidor LDAP.
	 * 
	 * As seguintes informações são lidas das propriedades. Verifique EmsUtil.properties.
	 * 	  -Dems_ldap_admin_passwd="123456"
 	 *	  -Dems_ldap_admin="cn=admin,dc=unb,dc=br"
	 *    -Dems_ldap_url="ldap://localhost:2389"
	 *    
 	 * Obs.: As properties devem ser incluídas no arquivo standalone.conf do JBoss/Wildfly ou na IDE Eclipse Open Launch Configuration/VM Arguments   
	 *    
	 * @param login do usuário. Ex: geral
	 * @return user object ou exception EmsValidationException
	 * @author Everton de Vargas Agilar
	 */
	public static Object ldapSearch(final String login) {
		if (login != null){
			Hashtable<String, String> env = new Hashtable<String, String>(11);
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, properties.ldapUrl);  
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, properties.ldapAdmin);
			env.put(Context.SECURITY_CREDENTIALS, properties.ldapAdminPasswd);  // admin password in ems_ldap_server.json catalog
			LdapContext ctx = null;
			try{
				ctx = new InitialLdapContext(env, null);
				try{
					NamingEnumeration<SearchResult> answer = ctx.search("dc=unb,dc=br", "uid="+ login, null);
					return answer.next().getAttributes();
				}finally{
					ctx.close();
				}
			} catch (javax.naming.AuthenticationException e) {
				throw new EmsValidationException("Invalid ldap admin credentials.");
			} catch (NamingException e){
				throw new EmsValidationException("Não foi possível pesquisar usuário no servidor LDAP "+ properties.ldapUrl);
			}
		}else{
			throw new EmsValidationException("Parâmetro login não pode ser null para EmsUtil.ldapSearch.");
		}
	}

	
	/**
	 * Realiza a conversão de um List<Object> para um List<Map<String, Object>>.
	 * 
	 * Para que seja posśivel a conversão é necessário passar a lista dos campos (fields).
	 * 
	 * @param fields lista de campos. Pode ser passado como um array de campos, string de campos separado por vírgula ou lista de campos.
	 * @return List<Map<String, Object>> ou exception EmsValidationException
	 * @author Everton de Vargas Agilar, 
	 * @author Rogério Guimarães Sampaio
	 */
	public static List<Map<String, Object>> ListObjectToListMap(final Object fields, final List<?> listObj){		
		if (fields != null && listObj != null){
			String[] fieldNames = null;
			if (fields instanceof String){
				fieldNames = ((String)fields).split(",");
			}else if (fields instanceof String[] ){
				fieldNames = (String[])fields;
			}else if (fields instanceof List<?>){
				fieldNames = (String[]) ((List<?>)fields).toArray();
			}else{
				throw new EmsValidationException("Parâmetro fields não é do tipo correto para EmsUtil.ListObjectToListMap.");
			}
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(listObj.size());
			int colSize = fieldNames.length;
			for(Object obj : listObj){
				int index = 0;
				Map<String, Object> objVo = new HashMap<>(colSize);
				for (String fieldName : fieldNames) {			
					objVo.put(fieldName, ((Object[])obj)[index++]);
				}
				result.add(objVo);
			}			
			return result;
		}else{
			throw new EmsValidationException("Parâmetros fields e ListObj não podem ser null para EmsUtil.ListObjectToListMap.");
		}
	}	

	/**
	 * Classe que armazena as propriedades para o SDK.
	 *  
	 * Essas informações são carregadas durante durante o deployment do arquivo standalone.conf no JBoss/Wildfly.
	 * 
	 * No servidor JBoss/Wildfly, estão definidas em configuration/standalone/standalone.conf. As properties 
	 * podem ser incluídas também na IDE Eclipse Open Launch Configuration/VM Arguments   
	 *
	 * 
	 * Definições de propriedade no JBoss/Widfly: 
	 *    -Dcookie=erlangms
	 *    -Dems_node=node01
	 *    -Dems_emsbus=http://localhost:2301
	 *    -Dems_cookie=erlangms
	 *    -Dems_max_thread_pool_by_agent=100
	 *    -Dems_user=xxxxxxx 
	 *    -Dems_ldap_admin_passwd="xxxxxxx"
 	 *	  -Dems_ldap_admin="cn=admin,dc=unb,dc=br"
	 *    -Dems_ldap_url="ldap://localhost:2389"
	 *    -Dems_smtp_passwd="xxxxxxxx"
	 *    -Dems_smtp_from="erlangms@unb.br"
	 *    -Dems_smtp_port=587
	 *    -Dems_smtp="mail.unb.br"

	 *  
	 * @author Everton de Vargas Agilar
	 */
    public static class EmsProperties{
		public int maxThreadPool;		
		public String ems_bus_node1;	   // Ex.: default is ems_bus
		public String cookie;	 		   // Ex: erlangms
    	public String ESB_URL;			   // Ex: http://localhost:2301
    	public String hostName;	
    	public  String nodeName;
    	public String nodeUser;
    	public String nodePasswd;
        public String authorizationHeaderName;
        public String authorizationHeaderValue;
        public boolean debug;
        public int msg_timeout = 60000;
		
        // smtp
        public int smtpPort;			  // Ex: 25
		public String smtp;				  // Ex: smtp.unb.br
		public String smtpFrom;			  // Ex: evertonagilar@unb.br
		public String smtpPasswd;		  

		// ldap
    	public String ldapUrl;				// Ex: ldap://localhost:2389
		public String ldapAdmin;			// Ex: cn=admin,dc=unb,dc=br
		public String ldapAdminPasswd;		// Ex: 123456
    }
    
	/**
	 * Retorna as propriedades para o SDK do barramento de serviços ERLANGMS.
	 * 
	 * Exemplo: 
	 *    -Dcookie=erlangms
	 *    -Dems_node=node01
	 *    -Dems_emsbus_url=http://localhost:2301
	 *    -Dems_emsbus=ems_bus
	 *    -Dems_cookie=erlangms
	 *    -Dems_max_thread_pool_by_agent=100
	 *    -Dems_debug=false
	 *    -Dems_user=xxxxxxx 
	 *    -Dems_ldap_admin_passwd="xxxxxx"
 	 *	  -Dems_ldap_admin="cn=admin,dc=unb,dc=br"
	 *    -Dems_ldap_url="ldap://localhost:2389"
	 *    -Dems_smtp_passwd="xxxxxxxx"
	 *    -Dems_smtp_from="erlangms@unb.br"
	 *    -Dems_smtp_port=587
	 *    -Dems_smtp="mail.unb.br"
	 * @author Everton de Vargas Agilar
	 */
	private static EmsProperties getProperties() {
		EmsProperties prop = new EmsProperties();
		String tmp_thread_pool = System.getProperty("ems_thread_pool");
		if (tmp_thread_pool != null){
			try{
				prop.maxThreadPool = Integer.parseInt(tmp_thread_pool);
			}catch (NumberFormatException e){
				prop.maxThreadPool = 12;
			}
		}else{
			prop.maxThreadPool = 12;
		}
		
		String tmp_cookie = System.getProperty("ems_cookie");
		if (tmp_cookie != null){
		   prop.cookie = tmp_cookie;
	   }else{
		   prop.cookie = "erlangms";
	   }

	   String tmp_host = System.getProperty("ems_host");
	   if (tmp_host != null){
		   prop.hostName = tmp_host;
	   }else{
		   try {
			   prop.hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				prop.hostName = "localhost";
				System.out.println("Não foi possível obter o hostname da máquina onde está o node. Usando localhost.");
			}
	   }

	   if (System.getProperty("ems_debug", "false").equalsIgnoreCase("true")){
		   prop.debug = true;
	   }else{
		   prop.debug = false;
	   }

	   String tmp_nodeName = System.getProperty("ems_node");
	   if (tmp_nodeName != null){
		   prop.nodeName = tmp_nodeName;
	   }else{
		   prop.nodeName = "node01";
	   }
	   
	   String tmp_ESB_URL = System.getProperty("ems_bus_url");
	   if (tmp_ESB_URL != null){
		   if (tmp_ESB_URL.indexOf(":") == -1){
			   tmp_ESB_URL = tmp_ESB_URL + ":2301";
		   }
		   prop.ESB_URL = tmp_ESB_URL;
	   }else{
		   prop.ESB_URL = "http://localhost:2301";
	   }
	   
	   String tmp_ems_bus_node1 = System.getProperty("ems_bus_node1");
	   if (tmp_ems_bus_node1 != null){
		   prop.ems_bus_node1 = tmp_ems_bus_node1;
	   }else{
		   prop.ems_bus_node1 = "ems_bus";
	   }

	   
	   String tmp_user = System.getProperty("ems_user");
	   if (tmp_user != null){
		   prop.nodeUser = tmp_user;
	   }else{
		   prop.nodeUser = "geral";
	   }
	   
	   String tmp_password = System.getProperty("ems_password");
	   if (tmp_password != null){
		   prop.nodePasswd = tmp_password;
	   }else{
		   prop.nodePasswd = "123456";
	   }
	   
       String usernameAndPassword = prop.nodeUser + ":" + prop.nodePasswd;
       prop.authorizationHeaderName = "Authorization";
       prop.authorizationHeaderValue = "Basic " +  toBase64(usernameAndPassword);

       // SMTP properties
       
	   String tmp_smtp = System.getProperty("ems_smtp");
	   if (tmp_smtp != null){
		   prop.smtp = tmp_smtp;
	   }else{
		   prop.smtp = "smtp.unb.br";
	   }

	   String tmp_smtp_port = System.getProperty("ems_smtp_port");
	   if (tmp_smtp_port != null){
		   try{
			   prop.smtpPort = Integer.parseInt(tmp_smtp_port);
		   } catch(NumberFormatException e){
			   prop.smtpPort = 25;
		   }
	   }else{
		   prop.smtpPort = 25;
	   }
       
	   String tmp_smtp_from = System.getProperty("ems_smtp_from");
	   if (tmp_smtp_from != null){
		   prop.smtpFrom = tmp_smtp_from;
	   }else{
		   prop.smtpFrom = "erlangms@unb.br";
	   }

	   String tmp_smtp_passwd = System.getProperty("ems_smtp_passwd");
	   if (tmp_smtp_passwd != null){
		   prop.smtpPasswd = tmp_smtp_passwd;
	   }else{
		   prop.smtpPasswd = "123456";
	   }

	   // LDAP properties
	   
	   String tmp_ldap_url = System.getProperty("ems_ldap_url");
	   if (tmp_ldap_url != null){
		   if (tmp_ldap_url.indexOf(":") == -1){
			   tmp_ldap_url = tmp_ldap_url + ":2389";
		   }
		   if (!tmp_ldap_url.startsWith("ldap://")){
			   tmp_ldap_url = "ldap://" + tmp_ldap_url;
		   }
		   prop.ldapUrl = tmp_ldap_url;
	   }else{
		   prop.ldapUrl = "ldap://localhost:2389";
	   }

	   String tmp_ldap_admin = System.getProperty("ems_ldap_admin");
	   if (tmp_ldap_admin != null){
		   prop.ldapAdmin = tmp_ldap_admin;
	   }else{
		   prop.ldapAdmin = "cn=admin,dc=unb,dc=br";
	   }

	   String tmp_ldap_admin_passwd = System.getProperty("ems_ldap_admin_passwd");
	   if (tmp_ldap_admin_passwd != null){
		   prop.ldapAdminPasswd = tmp_ldap_admin_passwd;
	   }else{
		   prop.ldapAdminPasswd = "123456";
	   }

		String tmp_msg_timeout = System.getProperty("ems_msg_timeout");
		if (tmp_msg_timeout != null){
			try{
				prop.msg_timeout = Integer.parseInt(tmp_msg_timeout);
			}catch (NumberFormatException e){
				prop.msg_timeout = 60000;
			}
		}else{
			prop.msg_timeout = 60000;
		}

	   return prop;
	}

    private static class MyAuthenticator  extends Authenticator{
        private String userName = null;
        private String password = null;

        public MyAuthenticator(final String username, final String password) {
            this.userName = username;
            this.password = password;
        }
        protected PasswordAuthentication getPasswordAuthentication(){
            return new PasswordAuthentication(userName, password);
        }
    }	
    
    private static class MailSenderInfo {
        private String mailServerHost;
        private String mailServerPort = "25";
        private String fromAddress;
        private String toAddress;
        private String userName;
        private String password;
        private boolean validate = false;
        private String subject;
        private String content;
        private String[] attachFileNames={};

        private boolean withAttachment=true;
        public boolean isWithAttachment() {
            return withAttachment;
        }

        public void setWithAttachment(boolean withAttachment) {
            this.withAttachment = withAttachment;
        }

        public Properties getProperties(){
            Properties p = new Properties();
            p.put("mail.smtp.host", this.mailServerHost);
            p.put("mail.smtp.port", this.mailServerPort);
            p.put("mail.smtp.auth", validate ? "true" : "false");
            return p;
        }

        public void setMailServerHost(String mailServerHost) {
            this.mailServerHost = mailServerHost;
        }

        public void setMailServerPort(String mailServerPort) {
            this.mailServerPort = mailServerPort;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }

        public String getToAddress() {
            return toAddress;
        }

        public void setToAddress(String toAddress) {
            this.toAddress = toAddress;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isValidate() {
            return validate;
        }

        public void setValidate(boolean validate) {
            this.validate = validate;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String[] getAttachFileNames() {
            return attachFileNames;
        }

        public void setAttachFileNames(String[] attachFileNames) {
            this.attachFileNames = attachFileNames;
        }
    }    


	/**
	 * Permite enviar um e-mail em formato texto com uma lista de anexos.
	 * 
	 * Exemplo sem anexo: 
	 * 		EmsUtil.sendTextMail("evertonagilar@gmail.com", "isso é um teste", "conteúdo do email", null);
	 * 
	 * Exemplo com anexo:
	 * 		String[] anexos = new String[1];
	 *		anexos[0] = "/home/everton/desenvolvimento/erlangms/ems-bus/build.sh";
	 *		EmsUtil.sendTextMail("evertonagilar@gmail.com", "isso é um teste", "conteúdo do email", anexos);
     *
	 * 
	 * As seguintes informações são lidas das properties. Verifique EmsUtil.properties:
	 *    -Dems_smtp_passwd="xxxxxxxx"
	 *    -Dems_smtp_from="erlangms@unb.br"
	 *    -Dems_smtp_port=587
	 *    -Dems_smtp="mail.unb.br"
	 *    
	 * Obs.: As properties devem ser incluídas no arquivo standalone.conf do JBoss/Wildfly ou na IDE Eclipse Open Launch Configuration/VM Arguments   
	 * 
	 * @param to  para quem vai ser enviado o e-mail.
	 * @param subject título do e-mail. (Obrigatório)
	 * @param content conteúdo do e-mail. (Obrigatório)
	 * @param attachment lista de arquivos para anexar do e-mail. (Opcional)
	 * @author Everton de Vargas Agilar
	 */
    public static void sendTextMail(final String to, 
    								final String subject, 
    								final String content,
    								final String[] attachment) {
    	MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(properties.smtp);
        mailInfo.setMailServerPort(Integer.toString(properties.smtpPort));
        mailInfo.setValidate(true);
        mailInfo.setUserName(properties.smtpFrom);
        mailInfo.setPassword(properties.smtpPasswd);
        mailInfo.setFromAddress(properties.smtpFrom);
        mailInfo.setToAddress(to);
        mailInfo.setSubject(subject);
        mailInfo.setContent(content);
        if (attachment != null && attachment.length != 0){
            mailInfo.setAttachFileNames(attachment);
        }else{
            mailInfo.setWithAttachment(false);
        }
        
        MyAuthenticator authenticator = null;
        Properties pro = mailInfo.getProperties();
        if (mailInfo.isValidate()) {
            authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }

        Session sendMailSession = Session.getDefaultInstance(pro,authenticator);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(from);
            Address addr = new InternetAddress(mailInfo.getToAddress());
            mailMessage.setRecipient(Message.RecipientType.TO, addr);
            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new Date());
            String mailContent = mailInfo.getContent();
            mailMessage.setText(mailContent);
            Multipart multipart=new MimeMultipart();
            if(mailInfo.isWithAttachment()){
                for(int i=0;i<mailInfo.getAttachFileNames().length;i++) {
                    DataSource source = new FileDataSource(mailInfo.getAttachFileNames()[i]);
                    BodyPart bodyPart=new MimeBodyPart();
                    bodyPart.setDataHandler(new DataHandler(source));
                    String[]ss=mailInfo.getAttachFileNames()[i].split("/");
                    bodyPart.setFileName(ss[ss.length-1]);
                    multipart.addBodyPart(bodyPart);
                }
                BodyPart bodyPart=new MimeBodyPart();
                bodyPart.setContent(mailInfo.getContent(),"text/html");
                multipart.addBodyPart(bodyPart);
                mailMessage.setContent(multipart);
            }
            Transport.send(mailMessage);
        } catch (MessagingException ex) {
        	throw new EmsValidationException("Não foi possível enviar e-mail para "+ to + ". Erro interno: "+ ex.getMessage());
        }
    }

	/**
	 * Permite enviar um e-mail em formato HTML com uma lista de anexos. (private porque ainda não funciona)
	 * Ex.: EmsUtil.sendHtmlMail("evertonagilar@gmail.com", "isso é um teste", "<h1>contedo do email<h1>", null);
	 * 
	 * As seguintes informações são lidas das properties. Verifique EmsUtil.properties:
	 *    -Dems_smtp_passwd="xxxxxxxx"
	 *    -Dems_smtp_from="erlangms@unb.br"
	 *    -Dems_smtp_port=587
	 *    -Dems_smtp="mail.unb.br"
	 *    
	 * Obs.: As properties devem ser incluídas no arquivo standalone.conf do JBoss/Wildfly ou na IDE Eclipse Open Launch Configuration/VM Arguments.
	 *    
	 * @param to  para quem vai ser enviado o e-mail.
	 * @param subject título do e-mail. (Obrigatório)
	 * @param content conteúdo do e-mail. (Obrigatório)
	 * @param attachment lista de arquivos para anexar do e-mail. (Opcional)
	 * @author Everton de Vargas Agilar
	 */
    @SuppressWarnings("unused")
	private static void sendHtmlMail(final String to, 
									 final String subject, 
									 final String content,
									 final String[] attachment){
    	MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(properties.smtp);
        mailInfo.setMailServerPort(Integer.toString(properties.smtpPort));
        mailInfo.setValidate(true);
        mailInfo.setUserName(properties.smtpFrom);
        mailInfo.setPassword(properties.smtpPasswd);
        mailInfo.setFromAddress(properties.smtpFrom);
        mailInfo.setToAddress(to);
        mailInfo.setSubject(subject);
        mailInfo.setContent(content);
        if (attachment != null && attachment.length != 0){
            mailInfo.setAttachFileNames(attachment);
        }else{
            mailInfo.setWithAttachment(false);
        }

    	
        MyAuthenticator authenticator = null;
        Properties pro = mailInfo.getProperties();
        if (mailInfo.isValidate()) {
        	authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }
        Session sendMailSession = Session.getDefaultInstance(pro,authenticator);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(from);
            Address addr = new InternetAddress(mailInfo.getToAddress());
            mailMessage.setRecipient(Message.RecipientType.TO, addr);
            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new Date());
            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            mailMessage.setContent(mainPart);

            BodyPart bodyPart=new MimeBodyPart();
            Multipart multipart=new MimeMultipart();
            if(mailInfo.getAttachFileNames().length!=0){
//                    for(int i=0;i<mailInfo.getAttachFileNames().length;i++) {
                DataSource source = new FileDataSource(mailInfo.getAttachFileNames()[0]);
                bodyPart.setDataHandler(new DataHandler(source));
                bodyPart.setFileName(mailInfo.getAttachFileNames()[0]);
                multipart.addBodyPart(bodyPart);
//                    }
            }
            mailMessage.setContent(multipart);
            Transport.send(mailMessage);
        } catch (MessagingException ex) {
        	throw new EmsValidationException("Não foi possível enviar e-mail para "+ to + ". Erro interno: "+ ex.getMessage());
        }
    }


	/**
	 * Algoritmo SHA-1
	 * @author Everton de Vargas Agilar
	 */
    public static String toSHA1(final String value) {
        if (value != null){
        	return new String(messageDigestSHA1.digest(value.getBytes()));
        }else{
        	return "";
        }
    }
    
	/**
	 * Algoritmo base64
	 * @author Everton de Vargas Agilar
	 */
    public static String toBase64(final String value){
    	if (value != null){
    		return base64Encoder.encodeToString(value.getBytes());
    	}else{
    		return "";
    	}
    }

    
	/**
	 * Classe responsável por representar um filtro após o parser.
	 * @author Everton de Vargas Agilar
	 */
    public static class EmsFilterStatement {
    	StringBuilder where = null;
    	Map<String, Object> filtro_obj = null;
    	public EmsFilterStatement(final StringBuilder where, final Map<String, Object> filtro_obj){
    		this.where = where;
    		this.filtro_obj = filtro_obj;
    	}
    }
    
	/**
	 * Faz o parser do parâmetro filter e retorna a cláusula where para um sql nativo.
	 * Se não for informado o parâmetro filter ou o filtro for vazio, retorna null.
	 * Se o filter estiver com sintáxe incorreta, retorna exception EmsValidationException.
	 * @param filter filtro. Ex.: {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @return filtro ou null
	 * @author Everton de Vargas Agilar
	 */
    @SuppressWarnings("unchecked")
	public static EmsFilterStatement parseSqlNativeFilter(final String filter){
    	if (filter != null && filter.length() > 5){
			try{
				StringBuilder where = null;
				Map<String, Object> filtro_obj = null;
				boolean useAnd = false; 
				filtro_obj = (Map<String, Object>) EmsUtil.fromJson(filter, HashMap.class);
				where = new StringBuilder(" where ");
				for (String field : filtro_obj.keySet()){
					if (useAnd){
						where.append(" and ");
					}
					String[] field_defs = field.split("__");
					String fieldName;
					String fieldOperator;
					String sqlOperator;
					int field_len = field_defs.length; 
					if (field_len == 1){
						fieldName = field;
						fieldOperator = "=";
						sqlOperator = "=";
					} else if (field_len == 2){
						fieldName = field_defs[0];
						fieldOperator = field_defs[1];
						sqlOperator = EmsUtil.fieldOperatorToSqlOperator(fieldOperator);
					}else{
						throw new EmsValidationException("Campo de pesquisa "+ field + " inválido.");
					}
					if (field_len == 2){
						if (fieldOperator.equals("isnull")){
							boolean fieldBoolean = EmsUtil.parseAsBoolean(filtro_obj.get(field)); 
							if (fieldBoolean){
								where.append(fieldName).append(" is null ");
							}else{
								where.append(fieldName).append(" is not null ");
							}
						} else if(fieldOperator.equals("icontains") || fieldOperator.equals("ilike")){
							fieldName = String.format("lower(this.%s)", fieldName);
							where.append(fieldName).append(sqlOperator).append("?");
						}else{
							fieldName = String.format("this.%s", fieldName);
							where.append(fieldName).append(sqlOperator).append("?");
						}
					}else{
						fieldName = String.format("this.%s", fieldName);
						where.append(fieldName).append(sqlOperator).append("?");
					}
					useAnd = true;
				}
				return new EmsFilterStatement(where, filtro_obj);
			}catch (Exception e){
				throw new EmsValidationException("Filtro da pesquisa inválido. Erro interno: "+ e.getMessage());
			}
    	}else{
    		return null;
    	}
    }
    
}
