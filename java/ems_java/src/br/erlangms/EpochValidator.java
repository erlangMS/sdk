package br.erlangms;

import java.util.Date;
import java.util.regex.Pattern;

public class EpochValidator {

    public static boolean isEpochTimestamp(final String timestampStr) {
        // Verifica se começa com um número e contém apenas dígitos e vírgulas
        if (!Pattern.matches("^[0-9][0-9,]*$", timestampStr)) {
            return false;
        }

        // Conta quantas vírgulas existem
        long commaCount = timestampStr.chars().filter(ch -> ch == ',').count();

        // Precisa ter exatamente 3 ou 4 vírgulas
        if (commaCount != 3 && commaCount != 4) {
            return false;
        }

        // Remove vírgulas e tenta converter para um número
        try {
            String cleanNumber = timestampStr.replace(",", "");
            long epochMillis = Long.parseLong(cleanNumber);
            return isEpochTimestamp(epochMillis);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
   
    public static boolean isEpochTimestamp(long timestamp) {
        // A Epoch começa em 1 de janeiro de 1970, então o timestamp deve ser positivo.
        // Um valor razoável seria entre 1970 e um limite futuro, como 2100.
        long minTimestamp = 0L; // 1970-01-01 00:00:00 UTC
        long maxTimestamp = 4102444800000L; // Aproximadamente 2100-01-01 UTC

        return timestamp >= minTimestamp && timestamp <= maxTimestamp;
    }

    /**
     * Converte uma string formatada com vírgulas para um objeto Date.
     */
    public static Date StrToEpochTimestamp(String timestampStr) {
        try {
            // Remove vírgulas e converte para long
            long timestamp = Long.parseLong(timestampStr.replace(",", ""));
            
            // Verifica se o timestamp é válido
            if (!isEpochTimestamp(timestamp)) {
                throw new IllegalArgumentException("O número fornecido não parece ser um timestamp válido.");
            }

            // Retorna a data correspondente
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Entrada inválida: " + timestampStr, e);
        }
    }
    
    public static Date LongToEpochTimestamp(Long timestamp) {
        try {
            if (!isEpochTimestamp(timestamp)) {
                throw new IllegalArgumentException("O número fornecido não parece ser um timestamp válido.");
            }

            // Retorna a data correspondente
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data inválida", e);
        }
    }    
    

}
