/*
 * 
 */
package br.erlangms.rest.request;


public class RestApiRequestStatistics implements IRestApiRequestStatistics {
    private long startTime;
    private long stopTime;
    private long elapsedTime;
    private int usedCount = 0;

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStopTime() {
        return stopTime;
    }

    @Override
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
        this.elapsedTime = stopTime - startTime;
    }

    @Override
    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @Override
    public int getUsedCount() {
        return usedCount;
    }

    @Override
    public void incrementUsedCount() {
        usedCount += 1;
    }

    @Override
    public String toString() {
        return "{" + "startTime=" + startTime + ", stopTime=" + stopTime + ", elapsedTime=" + elapsedTime + ", usedCount=" + usedCount + '}';
    }

}
