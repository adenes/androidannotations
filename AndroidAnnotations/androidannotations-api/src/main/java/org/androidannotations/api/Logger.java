package org.androidannotations.api;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by denes on 16/07/14.
 */
public final class Logger {

	private final Class<?> ownerClass;
	private final String tag;
	private final boolean enabled;

	private static final ConcurrentMap<String, Logger> INSTANCES = new ConcurrentHashMap<String, Logger>();

	private Logger(Class<?> ownerClass, String tag, boolean enabled) {
		this.ownerClass = ownerClass;
		this.tag = tag;
		this.enabled = enabled;
	}

	public static Logger logger(Object ownerObject, String tag, boolean enabled) {
		return logger(ownerObject.getClass(), tag, enabled);
	}

	public static Logger logger(Class<?> ownerClass, String tag, boolean enabled) {
		tag = validateTag(ownerClass, tag);
		String mapKey = toMapKey(ownerClass, tag, enabled);

		Logger ret = INSTANCES.get(mapKey);
		if (ret == null) {
			Logger newLogger = new Logger(ownerClass, tag, enabled);
			ret = INSTANCES.putIfAbsent(mapKey, newLogger);
			if (ret == null) ret = newLogger;
		}

		return ret;
	}

	private static String validateTag(Class<?> ownerClass, String tag) {
		if (tag == null || tag.length() == 0) {
			return ownerClass.getName();
		} else {
			return tag;
		}
	}

	private static String toMapKey(Class<?> ownerClass, String tag, boolean enabled) {
		return ownerClass.getName() + ":" + tag + ":" + enabled;
	}

	static String shortenPackageName(Class<?> cl) {
		return shortenPackageName(cl.getName());
	}

	static String shortenPackageName(String className) {
		int lastIdx = -1;
		int idx = className.indexOf('.');
		if (idx == -1) {
			return className;
		}

		StringBuilder sb = new StringBuilder(className.length());
		while (idx > -1) {
			sb.append(className.charAt(lastIdx + 1)).append('.');
			lastIdx = idx;
			idx = className.indexOf('.', lastIdx + 1);
		}

		sb.append(className.substring(lastIdx + 1));

		return sb.toString();
	}

	public Logger force() {
		if (this.enabled) {
			return this;
		} else {
			return logger(ownerClass, tag, true);
		}
	}

	private void log(int priority, String msg, Object... args) {
		if (!enabled) return;
		Log.println(priority, tag, createMessage(msg, args));

	}

	private String createMessage(String msg, Object... args) {
		if (args == null || args.length == 0) {
			return msg;
		}

		msg = String.format(msg, args);
		if (args[args.length - 1] instanceof Throwable) {
			msg += "\n" + Log.getStackTraceString((Throwable) args[args.length - 1]);
		}

		return msg;
	}

	public void v(String message, Object... args) {
		log(Log.VERBOSE, message, args);
	}

	public void d(String message, Object... args) {
		log(Log.DEBUG, message, args);
	}

	public void i(String message, Object... args) {
		log(Log.INFO, message, args);
	}


	public void w(String message, Object... args) {
		log(Log.WARN, message, args);
	}

	public void e(String message, Object... args) {
		log(Log.ERROR, message, args);
	}

}
