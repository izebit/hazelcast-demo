package ru.izebit.client.serialization;

import org.apache.commons.lang3.reflect.FieldUtils;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Collections.singletonList;

/**
 * @author <a href="mailto:a.konovalov@fasten.com">Artem Konovalov</a> <br/>
 *         Creation date: 7/14/17.
 * @since 1.0
 */
public class DynamicUtils {

    public static ClassLoader getClassLoader(String className, String code) throws Exception {
        return getClassLoader(compile(className, code));
    }

    private static String compile(String className, String code) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Path output = Files.createTempDirectory("_" + System.currentTimeMillis());

        JavaCompiler.CompilationTask task = compiler
                .getTask(null,
                        fileManager,
                        null,
                        Arrays.asList("-d", output.toAbsolutePath().toString()),
                        null,
                        singletonList(new JavaSourceFromString(className, code))
                );

        boolean result = task.call();
        if (!result)
            throw new IllegalStateException("something wrong happened");

        return output.toAbsolutePath().toString();
    }

    private static ClassLoader getClassLoader(String classPath) throws Exception {
        return new URLClassLoader(
                new URL[]{Paths
                        .get(classPath)
                        .toUri()
                        .toURL()
                },
                ClassLoader.getSystemClassLoader()
        );
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        private JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    public static String getStringFrom(Object object) throws Exception {
        StringBuilder string = new StringBuilder()
                .append(object.getClass().getSimpleName())
                .append(" : \n");

        for (Field field : FieldUtils.getAllFields(object.getClass()))
            string.append(field.getName()).append("=").append(FieldUtils.readField(field, object, true)).append("\n");

        return string.toString();
    }
}

