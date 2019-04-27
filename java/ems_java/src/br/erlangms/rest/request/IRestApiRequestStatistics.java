/*
 * 
 */
package br.erlangms.rest.request;

import java.io.Serializable;

/**
 * Classe que armazena estatísticas sobre a execução de uma requisição
 *
 *
 * @author Everton de Vargas Agilar
 * @version 1.0.0
 * @since 25/04/2019
 *
 */
public interface IRestApiRequestStatistics extends Serializable {
    long getElapsedTime();
    long getStartTime();
    long getStopTime();
    int getUsedCount();
    void incrementUsedCount();
    void setElapsedTime(long elapsedTime);
    void setStartTime(long startTime);
    void setStopTime(long stopTime);
}
