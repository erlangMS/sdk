package br.erlangms;

import java.util.Date;

public class EpochValidator {

    public static boolean isEpochTimestamp(String timestampStr) {
        try {
            // Remove vírgulas e converte para long
            long timestamp = Long.parseLong(timestampStr.replace(",", ""));
            
            // A Epoch começa em 1 de janeiro de 1970, então o timestamp deve ser positivo.
            // Definimos um intervalo razoável até o ano 2100.
            long minTimestamp = 0L; // 1970-01-01 00:00:00 UTC
            long maxTimestamp = 4102444800000L; // Aproximadamente 2100-01-01 UTC

            return timestamp >= minTimestamp && timestamp <= maxTimestamp;
        } catch (NumberFormatException e) {
            return false; // Se não for um número válido, não é um Epoch timestamp
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

}
