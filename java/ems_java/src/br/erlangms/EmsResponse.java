package br.erlangms;

public final class EmsResponse {
	public EmsResponse(int code, String content) {
		this.code = code;
		this.content = content;
	}
	public int code;
	public String content;
}
