/*
 * 
 */
package br.erlangms.rest.util;

/**
 * <p>FunÃ§Ãµes Ãºteis para trabalhar com enums</p>
 *
 * @author Everton de Vargas Agilar
 */

public final class EnumUtils {

    /**
    * Converte um inteiro para a enumeraÃ§Ã£o de acordo com clazz
    * @param value código da enumeraÃ§Ã£o
    * @param clazz	classe da enumeraÃ§Ã£o
    * @return enumeraÃ§Ã£o
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
                   throw new IllegalArgumentException("ParÃ¢metro clazz do método EmsUtil.intToEnum não deve ser null.");
           }
   }

   /**
    * Converte a descriÃ§Ã£o da enumeraÃ§Ã£o para a enumeraÃ§Ã£o de acordo com clazz
    * @param value descriÃ§Ã£o da enumeraÃ§Ã£o
    * @param clazz	classe da enumeraÃ§Ã£o
    * @return enumeraÃ§Ã£o
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
                            if(t.name().equalsIgnoreCase(value) || t.toString().equalsIgnoreCase(value)) {
                                 return t;
                             }
                         }
               }
                   throw new IllegalArgumentException("Valor inválido para o campo "+ clazz.getSimpleName());
           }else{
                   throw new IllegalArgumentException("ParÃ¢metros clazz e value do método EmsUtil.StrToEnum não devem ser null.");
           }
   }

}
