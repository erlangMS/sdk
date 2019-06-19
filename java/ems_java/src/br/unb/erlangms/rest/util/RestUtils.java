package br.unb.erlangms.rest.util;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.schema.RestField;
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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
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
import org.apache.commons.lang3.time.FastDateFormat;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.olap4j.impl.ArrayMap;

/**
 * <p>
 * Funções úteis para trabalhar com RESTful</p>
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 */
public final class RestUtils {

    public static final Logger logger = Logger.getLogger("RestUtils");
    public static final String dateFormatDDMMYYYY = "dd/MM/yyyy";
    public static final String dateFormatDDMMYYYY_HHmm = "dd/MM/yyyy HH:mm";
    public static final String dateFormatDDMMYYYY_HHmmss = "dd/MM/yyyy HH:mm:ss";
    public static final String dateFormatYYYYMMDD = "yyyy-MM-dd";
    public static final String dateFormatYYYYMMDD_HHmm = "yyyy-MM-dd HH:mm";
    public static final String dateFormatYYYYMMDD_HHmmss = "yyyy-MM-dd HH:mm:ss";

    private static Gson gson = null;
    private static Gson gson2 = null;

    static {
        gson = new GsonBuilder()
                .setExclusionStrategies(new SerializeStrategy())
                .setDateFormat(dateFormatDDMMYYYY)
                //.serializeNulls() <-- uncomment to serialize NULL fields as well
                .registerTypeAdapter(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                    @Override
                    public JsonElement serialize(BigDecimal value, Type arg1, com.google.gson.JsonSerializationContext arg2) {
                        String result;
                        result = RestUtils.getDoubleFormatter().format(value);
                        return new JsonPrimitive(result);
                    }
                })
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue()) {
                            return new JsonPrimitive(src.longValue());
                        }
                        return new JsonPrimitive(src);
                    }
                })
                .registerTypeAdapter(String.class, new JsonSerializer<String>() {
                    @Override
                    public JsonElement serialize(String value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value.trim());
                    }
                })
                .registerTypeAdapter(Integer.class, new JsonSerializer<Integer>() {
                    @Override
                    public JsonElement serialize(Integer value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value);
                    }
                })
                .registerTypeAdapter(Float.class, new JsonSerializer<Float>() {
                    @Override
                    public JsonElement serialize(Float value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value);
                    }
                })
                .registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public JsonElement serialize(java.util.Date value, Type typeOfSrc, JsonSerializationContext context) {
                        if (value.getHours() == 0 && value.getMinutes() == 0) {
                            return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY).format(value));
                        } else {
                            if (value.getSeconds() == 0) {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).format(value));
                            } else {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).format(value));
                            }
                        }
                    }
                })
                .registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
                        if (value.getHours() == 0 && value.getMinutes() == 0) {
                            return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY).format(value));
                        } else {
                            if (value.getSeconds() == 0) {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).format(value));
                            } else {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).format(value));
                            }
                        }
                    }
                })
                .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                    @Override
                    public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        final String m_erro = "não é uma data válida.";
                        try {
                            int len_value = value.length();
                            if (len_value >= 6 && len_value <= 10) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject(value);
                            } else if (len_value == 16) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject(value);
                            } else if (len_value == 19) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject(value);
                            } else {
                                throw new RestApiException(m_erro);
                            }
                        } catch (ParseException e) {
                            throw new RestApiException(m_erro);
                        }
                    }
                })
                .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                    @Override
                    public java.sql.Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        final String m_erro = "não é uma data válida";
                        try {
                            int len_value = value.length();
                            if (len_value >= 6 && len_value <= 10) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject(value)).getTime());
                            } else if (len_value == 16) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject(value)).getTime());
                            } else if (len_value == 19) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject(value)).getTime());
                            } else {
                                throw new RestApiException(m_erro);
                            }
                        } catch (final ParseException e) {
                            throw new RestApiException(m_erro);
                        }
                    }
                })
                .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                .registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
                    @Override
                    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        if (value.equalsIgnoreCase("true")) {
                            return true;
                        } else if (value.equalsIgnoreCase("false")) {
                            return false;
                        } else if (value.equalsIgnoreCase("1")) {
                            return true;
                        } else if (value.equalsIgnoreCase("0")) {
                            return false;
                        } else if (value.equalsIgnoreCase("sim")) {
                            return true;
                        } else if (value.equalsIgnoreCase("1.0")) {
                            return true;
                        } else if (value.equalsIgnoreCase("yes")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create();

        gson2 = new GsonBuilder()
                .setExclusionStrategies(new SerializeStrategy())
                .setDateFormat(dateFormatDDMMYYYY)
                //.serializeNulls() <-- uncomment to serialize NULL fields as well
                .registerTypeAdapter(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                    @Override
                    public JsonElement serialize(BigDecimal value, Type arg1, com.google.gson.JsonSerializationContext arg2) {
                        String result;
                        result = RestUtils.getDoubleFormatter().format(value);
                        return new JsonPrimitive(result);
                    }
                })
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue()) {
                            return new JsonPrimitive(src.longValue());
                        }
                        return new JsonPrimitive(src);
                    }
                })
                .registerTypeAdapter(String.class, new JsonSerializer<String>() {
                    @Override
                    public JsonElement serialize(String value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value.trim());
                    }
                })
                .registerTypeAdapter(Integer.class, new JsonSerializer<Integer>() {
                    @Override
                    public JsonElement serialize(Integer value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value);
                    }
                })
                .registerTypeAdapter(Float.class, new JsonSerializer<Float>() {
                    @Override
                    public JsonElement serialize(Float value, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(value);
                    }
                })
                .registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public JsonElement serialize(java.util.Date value, Type typeOfSrc, JsonSerializationContext context) {
                        if (value.getHours() == 0 && value.getMinutes() == 0) {
                            return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY).format(value));
                        } else {
                            if (value.getSeconds() == 0) {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).format(value));
                            } else {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).format(value));
                            }
                        }
                    }
                })
                .registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public JsonElement serialize(java.sql.Timestamp value, Type typeOfSrc, JsonSerializationContext context) {
                        if (value.getHours() == 0 && value.getMinutes() == 0) {
                            return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY).format(value));
                        } else {
                            if (value.getSeconds() == 0) {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).format(value));
                            } else {
                                return new JsonPrimitive(FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).format(value));
                            }
                        }
                    }
                })
                .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                    @Override
                    public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        final String m_erro = "não é uma data válida.";
                        try {
                            int len_value = value.length();
                            if (len_value >= 6 && len_value <= 10) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject(value);
                            } else if (len_value == 16) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject(value);
                            } else if (len_value == 19) {
                                return (Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject(value);
                            } else {
                                throw new RestApiException(m_erro);
                            }
                        } catch (final ParseException e) {
                            throw new RestApiException(m_erro);
                        }
                    }
                })
                .registerTypeAdapter(java.sql.Timestamp.class, new JsonDeserializer<java.sql.Timestamp>() {
                    @Override
                    public java.sql.Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        final String m_erro = "não é uma data válida.";
                        try {
                            int len_value = value.length();
                            if (len_value >= 6 && len_value <= 10) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject(value)).getTime());
                            } else if (len_value == 16) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject(value)).getTime());
                            } else if (len_value == 19) {
                                return new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject(value)).getTime());
                            } else {
                                throw new RestApiException(m_erro);
                            }
                        } catch (final ParseException e) {
                            throw new RestApiException(m_erro);
                        }
                    }
                })
                .registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
                    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        String value = json.getAsString();
                        if (value.equalsIgnoreCase("true")) {
                            return true;
                        } else if (value.equalsIgnoreCase("false")) {
                            return false;
                        } else if (value.equalsIgnoreCase("1")) {
                            return true;
                        } else if (value.equalsIgnoreCase("0")) {
                            return false;
                        } else if (value.equalsIgnoreCase("sim")) {
                            return true;
                        } else if (value.equalsIgnoreCase("1.0")) {
                            return true;
                        } else if (value.equalsIgnoreCase("yes")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create();

    }

    public static NumberFormat getDoubleFormatter() {
        NumberFormat doubleFormatter = NumberFormat.getInstance(Locale.US);
        doubleFormatter.setMaximumFractionDigits(2);
        doubleFormatter.setMinimumFractionDigits(2);
        return doubleFormatter;
    }

    public static boolean isAnyParameterAnnotated(Method method, Class<?> annotationType) {
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (Annotation[] annotations : paramAnnotations) {
            for (Annotation an : annotations) {
                if (an.annotationType().equals(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getClassAnnotationValue(@SuppressWarnings("rawtypes") Class classType,
                                                 @SuppressWarnings("rawtypes") Class annotationType, String attributeName) {
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

        @Override
        public boolean shouldSkipClass(Class<?> c) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            OneToOne oneToOne = f.getAnnotation(OneToOne.class);
            if (oneToOne != null && oneToOne.fetch() == FetchType.EAGER) {
                return false;
            }

            return (f.getDeclaredType() == List.class
                    || f.getAnnotation(OneToMany.class) != null
                    || f.getAnnotation(JoinTable.class) != null
                    || f.getAnnotation(ManyToMany.class) != null);
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
            throw new RestApiException("Not supported");
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void write(JsonWriter out, HibernateProxy value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            try {
                // Retrieve the original (not proxy) class
                Class<?> baseType = Hibernate.getClass(value);
                // Get the TypeAdapter of the original class, to delegate the serialization
                TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
                // Get a filled instance of the original class
                Object unproxiedValue = ((HibernateProxy) value).getHibernateLazyInitializer()
                        .getImplementation();
                // Serialize the value
                delegate.write(out, unproxiedValue);
            } catch (final Exception e) {
                out.nullValue();
            }
        }
    }

    public interface JsonModelAdapter {
        public Object findById(Class<?> classOfModel, Integer id);
    }

    /**
     * Seta os valores no objeto a partir de um map.
     *
     * @param obj    Instância de um objeto
     * @param values Map com chave/valor dos dados que seráo aplicados no objeto
     * @return Object objeto mapeado
     * @throws java.lang.Exception
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object setValuesFromMap(final Object obj, final Map<String, Object> values) throws Exception {
        return setValuesFromMap(obj, values, null);
    }

    /**
     * Seta os valores no objeto a partir de um map.
     *
     * @param obj              Instância de um objeto
     * @param values           Map com chave/valor dos dados que seráo aplicados no objeto
     * @param jsonModelAdapter permite mapear atributos objetos
     * @return Object objeto mapeado
     * @throws java.lang.Exception
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object setValuesFromMap(final Object obj, final Map<String, Object> values, final JsonModelAdapter jsonModelAdapter) throws Exception {
        if (obj != null && values != null && values.size() > 0) {
            Class<? extends Object> class_obj = obj.getClass();
            for (String field_name : values.keySet()) {
                try {
                    Field field = null;
                    try {
                        field = class_obj.getDeclaredField(field_name);
                    } catch (NoSuchFieldException e) {
                        // Ignora o campo quando ele Não existe
                        continue;
                    }
                    field.setAccessible(true);
                    Object new_value = values.get(field_name);
                    Class<?> tipo_field = field.getType();
                    if (tipo_field == Integer.class || tipo_field == int.class) {
                        if (new_value instanceof String) {
                            field.set(obj, Integer.parseInt((String) new_value));
                        } else if (new_value instanceof Double) {
                            field.set(obj, ((Double) new_value).intValue());
                        } else {
                            field.set(obj, (int) new_value);
                        }
                    } else if (tipo_field == Double.class || tipo_field == double.class) {
                        if (new_value instanceof String) {
                            field.set(obj, Double.parseDouble((String) new_value));
                        } else if (new_value instanceof Double) {
                            field.set(obj, ((Double) new_value).doubleValue());
                        } else {
                            field.set(obj, ((Float) new_value));
                        }
                    } else if (tipo_field == Float.class || tipo_field == float.class) {
                        if (new_value instanceof String) {
                            field.set(obj, Float.parseFloat((String) new_value));
                        } else if (new_value instanceof Double) {
                            field.set(obj, ((Double) new_value).floatValue());
                        } else {
                            field.set(obj, ((Float) new_value));
                        }
                    } else if (tipo_field == Long.class || tipo_field == long.class) {
                        if (new_value instanceof String) {
                            field.set(obj, Double.parseDouble((String) new_value));
                        } else {
                            field.set(obj, ((Double) new_value).longValue());
                        }
                    } else if (tipo_field == BigDecimal.class) {
                        if (new_value instanceof String) {
                            field.set(obj, BigDecimal.valueOf(Double.parseDouble((String) new_value)));
                        } else {
                            field.set(obj, BigDecimal.valueOf((double) new_value));
                        }
                    } else if (tipo_field == String.class) {
                        if (new_value instanceof String) {
                            field.set(obj, new_value);
                        } else if (new_value instanceof Double) {
                            // Parece um inteiro? (termina com .0)
                            if (new_value.toString().endsWith(".0")) {
                                field.set(obj, Integer.toString(((Double) new_value).intValue()));
                            } else {
                                field.set(obj, new_value.toString());
                            }
                        } else {
                            field.set(obj, new_value.toString());
                        }
                    } else if (tipo_field == Boolean.class || tipo_field == boolean.class) {
                        if (new_value instanceof String) {
                            if (((String) new_value).equalsIgnoreCase("true")) {
                                field.set(obj, true);
                            } else if (((String) new_value).equalsIgnoreCase("false")) {
                                field.set(obj, false);
                            } else if (((String) new_value).equalsIgnoreCase("1")) {
                                field.set(obj, true);
                            } else if (((String) new_value).equalsIgnoreCase("0")) {
                                field.set(obj, false);
                            } else if (((String) new_value).equalsIgnoreCase("sim")) {
                                field.set(obj, true);
                            } else if (((String) new_value).equalsIgnoreCase("1.0")) {
                                field.set(obj, true);
                            } else if (((String) new_value).equalsIgnoreCase("yes")) {
                                field.set(obj, true);
                            } else {
                                field.set(obj, false);
                            }
                        } else if (new_value instanceof Double) {
                            if (new_value.toString().equals("1.0")) {
                                field.set(obj, true);
                            } else {
                                field.set(obj, false);
                            }
                        } else if (new_value instanceof Boolean) {
                            field.set(obj, (boolean) new_value);
                        } else {
                            field.set(obj, false);
                        }
                    } else if (tipo_field == java.util.Date.class) {
                        final String m_erro = field_name + " não é uma data válida.";
                        if (new_value instanceof String) {
                            int len_value = ((String) new_value).length();
                            try {
                                if (len_value == 0) {
                                    field.set(obj, null);
                                } else if (len_value >= 6 && len_value <= 10) {
                                    field.set(obj, FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject((String) new_value));
                                } else if (len_value == 16) {
                                    field.set(obj, FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject((String) new_value));
                                } else if (len_value == 19) {
                                    field.set(obj, FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject((String) new_value));
                                } else {
                                    throw new RestApiException(m_erro);
                                }
                            } catch (ParseException e) {
                                try {
                                    if (len_value == 0) {
                                        field.set(obj, null);
                                    } else if (len_value >= 6 && len_value <= 10) {
                                        field.set(obj, FastDateFormat.getInstance(dateFormatYYYYMMDD).parseObject((String) new_value));
                                    } else if (len_value == 16) {
                                        field.set(obj, FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmm).parseObject((String) new_value));
                                    } else if (len_value == 19) {
                                        field.set(obj, FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmmss).parseObject((String) new_value));
                                    } else {
                                        throw new RestApiException(m_erro);
                                    }
                                } catch (ParseException em) {
                                    throw new RestApiException(m_erro);
                                }
                            }
                        } else {
                            throw new RestApiException(m_erro);
                        }
                    } else if (tipo_field == java.sql.Date.class) {
                        final String m_erro = field_name + " não é uma data válida.";
                        if (new_value instanceof String) {
                            int len_value = ((String) new_value).length();
                            try {
                                if (len_value == 0) {
                                    field.set(obj, null);
                                } else if (len_value >= 6 && len_value <= 10) {
                                    field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject((String) new_value)).getTime()));
                                } else if (len_value == 16) {
                                    field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject((String) new_value)).getTime()));
                                } else if (len_value == 19) {
                                    field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject((String) new_value)).getTime()));
                                } else {
                                    throw new RestApiException(m_erro);
                                }
                            } catch (ParseException e) {
                                try {
                                    if (len_value == 0) {
                                        field.set(obj, null);
                                    } else if (len_value >= 6 && len_value <= 10) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD).parseObject((String) new_value)).getTime()));
                                    } else if (len_value == 16) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmm).parseObject((String) new_value)).getTime()));
                                    } else if (len_value == 19) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmmss).parseObject((String) new_value)).getTime()));
                                    } else {
                                        throw new RestApiException(m_erro);
                                    }
                                } catch (ParseException em) {
                                    throw new RestApiException(m_erro);
                                }
                            }
                        } else {
                            throw new RestApiException(m_erro);
                        }
                    } else if (tipo_field == java.sql.Timestamp.class) {
                        final String m_erro = field_name + " não é uma data válida.";
                        java.sql.Timestamp new_time = null;
                        if (new_value instanceof String) {
                            int len_value = ((String) new_value).length();
                            try {
                                if (len_value == 0) {
                                    new_time = null;
                                } else if (len_value >= 6 && len_value <= 10) {
                                    new_time = new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject((String) new_value)).getTime());
                                } else if (len_value == 16) {
                                    new_time = new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject((String) new_value)).getTime());
                                } else if (len_value == 19) {
                                    new_time = new java.sql.Timestamp(((Date) FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject((String) new_value)).getTime());
                                } else {
                                    throw new RestApiException(m_erro);
                                }
                            } catch (ParseException e) {
                                try {
                                    if (len_value == 0) {
                                        field.set(obj, null);
                                    } else if (len_value >= 6 && len_value <= 10) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD).parseObject((String) new_value)).getTime()));
                                    } else if (len_value == 16) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmm).parseObject((String) new_value)).getTime()));
                                    } else if (len_value == 19) {
                                        field.set(obj, new java.sql.Date(((Date) FastDateFormat.getInstance(dateFormatYYYYMMDD_HHmmss).parseObject((String) new_value)).getTime()));
                                    } else {
                                        throw new RestApiException(m_erro);
                                    }
                                } catch (ParseException em) {
                                    throw new RestApiException(m_erro);
                                }
                            }
                            field.set(obj, new_time);
                        } else {
                            throw new RestApiException(m_erro);
                        }
                    } else if (tipo_field.isEnum()) {
                        try {
                            Integer idValue = null;
                            Enum<?> value = null;
                            if (new_value instanceof String) {
                                try {
                                    idValue = Integer.parseInt((String) new_value);
                                    value = EnumUtils.intToEnum(idValue, (Class<Enum>) tipo_field);
                                } catch (NumberFormatException e) {
                                    value = EnumUtils.strToEnum((String) new_value, (Class<Enum>) tipo_field);
                                }
                            } else {
                                idValue = ((Double) new_value).intValue();
                                value = EnumUtils.intToEnum(idValue, (Class<Enum>) tipo_field);
                            }
                            field.set(obj, value);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            throw new RestApiException(field_name + " não é v�lido.");
                        }
                    } else if ((tipo_field.newInstance()) instanceof ItemTabEstruturada) {
                        int intValue = (int) new_value;
                        ItemTabEstruturada tab = ((ItemTabEstruturada) tipo_field.newInstance());
                        tab.setItem(intValue);
                        field.set(obj, tab);
                    } else if (tipo_field == byte[].class) {
                        byte[] value = null;

                        if (new_value instanceof ArrayList) {
                            value = toByteArray((ArrayList) new_value);
                        } else {
                            value = toByteArray(new ArrayList(((ArrayMap<Integer, Double>) new_value).values()));
                        }

                        field.set(obj, value);
                    } else if (tipo_field instanceof Object
                               && findFieldByAnnotation(tipo_field, Id.class) != null) {
                        try {
                            Integer idValue = null;
                            if (new_value instanceof String) {
                                idValue = Integer.parseInt((String) new_value);
                            } else {
                                idValue = ((Double) new_value).intValue();
                            }
                            if (idValue > 0 && jsonModelAdapter != null) {
                                Object model = jsonModelAdapter.findById(tipo_field, idValue);
                                field.set(obj, model);
                            }
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            throw new RestApiException(field_name + " inválido.");
                        }
                    } else {
                        throw new RestApiException("Não suporta o tipo de dado do campo " + field_name + ".");
                    }
                } catch (IllegalAccessException | RestApiException | InstantiationException e) {
                    throw new RestApiException("Campo " + field_name + " inválido. Motivo: " + e.getMessage());
                }
            }
        }
        return obj;
    }

    /**
     * Retorna o primeiro campo que encontrar a anotação passada como argumento.
     *
     * @param clazz Classe pojo. Ex.: OrgaoInterno.class
     * @param ann	  anotação que será pesquisada. Ex.: Id.class
     * @return Field ou null se Não encontrado
     * @author Everton de Vargas Agilar
     */
    public static Field findFieldByAnnotation(final Class<?> clazz, final Class<? extends Annotation> ann) {
        if (clazz != null && ann != null) {
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
     * Retorna a classe Field que representa o atributo id de uma entidade.
     *
     * @param clazz Classe pojo. Ex.: OrgaoInterno.class
     * @return Field ou null se Não encontrado
     * @author Everton de Vargas Agilar
     */
    public static Field findIdField(final Class<?> clazz) {
        if (clazz != null) {
            // Tenta buscar um atributo que tem a anotação Id da JPA
            Class<?> c = clazz;
            while (c != null) {
                for (Field field : c.getDeclaredFields()) {
                    if (field.getName().equals("id") || field.isAnnotationPresent(Id.class)) {
                        return field;
                    }
                }
                c = c.getSuperclass();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static byte[] toByteArray(List new_value) {
        final int n = new_value.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            double valor = (double) new_value.get(i);
            ret[i] = (byte) valor;
        }
        return ret;
    }

    //////////////////////////////////////////
    /**
     * Serializa um objeto para json
     *
     * @param obj Objeto que será serializado para json
     * @return string json da serialização
     * @author Everton de Vargas Agilar
     */
    public static String toJson(final Object obj) {
        return toJson(obj, false);
    }

    /**
     * Serializa um objeto para json
     *
     * @param obj                 Objeto que será serializado para json
     * @param serializeFullObject Se true, serializa atributos de classe também
     * @return string json da serialização
     * @author Everton de Vargas Agilar
     */
    public static String toJson(final Object obj, boolean serializeFullObject) {
        if (obj != null) {
            String result;
            if (serializeFullObject) {
                result = gson2.toJson(obj);
            } else {
                result = gson.toJson(obj);
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Serializa um objeto a partir de uma string json
     *
     * @param jsonString String json
     * @param <T>        String json
     * @param classOfObj	Classe do objeto que será serializado
     * @return objeto
     * @throws java.lang.Exception
     * @author Everton de Vargas Agilar
     */
    public static <T> T fromJson(final String jsonString, final Class<T> classOfObj) throws Exception {
        return (T) fromJson(jsonString, classOfObj, null);
    }

    /**
     * Serializa um objeto a partir de uma string json.
     * Quando a string json for vazio, apenas Instância um objeto da classe.
     *
     * @param jsonString       String json
     * @param <T>              String json
     * @param classOfObj	      Classe do objeto que será serializado
     * @param jsonModelAdapter adaptador para permitir obter atributos de modelo
     * @return objeto
     * @author Everton de Vargas Agilar
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String jsonString, final Class<T> classOfObj, final JsonModelAdapter jsonModelAdapter) throws Exception {
        if (classOfObj != null) {
            try {
                if (jsonString != null && !jsonString.isEmpty()) {
                    jsonString = unquoteString(jsonString.trim());
                    if (classOfObj == List.class || classOfObj == ArrayList.class) {
                        List<Object> values = gson.fromJson(jsonString, List.class);
                        return (T) values;
                    } else if (classOfObj == java.util.HashMap.class || classOfObj == Map.class) {
                        Map<String, Object> values = gson.fromJson(jsonString, Map.class);
                        return (T) values;
                    } else {
                        Map<String, Object> values = gson.fromJson(jsonString, Map.class);
                        T obj = null;
                        try {
                            obj = classOfObj.getConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RestApiException("Não suporta conversão do json da classe " + classOfObj.getSimpleName() + ". Json: " + jsonString);
                        }
                        setValuesFromMap(obj, values, jsonModelAdapter);
                        return obj;
                    }
                }
                return classOfObj.getConstructor().newInstance();
            } catch (JsonSyntaxException e) {
                throw new RestApiException("Sintáxe do JSON inválida.");
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RestApiException("Não suporta Instânciar objeto para a classe " + classOfObj.getSimpleName());
            }
        } else {
            throw new RestApiException("Parâmetro classOfObj do método fromJson não deve ser null.");
        }
    }

    /**
     * Obtém uma lista a partir de um json
     *
     * @param jsonString String json
     * @param <T>        String json
     * @param classOfObj	Classe do objeto que será serializado
     * @return list
     * @author Everton de Vargas Agilar
     */
    public static <T> List<T> fromListJson(final String jsonString, final Class<T> classOfObj) {
        return fromListJson(jsonString, classOfObj, null);
    }

    /**
     * Obtém uma lista a partir de um json. Se o json estiver vazio, retorna apenas uma lista vazia.
     *
     * @param jsonString       String json
     * @param classOfObj	      Classe do objeto que será serializado
     * @param <T>	             Classe do objeto que será serializado
     * @param jsonModelAdapter adaptador para permitir obter atributos de modelo
     * @return list
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> fromListJson(final String jsonString, final Class<T> classOfObj, final JsonModelAdapter jsonModelAdapter) {
        if (classOfObj != null) {
            ArrayList<T> newList = new ArrayList<>();
            if (jsonString != null && !jsonString.isEmpty()) {
                List<Object> values;
                try {
                    values = gson.fromJson(jsonString, List.class);
                } catch (JsonSyntaxException e) {
                    throw new RestApiException("Sintáxe do JSON inválida.");
                }
                for (Object value : values) {
                    try {
                        T obj = classOfObj.getConstructor().newInstance();
                        setValuesFromMap(obj, (Map<String, Object>) value, jsonModelAdapter);
                        newList.add(obj);
                    } catch (Exception e) {
                        throw new RestApiException("Não suporta conversão do json para " + classOfObj.getSimpleName() + ". Json: " + jsonString);
                    }
                }
            }
            return newList;
        } else {
            throw new RestApiException("Parâmetro classOfObj do método fromListJson Não deve ser null.");
        }
    }

    /**
     * Seta os valores nos Parâmetros de um query a partir de um map
     *
     * @param query  query com Parâmetros a setar
     * @param values	map com chave/valor dos dados que seráo aplicados na query
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setQueryParameterFromMap(final Query query, final Map<String, Object> values) {
        setQueryParameterFromMap(query, values, 1);
    }

    /**
     * Seta os valores nos Parâmetros de um query a partir de um map
     *
     * @param query          query com Parâmetros a setar
     * @param values	        map com chave/valor dos dados que seráo aplicados na query
     * @param parameterIndex a apartir de que índice deve acessar os parameters da query
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setQueryParameterFromMap(final Query query, final Map<String, Object> values, int parameterIndex) {
        if (query != null && values != null && values.size() > 0) {
            for (String field : values.keySet()) {
                try {
                    String[] field_defs = field.split("__");
                    String field_name;
                    String field_op;
                    int field_len = field_defs.length;
                    if (field_len == 1) {
                        field_name = field_defs[0];
                        field_op = "=";
                    } else if (field_len == 2) {
                        field_name = field_defs[0];
                        field_op = field_defs[1];
                        if (field_op.equals("isnull")) {
                            continue;
                        }
                    } else {
                        throw new RestApiException("Campo de pesquisa " + field + " inválido");
                    }
                    Object value_field = values.get(field);
                    Class<?> paramType = query.getParameter(parameterIndex).getParameterType();
                    if (paramType == null) {
                        if (value_field instanceof ArrayList<?>) {
                            paramType = ((ArrayList<?>) value_field).get(0).getClass();
                        } else {
                            paramType = value_field.getClass();
                        }
                    }
                    if (paramType == Integer.class) {
                        if (value_field instanceof String) {
                            query.setParameter(parameterIndex++, Integer.parseInt((String) value_field));
                        } else if (value_field instanceof Double) {
                            query.setParameter(parameterIndex++, ((Double) value_field).intValue());
                        } else if (value_field instanceof ArrayList<?>) {
                            //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                            List<Integer> value_field_parameter = new ArrayList<>();
                            if (((ArrayList) value_field).size() > 0) {
                                //Tests the type of the array using the first position
                                if (((ArrayList) value_field).get(0) instanceof String) {
                                    for (String string : (ArrayList<String>) value_field) {
                                        value_field_parameter.add(Integer.parseInt(string));
                                    }
                                } else if (((ArrayList) value_field).get(0) instanceof Double) {
                                    for (Double doubleValue : (ArrayList<Double>) value_field) {
                                        value_field_parameter.add(doubleValue.intValue());
                                    }
                                }
                            }
                            query.setParameter(parameterIndex++, value_field_parameter);
                        } else {
                            query.setParameter(parameterIndex++, value_field);
                        }
                    } else if (paramType == BigDecimal.class) {
                        if (value_field instanceof String) {
                            query.setParameter(parameterIndex++, BigDecimal.valueOf(Double.parseDouble((String) value_field)));
                        } else {
                            query.setParameter(parameterIndex++, BigDecimal.valueOf((double) value_field));
                        }
                    } else if (paramType == Double.class || paramType == double.class) {
                        if (value_field instanceof ArrayList<?>) {
                            //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                            List<Double> value_field_parameter = new ArrayList<Double>();
                            if (((ArrayList) value_field).size() > 0) {
                                //Tests the type of the array using the first position
                                if (((ArrayList) value_field).get(0) instanceof String) {
                                    for (String string : (ArrayList<String>) value_field) {
                                        value_field_parameter.add(Double.valueOf(string));
                                    }
                                } else if (((ArrayList) value_field).get(0) instanceof Double) {
                                    for (Double doubleValue : (ArrayList<Double>) value_field) {
                                        value_field_parameter.add(doubleValue);
                                    }
                                }
                            }
                            query.setParameter(parameterIndex++, value_field_parameter);
                        } else {
                            double valueDouble = parseAsDouble(value_field);
                            query.setParameter(parameterIndex++, Double.valueOf(valueDouble));
                        }
                    } else if (paramType == Long.class || paramType == long.class) {
                        if (value_field instanceof ArrayList<?>) {
                            //Used in the IN clause, accepts only homogeneous arrays of strings or doubles.
                            List<Double> value_field_parameter = new ArrayList<Double>();
                            if (((ArrayList) value_field).size() > 0) {
                                //Tests the type of the array using the first position
                                if (((ArrayList) value_field).get(0) instanceof String) {
                                    for (String string : (ArrayList<String>) value_field) {
                                        value_field_parameter.add(Double.valueOf(string));
                                    }
                                } else if (((ArrayList) value_field).get(0) instanceof Double) {
                                    for (Double doubleValue : (ArrayList<Double>) value_field) {
                                        value_field_parameter.add(doubleValue);
                                    }
                                }
                            }
                            query.setParameter(parameterIndex++, value_field_parameter);
                        } else {
                            Double valueDouble = parseAsDouble(value_field);
                            query.setParameter(parameterIndex++, valueDouble.longValue());
                        }

                    } else if (paramType.isEnum()) {

                        Integer idValue;
                        Enum<?> valueEnum;
                        if (value_field instanceof String) {
                            try {
                                idValue = Integer.parseInt((String) value_field);
                                valueEnum = EnumUtils.intToEnum(idValue, (Class<Enum>) paramType);
                            } catch (NumberFormatException e) {
                                valueEnum = EnumUtils.strToEnum((String) value_field, (Class<Enum>) paramType);
                            }
                        } else {
                            idValue = ((Double) value_field).intValue();
                            valueEnum = EnumUtils.intToEnum(idValue, (Class<Enum>) paramType);
                        }

                        query.setParameter(parameterIndex++, valueEnum);

                    } else if (paramType == String.class) {
                        String valueString;
                        if (value_field instanceof Double) {
                            // Parece um inteiro? (termina com .0)
                            if (value_field.toString().endsWith(".0")) {
                                valueString = Integer.toString(((Double) value_field).intValue());
                            } else {
                                valueString = value_field.toString();
                            }
                        } else {
                            valueString = value_field.toString();
                        }

                        // Muito cuidado com valueString == "" para Não gerar um sql like %%
                        if (!valueString.isEmpty()) {
                            switch (field_op) {
                                case "contains":
                                    valueString = "%" + valueString + "%";
                                    break;
                                case "icontains":
                                    valueString = "%" + valueString.toLowerCase() + "%";
                                    break;
                                case "like":
                                    valueString = valueString + "%";
                                    break;
                                case "ilike":
                                    valueString = valueString.toLowerCase() + "%";
                                    break;
                            }
                        }

                        query.setParameter(parameterIndex++, valueString);
                    } else if (paramType == Boolean.class) {
                        boolean value_boolean = parseAsBoolean(value_field);
                        query.setParameter(parameterIndex++, value_boolean);
                    } else if (paramType == java.util.Date.class) {
                        final String m_erro = field_name + " não é uma data válida.";
                        if (value_field instanceof String) {
                            int len_value = ((String) value_field).length();
                            try {
                                if (len_value >= 6 && len_value <= 10) {
                                    query.setParameter(parameterIndex++, FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject((String) value_field));
                                } else if (len_value == 16) {
                                    query.setParameter(parameterIndex++, FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject((String) value_field));
                                } else if (len_value == 19) {
                                    query.setParameter(parameterIndex++, FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject((String) value_field));
                                } else {
                                    throw new RestApiException(m_erro);
                                }
                            } catch (ParseException e) {
                                throw new RestApiException(m_erro);
                            }
                        } else {
                            throw new RestApiException(m_erro);
                        }
                    } else {
                        query.setParameter(parameterIndex++, value_field);
                    }
                } catch (RestApiException | NumberFormatException e) {
                    throw new RestApiException("Erro ao setar Parâmetros da query. Motivo: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retorna o id de um objeto. O id é um campo que tenha a anotação @Id da JPA
     *
     * @param obj	objeto
     * @return Id ou null se Não encontrado
     * @author Everton de Vargas Agilar
     */
    public static Integer getIdFromObject(final Object obj) {
        if (obj != null) {
            Field idField = findFieldByAnnotation(obj.getClass(), Id.class);
            if (idField != null) {
                try {
                    idField.setAccessible(true);
                    Object result = idField.get(obj);
                    if (result != null) {
                        return (int) result;
                    } else {
                        return null;
                    }
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    return null;
                }
            } else {
                throw new RestApiException("Objeto Não tem id.");
            }
        } else {
            throw new RestApiException("Parâmetro Obj do método getIdFromObject Não deve ser null.");
        }
    }

    /**
     * Converte um inteiro para a enumeração de acordo com clazz
     *
     * @param value código da enumeração
     * @param clazz	classe da enumeração
     * @return enumeração
     * @author Everton de Vargas Agilar
     */
    public static Enum<?> intToEnum(int value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
        if (clazz != null) {
            if (value >= 0) {
                for (Enum<?> t : clazz.getEnumConstants()) {
                    if (t.ordinal() == value) {
                        return t;
                    }
                }
            }
            throw new RestApiException("Valor inválido para o campo " + clazz.getSimpleName());
        } else {
            throw new RestApiException("Parâmetro clazz do método intToEnum Não deve ser null.");
        }
    }

    /**
     * Converte a descrição da enumeração para a enumeração de acordo com clazz
     *
     * @param value descrição da enumeração
     * @param clazz	classe da enumeração
     * @return enumeração
     * @author Everton de Vargas Agilar
     */
    public static Enum<?> StrToEnum(final String value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
        if (value != null && !value.isEmpty() && clazz != null) {
            for (Enum<?> t : clazz.getEnumConstants()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
            throw new RestApiException("Valor inválido para o campo " + clazz.getSimpleName());
        } else {
            throw new RestApiException("Parâmetros clazz e value do método StrToEnum não devem ser null.");
        }
    }

    public static String fieldOperatorToSqlOperator(final String fieldOperator) {
        switch (fieldOperator) {
            case "contains":
                return " like ";
            case "icontains":
                return " like ";
            case "like":
                return " like ";
            case "ilike":
                return " like ";
            case "notcontains":
                return " not like ";
            case "inotcontains":
                return " not like ";
            case "notlike":
                return " like ";
            case "inotlike":
                return " not like ";
            case "gt":
                return " > ";
            case "gte":
                return " >= ";
            case "lt":
                return " < ";
            case "lte":
                return " <= ";
            case "e":
                return " = ";
            case "ne":
                return " != ";
            case "isnull":
                return " is null ";
            case "equal":
                return " = ";
            case "in":
                return " in ";
        }
        throw new RestApiException("Operador de atributo " + fieldOperator + " inválido.");
    }

    public static String listFunctionToSqlFunction(final List<String> listFunction) {
        if (listFunction != null) {
            if (listFunction.isEmpty() || (listFunction.size() != 2)) {
                throw new RestApiException("Função SQL precisa de um operador e de uma coluna para listFunctionToSqlFunction.");
            }
            String function = listFunction.get(0);
            switch (function) {
                case "avg":
                    return " avg (" + listFunction.get(1) + ") ";
                case "count":
                    return " count (" + listFunction.get(1) + ") ";
                case "first":
                    return " first (" + listFunction.get(1) + ") ";
                case "last":
                    return " last (" + listFunction.get(1) + ") ";
                case "max":
                    return " max (" + listFunction.get(1) + ") ";
                case "min":
                    return " min (" + listFunction.get(1) + ") ";
                case "sum":
                    return " sum (" + listFunction.get(1) + ") ";
            }
            throw new RestApiException("Função SQL " + function + " inválido para listFunctionToSqlFunction.");
        } else {
            throw new RestApiException("Parâmetro listFunction não pode ser null para listFunctionToSqlFunction.");
        }
    }

    /**
     * Parse um objeto String, Double ou Boolean em um valor boolean.
     *
     * @param fieldValue valor String, Double ou Boolean.
     * @return boolean
     * @author Everton de Vargas Agilar (revisão)
     */
    public static boolean parseAsBoolean(final Object fieldValue) {
        if (fieldValue == null) {
            return false;
        } else if (fieldValue instanceof String) {
            if (((String) fieldValue).equalsIgnoreCase("true")) {
                return true;
            } else if (((String) fieldValue).equalsIgnoreCase("false")) {
                return false;
            } else if (((String) fieldValue).equalsIgnoreCase("1")) {
                return true;
            } else if (((String) fieldValue).equalsIgnoreCase("0")) {
                return false;
            } else if (((String) fieldValue).equalsIgnoreCase("sim")) {
                return true;
            } else if (((String) fieldValue).equalsIgnoreCase("1.0")) {
                return true;
            } else if (((String) fieldValue).equalsIgnoreCase("yes")) {
                return true;
            } else {
                return false;
            }
        } else if (fieldValue instanceof Double) {
            if (fieldValue.toString().equals("1.0")) {
                return true;
            } else {
                return false;
            }
        } else if (fieldValue instanceof Boolean) {
            return ((Boolean) fieldValue).booleanValue();
        } else {
            return false;
        }
    }


    /**
     * Parse um objeto String, Double ou Float em um valor Double.
     *
     * @param value_field valor String, Double ou Float.
     * @return Double ou null
     * @author Everton de Vargas Agilar
     */
    public static Double parseAsDouble(final Object value_field) {
        if (value_field != null) {
            if (value_field instanceof String) {
                return Double.parseDouble((String) value_field);
            } else if (value_field instanceof Double) {
                return ((Double) value_field).doubleValue();
            } else {
                return ((Float) value_field).doubleValue();
            }
        } else {
            return null;
        }
    }

    public static Object parseAsString(final RestField field, final Object fieldValue) {
        Object result = fieldValue;
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                // ok
            } else if (fieldValue instanceof Double) {
                // Parece um inteiro? (termina com .0)
                if (fieldValue.toString().endsWith(".0")) {
                    result = Integer.toString(((Double) fieldValue).intValue());
                } else {
                    result = fieldValue.toString();
                }
            } else if (fieldValue instanceof Integer) {
                result = fieldValue.toString();
            } else if (fieldValue instanceof Boolean) {
                result = fieldValue.toString();
            } else {
                throw new RestApiException(String.format(RestApiException.VALOR_ATRIBUTO_INCOMPATIVEL, field.getVoFieldName()));
            }
        }
        return result;
    }

    public static Object parseDate(final RestField field, final Object fieldValue) {
        Object result = fieldValue;
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                int len_value = ((String) fieldValue).length();
                try {
                    if (len_value >= 6 && len_value <= 10) {
                        result = FastDateFormat.getInstance(dateFormatDDMMYYYY).parseObject((String) fieldValue);
                    } else if (len_value == 16) {
                        result = FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmm).parseObject((String) fieldValue);
                    } else if (len_value == 19) {
                        result = FastDateFormat.getInstance(dateFormatDDMMYYYY_HHmmss).parseObject((String) fieldValue);
                    } else {
                        throw new RestApiException(String.format(RestApiException.VALOR_ATRIBUTO_INCOMPATIVEL, field.getVoFieldName()));
                    }
                } catch (RestApiException ex) {
                    throw ex;
                } catch (ParseException e) {
                    throw new RestApiException(String.format(RestApiException.VALOR_ATRIBUTO_INCOMPATIVEL, field.getVoFieldName()));
                }
            } else {
                throw new RestApiException(String.format(RestApiException.VALOR_ATRIBUTO_INCOMPATIVEL, field.getVoFieldName()));
            }
        }
        return result;
    }

    /**
     * Obter o array de unique constraints de um model
     *
     * @param classOfModel classe do modelo
     * @return array of UniqueConstraint
     * @author Everton de Vargas Agilar
     */
    public static UniqueConstraint[] getTableUniqueConstraints(final Class<?> classOfModel) {
        if (classOfModel != null) {
            Table tableAnnotation = classOfModel.getAnnotation(Table.class);
            return tableAnnotation.uniqueConstraints();
        } else {
            throw new RestApiException("Parâmetro classOfModel não pode ser null para getTableUniqueConstraints.");
        }
    }

    /**
     * Obter a lista de fields com unique constraint de um model.
     * Obs.: Id não é retornado embora tenha a constraint unique.
     *
     * @param classOfModel classe do modelo
     * @return lista de campos
     * @author Everton de Vargas Agilar
     */
    public static List<Field> getFieldsWithUniqueConstraint(final Class<?> classOfModel) {
        if (classOfModel != null) {
            Field[] fields = classOfModel.getDeclaredFields();
            List<Field> result = new ArrayList<>();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class
                ).unique() && !field.isAnnotationPresent(Id.class)) {
                    result.add(field);
                }
            }
            return result;
        } else {
            throw new RestApiException("Parâmetro classOfModel não pode ser null para getFieldsWithUniqueConstraint.");
        }
    }

    /**
     * Obter a lista de fields de um model.
     * Obs.: somente fields com a anotação Column s�o retornados.
     *
     * @param classOfModel classe do modelo
     * @return lista de campos
     * @author Everton de Vargas Agilar
     */
    public static List<Field> getFieldsFromModel(final Class<?> classOfModel) {
        if (classOfModel != null) {
            Field[] fields = classOfModel.getDeclaredFields();
            List<Field> result = new ArrayList<>();
            result.addAll(Arrays.asList(fields));
            return result;
        } else {
            throw new RestApiException("Parâmetro classOfModel não pode ser null para getFieldsFromModel.");
        }
    }

    /**
     * Realiza a conversão de um lista para map
     *
     * Para que seja possível a conversão é necessário passar a lista dos campos (fields).
     *
     * @param fields  lista de campos. Pode ser passado como um array de campos, string de campos separado por vírgula ou lista de campos.
     * @param listObj lista de objetos
     * @return list ou exception Exception
     * @author Everton de Vargas Agilar,
     * @author Rogério Guimarães Sampaio
     */
    public static List<Map<String, Object>> ListObjectToListMap(final Object fields, final List<?> listObj) {
        if (fields != null && listObj != null) {
            String[] fieldNames = null;
            if (fields instanceof String) {
                fieldNames = ((String) fields).split(",");
            } else if (fields instanceof String[]) {
                fieldNames = (String[]) fields;
            } else if (fields instanceof List<?>) {
                fieldNames = (String[]) ((List<?>) fields).toArray();
            } else {
                throw new RestApiException("Parâmetro fields não é do tipo correto para ListObjectToListMap.");
            }
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(listObj.size());
            int colSize = fieldNames.length;
            for (Object obj : listObj) {
                int index = 0;
                Map<String, Object> objVo = new HashMap<>(colSize);
                for (String fieldName : fieldNames) {
                    objVo.put(fieldName, ((Object[]) obj)[index++]);
                }
                result.add(objVo);
            }
            return result;
        } else {
            throw new RestApiException("Parâmetros fields e ListObj não podem ser null para ListObjectToListMap.");
        }
    }

    /**
     * Classe responsável por representar um filtro após o parser.
     *
     * @author Everton de Vargas Agilar
     */
    public static class EmsFilterStatement {
        StringBuilder where = null;
        Map<String, Object> filtro_obj = null;
        public EmsFilterStatement(final StringBuilder where, final Map<String, Object> filtro_obj) {
            this.where = where;
            this.filtro_obj = filtro_obj;
        }
    }

    /**
     * Faz o parser do Parâmetro filter e retorna a cláusula where para um sql nativo.
     * Se Não for informado o Parâmetro filter ou o filtro for vazio, retorna null.
     * Se o filter estiver com sintáxe incorreta, retorna exception Exception.
     *
     * @param filter filtro. Ex.: {"nome":"Everton de Vargas Agilar", "ativo":true}
     * @return filtro ou null
     * @author Everton de Vargas Agilar
     */
    @SuppressWarnings("unchecked")
    public static EmsFilterStatement parseSqlNativeFilter(final String filter) {
        if (filter != null && filter.length() > 5) {
            try {
                StringBuilder where;
                Map<String, Object> filtro_obj;
                boolean useAnd = false;
                filtro_obj
                = (Map<String, Object>) fromJson(filter, HashMap.class
                        );
                where = new StringBuilder(" where ");
                for (String field : filtro_obj.keySet()) {
                    if (useAnd) {
                        where.append(" and ");
                    }
                    String[] field_defs = field.split("__");
                    String fieldName;
                    String fieldOperator;
                    String sqlOperator;
                    int field_len = field_defs.length;
                    if (field_len == 1) {
                        fieldName = field;
                        fieldOperator = "=";
                        sqlOperator = "=";
                    } else if (field_len == 2) {
                        fieldName = field_defs[0];
                        fieldOperator = field_defs[1];
                        sqlOperator = fieldOperatorToSqlOperator(fieldOperator);
                    } else {
                        throw new RestApiException("Campo de pesquisa " + field + " inválido.");
                    }
                    if (field_len == 2) {
                        if (fieldOperator.equals("isnull")) {
                            boolean fieldBoolean = parseAsBoolean(filtro_obj.get(field));
                            if (fieldBoolean) {
                                where.append(fieldName).append(" is null ");
                            } else {
                                where.append(fieldName).append(" is not null ");
                            }
                        } else if (fieldOperator.equals("icontains") || fieldOperator.equals("ilike")) {
                            fieldName = String.format("lower(this.%s)", fieldName);
                            where.append(fieldName).append(sqlOperator).append("?");
                        } else {
                            fieldName = String.format("this.%s", fieldName);
                            where.append(fieldName).append(sqlOperator).append("?");
                        }
                    } else {
                        fieldName = String.format("this.%s", fieldName);
                        where.append(fieldName).append(sqlOperator).append("?");
                    }
                    useAnd = true;
                }
                return new EmsFilterStatement(where, filtro_obj);
            } catch (Exception e) {
                throw new RestApiException("Filtro da pesquisa inválido. Motivo: " + e.getMessage());
            }
        } else {
            return null;
        }
    }

    /**
     * Faz o unquote da string removendo os \" do início ao fim
     *
     * @param str string
     * @return str
     * @author Everton de Vargas Agilar
     */
    public static String unquoteString(final String str) {
        if (str != null && !str.isEmpty()) {
            return str.replaceAll("^\"|\"$", "").replaceAll("^\'|\'$", "");
        }
        return str;
    }

    /**
     * Remove todos os espaços de uma string.
     *
     * @param str
     * @return str string sem os espaços
     * @author Everton de Vargas Agilar
     */
    public static String removeAllSpaces(final String str) {
        if (str != null && !str.isEmpty()) {
            return str.replaceAll("\\s+", "");
        }
        return str;
    }


}
