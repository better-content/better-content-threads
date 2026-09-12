package com.bettercontent.threads.compat;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import static org.junit.jupiter.api.Assertions.*;

/** Checks compiled injection contracts against the actual pinned native dependency bytecode.
 * Does not bootstrap optional mods, perform reflection, or simulate successful gameplay events. */
final class NativeHookContractTest {
    private final Map<String, ClassNode> classes = new HashMap<>();

    @Test void everyGameplayMixinTargetsAnExistingNativeBoundary() throws Exception {
        String classpath = System.getProperty("threads.optionalCompileClasspath");
        assertNotNull(classpath, "Native dependency classpath must be supplied by the test task");
        var config = JsonParser.parseString(Files.readString(Path.of("src/main/resources/better_content_threads.mixins.json"))).getAsJsonObject();
        for (var entry : config.getAsJsonArray("mixins")) {
            String name = "com/bettercontent/threads/mixin/" + entry.getAsString();
            ClassNode mixin = readOwn(name);
            AnnotationNode annotation = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).stream()
                .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
            List<String> targets = new ArrayList<>();
            Object named = value(annotation, "targets");
            if (named instanceof List<?> names) names.forEach(n -> targets.add(n.toString().replace('.', '/')));
            Object typed = value(annotation, "value");
            if (typed instanceof List<?> names) names.forEach(n -> targets.add(((Type)n).getInternalName()));
            assertFalse(targets.isEmpty(), name + " lacks native targets");
            for (String targetName : targets) {
                ClassNode target = readNative(targetName, classpath);
                for (var method : mixin.methods) for (var hook : annotations(method.visibleAnnotations, method.invisibleAnnotations)) {
                    if (!hook.desc.startsWith("Lorg/spongepowered/asm/mixin/injection/")) continue;
                    Object selectors = value(hook, "method");
                    if (!(selectors instanceof List<?> methods)) continue;
                    for (var selector : methods) {
                        String selected = selector.toString();
                        int descriptor = selected.indexOf('(');
                        String methodName = descriptor < 0 ? selected : selected.substring(0, descriptor);
                        String desc = descriptor < 0 ? null : selected.substring(descriptor);
                        List<MethodNode> matches = target.methods.stream().filter(m -> m.name.equals(methodName) && (desc == null || m.desc.equals(desc))).toList();
                        assertFalse(matches.isEmpty(), name + " missing target " + targetName + "." + selected);
                        for (var at : nested(value(hook, "at"))) {
                            Object anchor = value(at, "target");
                            if (!(anchor instanceof String instruction) || !instruction.startsWith("L") || !instruction.contains(";")) continue;
                            String owner = instruction.substring(1, instruction.indexOf(';'));
                            String member = instruction.substring(instruction.indexOf(';') + 1);
                            if (!member.contains("(")) continue;
                            String invokedName = member.substring(0, member.indexOf('('));
                            String invokedDesc = member.substring(member.indexOf('('));
                            boolean found = matches.stream().anyMatch(m -> {
                                for (var node : m.instructions) if (node instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(invokedName) && call.desc.equals(invokedDesc)) return true;
                                return false;
                            });
                            assertTrue(found, name + " missing invocation anchor " + instruction + " in " + selected);
                        }
                    }
                }
            }
        }
    }

    private ClassNode readNative(String name, String classpath) throws IOException {
        if (classes.containsKey(name)) return classes.get(name);
        String resource = name + ".class";
        for (String element : classpath.split(java.io.File.pathSeparator)) {
            Path path = Path.of(element);
            if (Files.isDirectory(path) && Files.isRegularFile(path.resolve(resource))) return cache(name, Files.readAllBytes(path.resolve(resource)));
            if (!element.endsWith(".jar") || !Files.isRegularFile(path)) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                var entry = jar.getJarEntry(resource);
                if (entry != null) try (InputStream input = jar.getInputStream(entry)) { return cache(name, input.readAllBytes()); }
            }
        }
        throw new AssertionError("Pinned native dependency does not supply " + name);
    }
    private ClassNode readOwn(String name) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(input, name); return cache(name, input.readAllBytes());
        }
    }
    private ClassNode cache(String name, byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0); classes.put(name, node); return node;
    }
    private static List<AnnotationNode> annotations(List<AnnotationNode> a, List<AnnotationNode> b) {
        var result = new ArrayList<AnnotationNode>(); if (a != null) result.addAll(a); if (b != null) result.addAll(b); return result;
    }
    private static Object value(AnnotationNode annotation, String key) {
        if (annotation.values != null) for (int i = 0; i < annotation.values.size(); i += 2)
            if (key.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
        return null;
    }
    private static List<AnnotationNode> nested(Object value) {
        if (value instanceof AnnotationNode one) return List.of(one);
        if (value instanceof List<?> list) return list.stream().filter(AnnotationNode.class::isInstance).map(AnnotationNode.class::cast).toList();
        return List.of();
    }
}
