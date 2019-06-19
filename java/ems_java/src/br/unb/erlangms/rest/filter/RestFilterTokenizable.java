package br.unb.erlangms.rest.filter;

import br.unb.erlangms.rest.exception.RestTokenizableException;
import br.unb.erlangms.rest.filter.tokens.RestFilterAndToken;
import br.unb.erlangms.rest.filter.tokens.RestFilterJsonToken;
import br.unb.erlangms.rest.filter.tokens.RestFilterOrToken;
import br.unb.erlangms.rest.filter.tokens.RestFilterToken;
import java.util.ArrayList;

/**
 * Analisador léxico do operador filter
 *
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @version 1.0.0
 * @since 26/03/2019
 *
 */
public class RestFilterTokenizable implements IRestFilterTokenizable {

    private char[] src;
    private ArrayList<RestFilterToken> tokens;
    private int lookahead;
    private StringBuilder cur;

    private void skipSpaces() {
        while (lookahead < src.length && Character.isSpaceChar(src[lookahead])) {
            lookahead++;
        }
    }

    private void emit(char ch){
       if (src[lookahead] != ch){
           throw new RestTokenizableException("Esperado "+ ch + " mas "+ src[lookahead] + " encontrado.");
       }
        cur.append(src[lookahead++]);
    }

    @Override
    public ArrayList<RestFilterToken> tokenize(final char[] filter) {
        src = filter;
        tokens = new ArrayList<>();
        lookahead = 0;
        cur = new StringBuilder();
        while (lookahead < src.length) {
            if (Character.isSpaceChar(src[lookahead])) {
                skipSpaces();
            } else if (src[lookahead] == '{') {
                if (cur.length() > 0) {
                    tokens.add(new RestFilterToken(cur.toString(), lookahead));
                }
                cur = new StringBuilder();
                emit('{');
                skipSpaces();
                while (lookahead < src.length && src[lookahead] != '}') {
                    cur.append(src[lookahead]);
                    lookahead++;
                }
                emit('}');
                tokens.add(new RestFilterJsonToken(cur.toString(), lookahead-1));
                cur = new StringBuilder();
            } else {
                if (src[lookahead] == '(') {
                    tokens.add(new RestFilterToken("(", lookahead));
                    emit('(');
                    cur = new StringBuilder();
                } else if (src[lookahead] == ')') {
                    tokens.add(new RestFilterToken(")", lookahead));
                    emit(')');
                    cur = new StringBuilder();
                } else {
                    cur.append(src[lookahead++]);
                    switch (cur.toString().toLowerCase()) {
                        case "or":
                            tokens.add(new RestFilterOrToken("or", lookahead));
                            cur = new StringBuilder();
                            break;
                        case "and":
                            tokens.add(new RestFilterAndToken("and", lookahead));
                            cur = new StringBuilder();
                    }
                }
            }
        }
        if (cur.length() > 0) {
            tokens.add(new RestFilterToken(cur.toString(), lookahead));
        }
        return tokens;
    }
}
