package com.webapp.core.util;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection utility for dynamic method invocation
 *
 * Provides cached reflection-based method calls for dynamic class loading
 * and method execution throughout the application.
 */
public class ReflectionUtil {

    private static final ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * Invoke an instance method using reflection
     */
    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        String cacheKey = target.getClass().getName() + "." + methodName;
        Method method = METHOD_CACHE.get(cacheKey);

        if (method == null) {
            method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            METHOD_CACHE.put(cacheKey, method);
        }

        return method.invoke(target, args);
    }

    /**
     * Invoke a static method
     */
    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Class<?> clazz = Class.forName(className);
        return invokeMethod(null, methodName, paramTypes, args);
    }

    /**
     * Create a new instance using reflection
     */
    public static Object newInstance(String className, Class<?>[] paramTypes, Object... args) throws Exception {
        Class<?> clazz = Class.forName(className);
        return clazz.getConstructor(paramTypes).newInstance(args);
    }

    /**
     * Load a class by name
     */
    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
