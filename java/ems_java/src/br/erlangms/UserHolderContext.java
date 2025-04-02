package br.erlangms;

public class UserHolderContext {
	private static final ThreadLocal<UserHolder> context = new ThreadLocal<>();

	public static void setUser(UserHolder userHolder) {
        context.set(userHolder);
    }

    public static UserHolder getUser() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
    
}
