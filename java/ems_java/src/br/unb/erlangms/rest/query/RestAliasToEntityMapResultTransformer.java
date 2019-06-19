package br.unb.erlangms.rest.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilizada pelo método find(String sql) para mapear os dados
 *
 * @author Everton de Vargas Agilar
 */
public class RestAliasToEntityMapResultTransformer extends org.hibernate.transform.AliasedTupleSubsetResultTransformer {

    private static final long serialVersionUID = 1L;
    public static final RestAliasToEntityMapResultTransformer INSTANCE = new RestAliasToEntityMapResultTransformer();

    /**
     * Disallow instantiation of AliasToEntityMapResultTransformer.
     */
    private RestAliasToEntityMapResultTransformer() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        int tupleLengh = tuple.length;
        if (aliases[tupleLengh - 1].startsWith("__")) {
            --tupleLengh;
        }
        Map result = new HashMap(tupleLengh);
        for (int i = 0; i < tupleLengh; i++) {
            String alias = aliases[i];
            if (alias != null) {
                result.put(alias, tuple[i]);
            }
        }
        return result;
    }

    @Override
    public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
        return false;
    }

    /**
     * Serialization hook for ensuring singleton uniqueing.
     *
     * @return The singleton instance : {@link #INSTANCE}
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
