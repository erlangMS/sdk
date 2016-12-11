package br.erlangms;

public final class EmsResponse {
	public EmsResponse(int code, final String content) {
		this.code = code;
		this.content = content;
	}
	public int code;
	public String content;
}
