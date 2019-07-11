package br.unb.erlangms.rest.util;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

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
   public static Enum<?> intToEnum(int value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
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
   public static Enum<?> strToEnum(final String value, @SuppressWarnings("rawtypes") final Class<Enum> clazz) {
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
                               Object value2 = fld.get("value");
                               System.out.println(fld.toString());
                           } catch (NoSuchFieldException ex) {
                               Logger.getLogger(EnumUtils.class.getName()).log(Level.SEVERE, null, ex);
                           } catch (SecurityException ex) {
                               Logger.getLogger(EnumUtils.class.getName()).log(Level.SEVERE, null, ex);
                           } catch (IllegalArgumentException ex) {
                               Logger.getLogger(EnumUtils.class.getName()).log(Level.SEVERE, null, ex);
                           } catch (IllegalAccessException ex) {
                               Logger.getLogger(EnumUtils.class.getName()).log(Level.SEVERE, null, ex);
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
