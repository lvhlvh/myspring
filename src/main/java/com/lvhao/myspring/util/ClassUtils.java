package com.lvhao.myspring.util;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils {

    private static final String FILE_PREFIX = "file";
    
    private static final String PATH_SEPARATOR = "/";

    private static final String PACKAGE_SEPARATOR = ".";

    public static final String CLASS_FILE_SUFFIX = ".class";

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * 扫描指定包下的class文件，并对指定的类进行加载放入classSet返回
     *
     * @param packageName 指定包名
     * @return 指定包下的类的集合
     */
    public static Set<Class<?>> scanClasses(String packageName) {
        Set<Class<?>> classSet = new HashSet<>();
        // 1. 获取类加载器
        ClassLoader classLoader = getDefaultClassLoader();

        // 2. 通过类加载器获取到加载的资源信息
        URL url = classLoader.getResource(packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR));
        if (url == null) {
            return classSet;
        }

        // 3. 依据不同的资源类型，采用不同的方式获取资源的集合
        if (url.getProtocol().equalsIgnoreCase(FILE_PREFIX)) {
            File packageDir = new File(url.getPath());
            doScanClasses(classSet, packageDir, packageName);
        }

        return classSet;
    }

    private static void doScanClasses(Set<Class<?>> classSet, File file, String packageName) {
        if (file.isFile()) {
            if (file.getName().endsWith(CLASS_FILE_SUFFIX)) {
                // 1. 从file对象获取对应class的全限定名(binary name)
                String absolutePath = file.getAbsolutePath().replace(File.separator, ".");
                String classBinaryName = absolutePath.substring(absolutePath.indexOf(packageName), absolutePath.lastIndexOf("."));

                // 2. 将对应的Class对象放入classSet
                classSet.add(loadClass(classBinaryName));
            }
            return;
        }

        File[] files = file.listFiles(pathname -> file.isDirectory() || file.getName().endsWith(CLASS_FILE_SUFFIX));
        if (files != null) {
            for (File f : files) {
                doScanClasses(classSet, f, packageName);
            }
        }
    }

    private static Class<?> loadClass(String classBinaryName) {
        Class<?> ret = null;
        try {
            ret = Class.forName(classBinaryName);
        } catch (ClassNotFoundException e) {
            // do nothing
        }

        return ret;
    }

}
