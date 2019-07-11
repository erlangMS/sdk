package br.unb.erlangms.rest.util;

import com.google.gson.annotations.SerializedName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * <p>Funções úteis para trabalhar com enums</p>
 *
 * @author Everton de Vargas Agilar
 */

public final class EnumUtils {

    /**
    * Converte um inteiro para a enumeração de acordo com clazz
    * @param value código da enumeração
    * @param clazz	classe da enumeração
    * @return enumeração
    * @author Everton de Vargas Agilar
    */
   public static Enum<?> intToEnum(int value, final Class<Enum> clazz) {
           if (clazz != null){
                   if (value >= 0){
                           for(Enum<?> t : clazz.getEnumConstants()) {
                           if(t.ordinal() == value) {
                               return t;
                           }
                       }
                   }
                   throw new IllegalArgumentException("Valor inválido para o campo "+ clazz.getSimpleName());
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
   public static Enum<?> strToEnum(final String value, final Class<Enum> clazz) {
           if (value != null && !value.isEmpty() && clazz != null){
                   for(Enum<?> t : clazz.getEnumConstants()) {
                       if (t instanceof StringEnum){
                            if(((StringEnum)t).getValue().equalsIgnoreCase(value) ||
                               ((StringEnum)t).toString().equalsIgnoreCase(value)) {
                                 return t;
                             }
                        }else{
                           try {
                               Field fld = t.getClass().getField(t.name());
                               // Se tiver anotação, vamos procurar nas anotações também
                               Annotation[] annotations = fld.getAnnotations();
                               for (Annotation ann : annotations){
                                   if (ann instanceof XmlEnumValue){
                                       XmlEnumValue xmlEnumValue = (XmlEnumValue) ann;
                                       if (xmlEnumValue.value().equalsIgnoreCase(value)){
                                           return t;
                                       }
                                   }else if (ann instanceof SerializedName){
                                       SerializedName serializedName = (SerializedName) ann;
                                       if (serializedName.value().equalsIgnoreCase(value)){
                                           return t;
                                       }
                                   }
                               }
                           } catch (NoSuchFieldException | SecurityException | IllegalArgumentException ex) {
                               // Qualquer restrição ignora
                           }

                           if(t.name().equalsIgnoreCase(value) || t.toString().equalsIgnoreCase(value)) {
                                 return t;
                             }
                         }
               }
                   throw new IllegalArgumentException("Valor inválido para o campo "+ clazz.getSimpleName());
           }else{
                   throw new IllegalArgumentException("Parâmetros clazz e value do método EmsUtil.StrToEnum não devem ser null.");
           }
   }

}
