/*********************************************************************
 * @title Módulo EmsUtil
 * @version 1.0.0
 * @doc Funções úteis   
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/
 
package br.erlangms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
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
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.swing.text.MaskFormatter;
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
import com.google.gson.internal.LinkedTreeMap;
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
	public static Gson gson = null;
	private static Gson gson2 = null;
	public static EmsProperties properties = null;
	private static final SimpleDateFormat dateFormatDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat dateFormatDDMMYYYY_HHmm = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final SimpleDateFormat dateFormatDDMMYYYY_HHmmss = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat dateFormatYYYYMMDD_HHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final SimpleDateFormat dateFormatYYYYMMDD_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static MessageDigest messageDigestSHA1 = null;
	private static java.util.Base64.Encoder base64Encoder = null;
	private final static String HEX = "0123456789ABCDEF";
	private final static String seed = "LDAPCorp_pwdupdate";
    private static String[] args = null;

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
		properties = getProperties();
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
	 * @param <T> String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @return objeto
	 * @author Everton de Vargas Agilar
	 */
	public static <T> T fromJson(final String jsonString, final Class<T> classOfObj) {
		return (T) fromJson(jsonString, classOfObj, null);
	}

	/**
	 * Serializa um objeto a partir de uma string json.
	 * Quando a string json é vazio, apenas instância um objeto da classe.
	 * @param jsonString String json
	 * @param <T>  String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo
	 * @return objeto  
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
	 * @param <T> String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @return list
	 * @author Everton de Vargas Agilar
	 */
	public static <T> List<T> fromListJson(final String jsonString, final Class<T> classOfObj) {
		return fromListJson(jsonString, classOfObj, null);
	}
	 
	/**
	 * Obtém uma lista a partir de um json. Se o json estiver vazio, retorna apenas uma lista vazia.
	 * @param jsonString String json
	 * @param classOfObj	Classe do objeto que será serializado
	 * @param <T>	Classe do objeto que será serializado
 	 * @param jsonModelAdapter adaptador para permitir obter atributos de modelo
 	 * @return list 
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
						if (value_field instanceof ArrayList<?>) {
							paramType = ((ArrayList<?>) value_field).get(0).getClass();
						}else {
							paramType = value_field.getClass();
						}
					}
					if (paramType == Integer.class){
						if (value_field instanceof String){
							query.setParameter(p++, Integer.parseInt((String) value_field));
						}else if (value_field instanceof Double){
							query.setParameter(p++, ((Double)value_field).intValue());
						}else if( value_field instanceof ArrayList<?>){
							//Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
							List<Integer> value_field_parameter = new ArrayList<Integer>();
							if (((ArrayList) value_field).size() > 0) {
								//Tests the type of the array using the first position
								if (((ArrayList) value_field).get(0) instanceof String) {
									for (String string : (ArrayList<String>)value_field) {
										value_field_parameter.add(Integer.parseInt(string));
									}
								} else if (((ArrayList) value_field).get(0) instanceof Double) {
									for (Double doubleValue : (ArrayList<Double>)value_field) {
										value_field_parameter.add(doubleValue.intValue());
									}
								}
							}
							query.setParameter(p++, value_field_parameter);
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
						if( value_field instanceof ArrayList<?>){
							//Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
							List<Double> value_field_parameter = new ArrayList<Double>();
							if (((ArrayList) value_field).size() > 0) {
								//Tests the type of the array using the first position
								if (((ArrayList) value_field).get(0) instanceof String) {
									for (String string : (ArrayList<String>)value_field) {
										value_field_parameter.add(Double.valueOf(string));
									}
								} else if (((ArrayList) value_field).get(0) instanceof Double) {
									for (Double doubleValue : (ArrayList<Double>) value_field){
										value_field_parameter.add(doubleValue);
									}
								}
							}
							query.setParameter(p++, value_field_parameter);
						}else {
							double valueDouble = parseAsDouble(value_field);
							query.setParameter(p++, Double.valueOf(valueDouble));
						}
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
	 * Passando um objeto e retorna um map. Se o obj já é um map não faz nada e apenas o retorna.
	 * @param obj Instância de um objeto
	 * @return map de objetos
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
	
		
	@SuppressWarnings("rawtypes")
	public static byte[] toByteArray(List new_value) {
	    final int n = new_value.size(); 
	    byte ret[] = new byte[n];
	    for (int i = 0; i < n; i++) {
	      double valor = (double)new_value.get(i);
	      ret[i] = (byte) valor;
	    }
	    return ret;
	}	
	
	/**
	 * Seta os valores no objeto a partir de um map.
	 * @param obj Instância de um objeto
	 * @param values	Map com chave/valor dos dados que serão aplicados no objeto
	 * @param jsonModelAdapter jsonModelAdapter
	 * @return Object objeto
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
								try {
									if (len_value == 0){
										field.set(obj, null);
									}
									else if (len_value >= 6 && len_value <= 10){
		                        		field.set(obj, dateFormatYYYYMMDD.parse((String) new_value));
		    						}else if (len_value == 16){
		                        		field.set(obj, dateFormatYYYYMMDD_HHmm.parse((String) new_value));
		    						}else if (len_value == 19){
		    							field.set(obj, dateFormatYYYYMMDD_HHmmss.parse((String) new_value));
		    						}else{
		    							throw new EmsValidationException(m_erro);
		    						}
								} catch (ParseException em) {
									throw new EmsValidationException(m_erro);
								}
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
								try {
									if (len_value == 0){
										field.set(obj, null);
									}
									else if (len_value >= 6 && len_value <= 10){
		                        		field.set(obj, new java.sql.Date(dateFormatYYYYMMDD.parse((String) new_value).getTime()));
		    						}else if (len_value == 16){
		                        		field.set(obj, new java.sql.Date(dateFormatYYYYMMDD_HHmm.parse((String) new_value).getTime()));
		    						}else if (len_value == 19){
		    							field.set(obj, new java.sql.Date(dateFormatYYYYMMDD_HHmmss.parse((String) new_value).getTime()));
		    						}else{
		    							throw new EmsValidationException(m_erro);
		    						}
								}catch (ParseException em) {
									throw new EmsValidationException(m_erro);
								}
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
								try {
									if (len_value == 0){
										field.set(obj, null);
									}
									else if (len_value >= 6 && len_value <= 10){
		                        		field.set(obj, new java.sql.Date(dateFormatYYYYMMDD.parse((String) new_value).getTime()));
		    						}else if (len_value == 16){
		                        		field.set(obj, new java.sql.Date(dateFormatYYYYMMDD_HHmm.parse((String) new_value).getTime()));
		    						}else if (len_value == 19){
		    							field.set(obj, new java.sql.Date(dateFormatYYYYMMDD_HHmmss.parse((String) new_value).getTime()));
		    						}else{
		    							throw new EmsValidationException(m_erro);
		    						}
								}catch (ParseException em) {
									throw new EmsValidationException(m_erro);
								}
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
					}else if (tipo_field == byte[].class){
						byte[] value = null;
						
						if(new_value instanceof ArrayList) {
							value = toByteArray((ArrayList)new_value);
						} else {
							value = toByteArray(new ArrayList(((LinkedTreeMap<Integer, Double>)new_value).values()));
						}
						
						field.set(obj, value);
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
	 * @param obj	objeto
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
	 * @return Rest client
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
	 * @param request requisição
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
			case "in": return " IN ";
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
	 * @param params parâmetros do relatório.  
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
	 * @param classOfModel classe do modelo
	 * @return array of UniqueConstraint
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
	 * @param classOfModel classe do modelo  
	 * @return lista de campos 
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
	 * @param classOfModel classe do modelo
	 * @return lista de campos 
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
	 * Realiza a conversão de um lista para map
	 * 
	 * Para que seja posśivel a conversão é necessário passar a lista dos campos (fields).
	 * 
	 * @param fields lista de campos. Pode ser passado como um array de campos, string de campos separado por vírgula ou lista de campos.
	 * @param listObj lista de objetos
	 * @return list ou exception EmsValidationException
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
    	public String nodeName;
    	public String nodeUser;
    	public String nodePasswd;
        public String authorizationHeaderName;
        public String authorizationHeaderValue;
        public Map<String, Object> daemon_params;
        public String daemon_params_encode;
        public boolean debug;
        public int msg_timeout = 60000;
        public String environment = "desenv";
        public boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
        public boolean isLinux = System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0;
        public boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
        public int pidfileWatchdogTimer = 30000;
		public String pidfile;
		public String logfile;
		public String daemon_service;
		public String daemon_id;

        // smtp
        public int smtpPort;			  		// Ex: 25
		public String smtp;				  		// Ex: smtp.unb.br
		public String smtpFrom;			  		// Ex: evertonagilar@unb.br
		public String smtpPasswd;		  

		// ldap
    	public String ldapUrl;					// Ex: ldap://localhost:2389
		public String ldapAdmin;				// Ex: cn=admin,dc=unb,dc=br
		public String ldapAdminPasswd;			// Ex: 123456
		public int postUpdateTimeout;			// Ex: 30000
		public String service_scan = "br.unb";
    }
    
	/**
	 * Retorna as propriedades para o SDK do barramento de serviços ERLANGMS.
	 * 
	 * Exemplo: 
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
	@SuppressWarnings("unchecked")
	private static EmsProperties getProperties() {
		EmsProperties prop = new EmsProperties();
		
		// Atenção: ems_daemon_params deve ser o primeiro parâmetro lido das propriedades
		// pois os próximos parâmetros podem ser armazenados em ems_daemon_params ou obtidos das properties da JVM
		String tmp_daemon_params = getProperty("ems_daemon_params");
		if (tmp_daemon_params != null) {
			try{
				// O barramento vai passar sempre como base64
				// mas na IDE o desenvolvedor pode passar texto puro
				if (tmp_daemon_params.startsWith("base64:")){
					prop.daemon_params_encode = "base64";
					tmp_daemon_params = tmp_daemon_params.substring(7);
					tmp_daemon_params = decode64(tmp_daemon_params);
				}else {
					prop.daemon_params_encode = "";
				}
				prop.daemon_params = EmsUtil.fromJson(tmp_daemon_params, HashMap.class);
			}catch (Exception e) {
				System.out.println("Não foi possível fazer o parse do parâmetro ems_daemon_params. Erro interno: "+ e.getMessage());
				prop.daemon_params = new HashMap<String, Object>();
			}
		}else {
			prop.daemon_params = new HashMap<String, Object>(); 
		}
			
		String tmp_thread_pool = getProperty("ems_thread_pool");
		if (tmp_thread_pool != null){
			try{
				prop.maxThreadPool = Integer.parseInt(tmp_thread_pool);
			}catch (NumberFormatException e){
				prop.maxThreadPool = 12;
			}
		}else{
			prop.maxThreadPool = 12;
		}
		
		String tmp_cookie = getProperty("ems_cookie");
		if (tmp_cookie != null){
		   prop.cookie = tmp_cookie;
	   }else{
		   prop.cookie = "erlangms";
	   }

	   String tmp_host = getProperty("ems_host");
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

	   if (getProperty("ems_debug", "false").equalsIgnoreCase("true")){
		   prop.debug = true;
	   }else{
		   prop.debug = false;
	   }

	   prop.service_scan  = getProperty("ems_service_scan", "br.unb");

	   String tmp_nodeName = getProperty("ems_node");
	   if (tmp_nodeName != null){
		   prop.nodeName = tmp_nodeName;
	   }else{
		   prop.nodeName = "node01";
	   }
	   
	   String tmp_environment = getProperty("ems_environment");
	   if (tmp_environment != null){
		   prop.environment = tmp_environment;
	   }else{
		   prop.environment = "desenv";
	   }

	   String tmp_ESB_URL = getProperty("ems_bus_url");
	   if (tmp_ESB_URL != null){
		   if (tmp_ESB_URL.indexOf(":") == -1){
			   tmp_ESB_URL = tmp_ESB_URL + ":2301";
		   }
		   prop.ESB_URL = tmp_ESB_URL;
	   }else{
		   prop.ESB_URL = "http://localhost:2301";
	   }
	   
   
	   String tmp_user = getProperty("ems_user");
	   if (tmp_user != null){
		   prop.nodeUser = tmp_user;
	   }else{
		   prop.nodeUser = "geral";
	   }
	   
	   String tmp_password = getProperty("ems_password");
	   if (tmp_password != null){
		   prop.nodePasswd = tmp_password;
	   }else{
		   prop.nodePasswd = "123456";
	   }
	   
       String usernameAndPassword = prop.nodeUser + ":" + prop.nodePasswd;
       prop.authorizationHeaderName = "Authorization";
       prop.authorizationHeaderValue = "Basic " +  toBase64(usernameAndPassword);

       // SMTP properties
       
	   String tmp_smtp = getProperty("ems_smtp");
	   if (tmp_smtp != null){
		   prop.smtp = tmp_smtp;
	   }else{
		   prop.smtp = "smtp.unb.br";
	   }

	   String tmp_smtp_port = getProperty("ems_smtp_port");
	   if (tmp_smtp_port != null){
		   try{
			   prop.smtpPort = Integer.parseInt(tmp_smtp_port);
		   } catch(NumberFormatException e){
			   prop.smtpPort = 25;
		   }
	   }else{
		   prop.smtpPort = 25;
	   }
       
	   String tmp_smtp_from = getProperty("ems_smtp_from");
	   if (tmp_smtp_from != null){
		   prop.smtpFrom = tmp_smtp_from;
	   }else{
		   prop.smtpFrom = "erlangms@unb.br";
	   }

	   String tmp_smtp_passwd = getProperty("ems_smtp_passwd");
	   if (tmp_smtp_passwd != null){
		   prop.smtpPasswd = tmp_smtp_passwd;
	   }else{
		   prop.smtpPasswd = "123456";
	   }

	   // LDAP properties
	   
	   String tmp_ldap_url = getProperty("ems_ldap_url");
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

	   String tmp_ldap_admin = getProperty("ems_ldap_admin");
	   if (tmp_ldap_admin != null){
		   prop.ldapAdmin = tmp_ldap_admin;
	   }else{
		   prop.ldapAdmin = "cn=admin,dc=unb,dc=br";
	   }

	   String tmp_ldap_admin_passwd = getProperty("ems_ldap_admin_passwd");
	   if (tmp_ldap_admin_passwd != null){
		   prop.ldapAdminPasswd = tmp_ldap_admin_passwd;
	   }else{
		   prop.ldapAdminPasswd = "123456";
	   }

		String tmp_msg_timeout = getProperty("ems_msg_timeout");
		if (tmp_msg_timeout != null){
			try{
				prop.msg_timeout = Integer.parseInt(tmp_msg_timeout);
			}catch (NumberFormatException e){
				prop.msg_timeout = 60000;
			}
		}else{
			prop.msg_timeout = 60000;
		}

		String tmp_postUpdateTimeout = getProperty("ems_post_update_timeout");
		if (tmp_postUpdateTimeout != null){
			try{
				prop.postUpdateTimeout = Integer.parseInt(tmp_postUpdateTimeout);
				if (prop.postUpdateTimeout < 15000) {
					prop.postUpdateTimeout = prop.postUpdateTimeout  + 5000;
				}
			}catch (NumberFormatException e){
				prop.postUpdateTimeout = 30000;
			}
		}else{
			prop.postUpdateTimeout = 30000;
		}
		
		prop.pidfile = getProperty("ems_pidfile");
		prop.pidfileWatchdogTimer = getPropertyAsInt("ems_pidfile_watchdog_timer", 30000);
		prop.logfile = getProperty("ems_logfile");
		prop.daemon_service = getProperty("ems_daemon_service");
		prop.daemon_id = getProperty("ems_daemon_id");

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
	public static void sendHtmlMail(final String to, 
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
                mailMessage.setContent(multipart);
            }
            
            Transport.send(mailMessage);
        } catch (MessagingException ex) {
        	throw new EmsValidationException("Não foi possível enviar e-mail para "+ to + ". Erro interno: "+ ex.getMessage());
        }
    }


	/**
	 * Algoritmo SHA-1
	 * @param value valor
	 * @return valor em SHA1
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
	 * @param value valor
	 * @return valor em base 64
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

    
	public static String encrypt(String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}
    
	public static String decrypt(String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
	
	public static byte[] toSHA1(byte[] convertme) {
	    MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    return md.digest(convertme);
	}
	
	public static String encode64(String str){	        
		String strenc = "";
		try{
            Base64.Encoder enc= Base64.getEncoder();	            
            strenc = new String(enc.encode(str.getBytes("UTF-8")));
        }
        catch(Exception e){
            System.out.println("Exception");
        }
        
        return strenc;
	}
	public static String decode64(String str){
		
		String strdec = "";
		
		try{		
	        Base64.Decoder dec= Base64.getDecoder(); 
	        strdec = new String(dec.decode(str.getBytes("UTF-8")));
		}
        catch(Exception e){
            System.out.println("Exception");
        }		
		return strdec;
	
	}	

    public static String formatarString(final String texto, final String mascara) throws ParseException {
        MaskFormatter mf = new MaskFormatter(mascara);
        mf.setValueContainsLiteralCharacters(false);
        return mf.valueToString(texto);
    }


    private static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    public static String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }
    
    
    //
    // Executa um comando e imprime sua saída no terminal se imprimeSaida for true (apenas Linux).
    //
    //
    public static void executeCommand(String command, boolean imprimeSaida) throws IOException {
        boolean isLinuxOS = EmsUtil.isLinuxOS(); 
        final ArrayList<String> commands = new ArrayList<String>();
        if (isLinuxOS){
        	commands.add("/bin/bash");
            commands.add("-c");
        }else{
        	commands.add("cmd.exe /c ");
        	command = command.replace("java", "java.exe");
        }
        commands.add(command);
        
        BufferedReader br = null;        

        try {           
        	if (isLinuxOS){
	        	final ProcessBuilder p = new ProcessBuilder(commands);
	            final Process process = p.start();
	            if (imprimeSaida){
		            final InputStream is = process.getInputStream();
		            final InputStreamReader isr = new InputStreamReader(is);
		            br = new BufferedReader(isr);
		            String line;            
		            while((line = br.readLine()) != null) {
		                System.out.println(line);
		            }
	            }
        	}else{
            	String cmd = EmsUtil.join(commands, " ");
            	Runtime.getRuntime().exec(cmd);
        	}
            
        } catch (IOException ioe) {
            logger.info(ioe.getMessage());
            throw ioe;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        }
    }
    
    

    //
    // Remove o pid de um processo de forma segura
    //
    //
    public static void removePidFile(final String fileNamePid){
    	try{
	    	File arquivo = new File(fileNamePid);
		    if (arquivo.exists()){
		    	arquivo.delete();
		    }
    	}catch (Exception e){
    		// não retorna erro ao processo chamador em caso de falha aqui
    	}
    }
    

	//
	// Obtém o pid do processo atual 
	//
	public static String getMeuPid() {
		return java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}



	//
	// Captura o sinal enviado por kill para finalizar o processo e faça o encerramento corretamente.
	// Agilar: Para ctrl + c ou kill [processo]. O poderoso kill -9 não é capturado, portanto seja legal e não use o parâmetro -9.
	//
	public static void addShutdownHook(String fileNamePid) {
        Runtime.getRuntime().addShutdownHook(new Thread(fileNamePid) {
            @Override
            public void run() {
            	EmsUtil.removePidFile(this.getName());
            	logger.info(String.format("%s finalizado.", this.getName()));
            }
        });		
	}
	
	
	//
	// Lê o pid de um processo armazenado em um arquivo pid.
	//
	public static Integer lePid(final String fileNamePidProcesso) throws NumberFormatException, IOException{
		BufferedReader reader = null;
		try {
        	reader = new BufferedReader(new FileReader(fileNamePidProcesso));        
            Integer pid = Integer.parseInt(reader.readLine());    
            return pid;        
        }finally{
        	if (reader != null){
        		reader.close();
        	}
        }
	}


	public static String getMeuIp(){
		Enumeration<NetworkInterface> nis = null;  
        try {  
            nis = NetworkInterface.getNetworkInterfaces();  
        } catch (SocketException e) {  
            logger.info(String.format("Erro ao obter IP. Motivo: %s", e.getMessage()));  
        }  
        if (nis  != null){
	        while (nis.hasMoreElements()) {  
	            NetworkInterface ni = (NetworkInterface) nis.nextElement();  
	            Enumeration<InetAddress> ias = ni.getInetAddresses();  
	            while (ias.hasMoreElements()) {  
	                InetAddress ia = (InetAddress) ias.nextElement();  
	                if (ia.getHostAddress().contains("164.41")) {   
	                	return ia.getHostAddress();      
	                }  
	            }  
	        }
        }
        return "";
	}

	//
	// Mata um processo pelo seu pid
	//
	public static void kill(Integer pid, boolean force) {  
        try {  
            if (EmsUtil.isLinuxOS()){
            	String cmd;
            	if (force){
            		cmd = String.format("kill -9 %d", pid);
            	}else{
            		cmd = String.format("kill %d", pid);
            	}
            	EmsUtil.executeCommand(cmd, false);
            }else{
	        	String line;  
	            String pid_str = pid.toString();
	            Process p = Runtime.getRuntime().exec("tasklist.exe /fo csv /nh");  
	            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));  
	            while ((line = input.readLine()) != null) {  
	            	if (!line.trim().equals("")) {  
	                	String[] args = line.split("\",\"");
	            		if (args[1].equals(pid_str)) {  
	            			Runtime.getRuntime().exec(String.format("taskkill /PID %d", pid));  
	                        return;  
	                    }  
	                }  
	            }  
	            input.close();  
            }      
        } catch (Exception err) {  
            logger.info(String.format("Erro ao matar processo %d", pid));  
        }  
    }  	

	// 
	// Retorna true/false se está rodando no Linux
	//
	public static boolean isLinuxOS(){
        boolean isLinuxOS = System.getProperty("os.name").toLowerCase().contains("linux"); 
		return isLinuxOS;
	}
	
	
	public static String join(final String[] list, final String conjunction){
		   StringBuilder sb = new StringBuilder();
		   boolean first = true;
		   for (String item : list)
		   {
		      if (first)
		         first = false;
		      else
		         sb.append(conjunction);
		      sb.append(item);
		   }
		   return sb.toString();
	}

	 public  static String join(final ArrayList<String> list, final String conjunction){
		   StringBuilder sb = new StringBuilder();
		   boolean first = true;
		   for (String item : list)
		   {
		      if (first)
		         first = false;
		      else
		         sb.append(conjunction);
		      sb.append(item);
		   }
		   return sb.toString();
	}

	 
	/**
	 * Obtém um parâmetro de configuração. Se ocorrer erro retorna null
	 * @param p propriedade
	 * @return valor do parâmetro ou null
	 * @author Everton de Vargas Agilar, Renato Carauta
	 */
	public static String getProperty(final String p) {
		if (p != null && !p.isEmpty()) {
			
			// Obs.: na inicialização do sdk, properties pode não estar disponível ainda
			if (properties != null) {
				Map<String, Object> c = properties.daemon_params;
				
				// Parâmetro erlangms.thread_pool
				if ((p.equals("erlangms.java_thread_pool") || p.equals("ems_thread_pool")) && c.containsKey("erlangms.java_thread_pool")){
					return c.get("erlangms.java_thread_pool").toString();
				}
	
				// Parâmetro erlangms.host
				if ((p.equals("erlangms.host") || p.equals("ems_host")) && c.containsKey("erlangms.host")){
					return (String) c.get("erlangms.host");
				}
	
				// Parâmetro erlangms.url
				if ((p.equals("erlangms.url") || p.equals("ems_bus_url")) && c.containsKey("erlangms.url")){
					return (String) c.get("erlangms.url");
				}
	
				// Parâmetro erlangms.user
				if ((p.equals("erlangms.user") || p.equals("ems_user")) && c.containsKey("erlangms.user")){
					return (String) c.get("erlangms.user");
				}
	
				// Parâmetro erlangms.password
				if ((p.equals("erlangms.password") || p.equals("ems_password")) && c.containsKey("erlangms.password")){
					return (String) c.get("erlangms.password");
				}
	
				// Parâmetro erlangms.smtp.password
				if ((p.equals("erlangms.smtp.password") || p.equals("ems_smtp_passwd")) && c.containsKey("erlangms.smtp.password")){
					return (String) c.get("erlangms.smtp.password");
				}
	
				// Parâmetro erlangms.smtp.from
				if ((p.equals("erlangms.smtp.from") || p.equals("ems_smtp_from")) && c.containsKey("erlangms.smtp.from")){
					return (String) c.get("erlangms.smtp.from");
				}
	
				// Parâmetro erlangms.smtp.port
				if ((p.equals("erlangms.smtp.port") || p.equals("ems_smtp_port")) && c.containsKey("erlangms.smtp.port")){
					return (String) c.get("erlangms.smtp.port").toString();
				}
	
				// Parâmetro erlangms.smtp.port
				if ((p.equals("erlangms.smtp.mail") || p.equals("ems_smtp")) && c.containsKey("erlangms.smtp.mail")){
					return (String) c.get("erlangms.smtp.mail");
				}
	
				// Parâmetro erlangms.environment
				if ((p.equals("erlangms.environment") || p.equals("ems_environment")) && c.containsKey("erlangms.environment")){
					return (String) c.get("erlangms.environment");
				}
	
				// Parâmetro erlangms.node
				if ((p.equals("erlangms.node") || p.equals("ems_node")) && c.containsKey("erlangms.node")){
					return (String) c.get("erlangms.node");
				}
	
				// Parâmetro erlangms.cookie
				if ((p.equals("erlangms.cookie") || p.equals("ems_cookie")) && c.containsKey("erlangms.cookie")){
					return (String) c.get("erlangms.cookie");
				}
				
				// Parâmetro erlangms.max_thread_pool_by_agent
				if ((p.equals("erlangms.max_thread_pool_by_agent") || p.equals("ems_max_thread_pool_by_agent")) && c.containsKey("erlangms.max_thread_pool_by_agent")){
					return (String) c.get("erlangms.max_thread_pool_by_agent");
				}
	
				// Parâmetro erlangms.debug
				if ((p.equals("erlangms.debug") || p.equals("ems_debug")) && c.containsKey("erlangms.debug")){
					return (String) c.get("erlangms.debug").toString();
				}
	
				// Parâmetro erlangms.ldap.passwd
				if ((p.equals("erlangms.ldap.passwd") || p.equals("ems_ldap_admin_passwd")) && c.containsKey("erlangms.ldap.passwd")){
					return (String) c.get("erlangms.ldap.passwd");
				}
	
				// Parâmetro erlangms.ldap.admin
				if ((p.equals("erlangms.ldap.admin") || p.equals("ems_ldap_admin")) && c.containsKey("erlangms.ldap.admin")){
					return (String) c.get("erlangms.ldap.admin");
				}
				
				// Parâmetro erlangms.ldap.url
				if ((p.equals("erlangms.ldap.url") || p.equals("ems_ldap_url")) && c.containsKey("erlangms.ldap.url")){
					return (String) c.get("erlangms.ldap.url");
				}
				
				// Parâmetro erlangms.msg_timeout
				if ((p.equals("erlangms.msg_timeout") || p.equals("ems_msg_timeout")) && c.containsKey("erlangms.msg_timeout")){
					return (String) c.get("erlangms.msg_timeout").toString();
				}
	
				// Parâmetro erlangms.post_update_timeout
				if ((p.equals("erlangms.post_update_timeout") || p.equals("ems_post_update_timeout")) && c.containsKey("erlangms.post_update_timeout")){
					return (String) c.get("erlangms.post_update_timeout").toString();
				}
	
				// Parâmetro erlangms.daemon_id
				if ((p.equals("erlangms.daemon_id") || p.equals("ems_daemon_id")) && c.containsKey("erlangms.daemon_id")){
					return (String) c.get("erlangms.daemon_id");
				}

				// Parâmetro erlangms.daemon_service
				if ((p.equals("erlangms.daemon_service") || p.equals("ems_daemon_service")) && c.containsKey("erlangms.daemon_service")){
					return (String) c.get("erlangms.daemon_service");
				}

				// Parâmetro erlangms.logfile
				if ((p.equals("erlangms.logfile") || p.equals("ems_logfile")) && c.containsKey("erlangms.logfile")){
					return (String) c.get("erlangms.logfile");
				}

				// Parâmetro erlangms.pidfile
				if ((p.equals("erlangms.pidfile") || p.equals("ems_pidfile")) && c.containsKey("erlangms.pidfile")){
					return (String) c.get("erlangms.pidfile");
				}

				// Parâmetro erlangms.pidfile_watchdog_timer
				if ((p.equals("erlangms.pidfile_watchdog_timer") || p.equals("ems_pidfile_watchdog_timer")) && c.containsKey("erlangms.pidfile_watchdog_timer")){
					return (String) c.get("erlangms.pidfile_watchdog_timer").toString();
				}
			
				// Parâmetro pode ter sido enviado em daemon_params 
				if (c.containsKey(p)){
					String result = c.get(p).toString();
					return (result == null || result.isEmpty()) ? "" : result.trim();
				}
				
				// Parâmetro erlangms.service_scan
				if ((p.equals("erlangms.service_scan") || p.equals("ems_service_scan")) && c.containsKey("erlangms.service_scan")){
					return (String) c.get("erlangms.service_scan");
				}

			}

			// Os parâmetros podem vir de args do método main também
			// para isso, a aplicação precisa invocar EmsUtil.setArgs()
			if (args != null) {
				try {
					for (String prop : args) {
						int posEq = prop.indexOf("=");
						if (posEq != -1) {
							String key = prop.substring(1, posEq);
							if (key.startsWith("-D")) key = key.substring(2); // -D
							if (key.startsWith("-")) key = key.substring(1);  // - 
							if (key.startsWith("-")) key = key.substring(1);  // --
							if(key.equalsIgnoreCase(p) || key.equalsIgnoreCase("D" + p)) {
								return prop.substring(posEq+1).trim();
							}
						}else {
							if (prop.equalsIgnoreCase(p)) {
								return prop.trim();
							}
						}
					}
				}catch (Exception e) {
					return null;
				}
			}
			String result = System.getProperty(p);
			// String vazia também é null
			if (result != null) {
				if (result.trim().isEmpty()) {
					return null;
				}else {
					return result;
				}
			}else {
				return null;
			}
		}else {
			return null;
		}
	}

	/**
	 * Obtém um parâmetro de configuração ou defaultValue se não encontrar
	 * @param property propriedade
	 * @param defaultValue valor default
	 * @return valor do parâmetro ou defaultValue se não encontrar
	 * @author Everton de Vargas Agilar
	 */
	public static String getProperty(final String property, final String defaultValue) {
		String result = getProperty(property);
		return (result == null || result.isEmpty()) ? defaultValue : result;
	}
	
	
	/**
	 * Obtém um parâmetro de configuração como inteiro. Se não encontrar retorna null
	 * @param property propriedade
	 * @return valor do parâmetro ou null
	 * @author Everton de Vargas Agilar
	 */
	public static Integer getPropertyAsInt(final String property) {
		try {
			return Integer.parseInt(getProperty(property));
		} catch (Exception e) {  
            return null;  
        }  			
	}
	
	/**
	 * Obtém um parâmetro de configuração como inteiro ou defaultValue se não encontrar.
	 * @param property propriedade
	 * @param defaultValue valor default
	 * @return valor do parâmetro ou defaultValue
	 * @author Everton de Vargas Agilar
	 */
	public static Integer getPropertyAsInt(final String property, Integer defaultValue) {
		try {
			return Integer.parseInt(getProperty(property));
		} catch (Exception e) {  
            return defaultValue;  
        }  			
	}
	
	
	/**
	 * Permite passar args que recebido em main para o SDK
	 * É utilizado para buscar parâmetros com getProperty
	 * @param args argumentos
	 * @author Everton de Vargas Agilar
	 */
	public static void setArgs(final String[] args) {
		EmsUtil.args = args;
		properties = getProperties(); 
	}
	 

	/**
	 * Cria um arquivo de pid para o processo.
	 * @param fileNamePid nome do arquivo de pid
	 * @param deleteIfExists apaga o arquivo se existe
	 * @throws Exception retorna erro se não consegue criar o pid  
	 * @return nome do arquivo criado ou exception
	 * @author Everton de Vargas Agilar
	 */
	 public static String createPidFile(final String fileNamePid, boolean deleteIfExists) throws Exception {
		 if (fileNamePid != null && !fileNamePid.isEmpty()) {
			 File arq = new File(fileNamePid);
			 if (arq.exists()){
				 System.out.println(String.format("Atenção: O arquivo de pid %s já existia.", fileNamePid));
				 if (deleteIfExists) {
					 arq.delete();
				 }
			 }
			 
			 try(FileWriter fw = new FileWriter(arq)) {
				 fw.write(getMeuPid());
	        	 fw.flush();
	        	 return fileNamePid;
			 }catch(IOException e){
				 throw new Exception("Não foi possível criar o arquivo de pid "+ fileNamePid + ". Motivo: "+ e.getMessage());
			 }
		 }else {
			 throw new Exception("Parâmetro fileNamePid é inválido para EmsUtil.createPidFile.");
		 }
	 }

	 
	/**
	 * Adiciona um hook na JVM para monitorar e manter o arquivo de pid do processo
	 * baseado no algorítmo criado para o SisRuCatracas
	 * @param fileNamePid nome do arquivo de pid
	 * @param serviceContext contexto JPA
	 * @param watchdogTimer tempo em milisegundos
	 * @author Everton de Vargas Agilar, Renato Carauta
	 */
	public static void addUpdatePidFileHook(final String fileNamePid, final EntityManager serviceContext, final Integer watchdogTimer) {
		class UpdatePidFileThead extends Thread {
			private String fileNamePid = null;
			private Integer watchdogTimer = null;
						
			public UpdatePidFileThead(final String fileNamePid, EntityManager serviceContext, Integer watchdogTimer){
				this.fileNamePid = fileNamePid;
				this.watchdogTimer = watchdogTimer;  
				setPriority(NORM_PRIORITY);
			}

			@Override
			public void run() {
				String pidStr = getMeuPid();
				//Query query = serviceContext.createNativeQuery("SELECT 1"); 
				while (isAlive()){
					try {
						Thread.sleep(watchdogTimer);
					} catch (InterruptedException e2) {
						// acordou
					}
					if (isAlive()){
						try{
							try { 
								// Pode travar e não voltar -^-
								// Se ficar muito tempo travado o barramento vai matar o processo e subir uma nova instãncia
								//query.getSingleResult(); 
							} catch (Exception e) {
								throw new Exception("Falha de conexão ao banco de dados identificada.");
							};  
							try (FileWriter arq = new FileWriter(this.fileNamePid)){
						        arq.write(pidStr);
						        arq.flush();
					        } catch (Exception e) {
					        	throw new Exception("Erro na gravação do arquivo de pid.");
					        }
					    }catch(Exception e){
					    	logger.info("Atenção: Não foi possível atualizar o arquivo de pid "+ fileNamePid + ". Motivo: "+ e.getMessage());
					    }
					}
		        }
			}
		}
		logger.info("Adicionando um hook para atualizar o arquivo de pid "+ fileNamePid + " a cada " + String.valueOf(watchdogTimer) + "ms."); 
		new UpdatePidFileThead(fileNamePid, serviceContext, watchdogTimer).start();
	}
	
	
	/**
	 * Adiciona um hook na JVM para monitorar o arquivo de pid do processo
	 * Se ficar desatualizado por muito tempo, o processo será encerrado normalmente
	 * @param fileNamePid nome do arquivo do pid
	 * @param watchdogTimer tempo em milisegundos
	 * @author Everton de Vargas Agilar, Renato Carauta
	 */
	public static void addMonitorPidFileHook(final String fileNamePid, Integer watchdogTimer) {
		class UpdatePidFileThead extends Thread {
			private String fileNamePid = null;
			private Integer watchdogTimer = 60000;

			public UpdatePidFileThead(final String fileNamePid, Integer watchdogTimer){
				this.fileNamePid = fileNamePid;
				this.watchdogTimer = watchdogTimer;
				setPriority(MIN_PRIORITY);
			}

			@Override
			public void run() {
				int numeroTentativas = 1;
				while (isAlive()){
					try {
						Thread.sleep(watchdogTimer);
					} catch (InterruptedException e2) {
						// acordou
					}
					if (isAlive()){
						try{
					    		File arq = new File(fileNamePid);
					    		if (!arq.exists()) {
					    			throw new Exception("O arquivo de pid "+ fileNamePid + " não foi encontrado.");
					    		}
					    		long tempoDecorrido = System.currentTimeMillis() - arq.lastModified();
					    		long diffTempo = tempoDecorrido - watchdogTimer;
					    		if(diffTempo > watchdogTimer) {
					    			throw new Exception("O arquivo de pid "+ fileNamePid + " está desatualizado há mais de "+ String.valueOf(diffTempo) + "ms.");
					    		}
					    		numeroTentativas = 1; // O contador é zerado quando o arquivo é verificado com sucesso
					    }catch(Exception e){
					    	// O processo será encerrado somente na terceira tentativa
					    	// antes disso, somente um alerta no log será feito
					    	if (numeroTentativas > 3 ) {
					    		logger.info("Fatal: O processo será encerrado pois está travado. Erro interno: " + e.getMessage());
					    		removePidFile(fileNamePid); 
					    		System.exit(1);
					    	}else {
					    		logger.info("Atenção: O processo parece estar travado (Tentativa: "+ String.valueOf(numeroTentativas) + "). Erro interno: " + e.getMessage());
					    	}
					    	numeroTentativas++;
					    }
					}
		        }
			}
		}
		logger.info("Adicionando um hook para monitorar o arquivo de pid "+ fileNamePid + " a cada " + String.valueOf(watchdogTimer) + "ms.");
		new UpdatePidFileThead(fileNamePid, watchdogTimer).start();
	}
	
}
	 
	 