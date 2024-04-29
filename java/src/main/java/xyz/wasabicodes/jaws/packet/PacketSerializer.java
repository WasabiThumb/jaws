package xyz.wasabicodes.jaws.packet;

import xyz.wasabicodes.jaws.packet.data.PacketData;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class PacketSerializer {

    private static boolean INIT = false;
    private static Map<PacketIdentifier, Class<? extends Packet>> REGISTRY;

    public static void init() {
        if (INIT) return;
        REGISTRY = buildRegistry();
        INIT = true;
    }

    public static byte[] serialize(Packet p, ByteOrder bo) {
        PacketData pd = PacketData.create(p.getElementCount());
        pd.byteOrder(bo);
        p.write(pd);

        byte[] ret = new byte[pd.size() + 1];
        ret[0] = p.id.toByte();
        pd.toBytes(ret, 1);
        return ret;
    }

    public static Packet deserialize(ByteBuffer payload, ByteOrder bo) throws IllegalArgumentException {
        final int size = payload.limit();
        if (size < 1) throw new IllegalArgumentException("Payload is empty");

        Packet p = constructByID(PacketIdentifier.from(payload.get(0)));

        PacketData pd = PacketData.of(payload.slice(1, size - 1));
        pd.byteOrder(bo);
        p.read(pd);
        return p;
    }

    //

    private static Packet constructByID(PacketIdentifier id) throws IllegalArgumentException {
        init();

        Class<? extends Packet> type = REGISTRY.get(id);
        if (type == null) throw new IllegalArgumentException("Unrecognized packet ID " + id);

        Constructor<? extends Packet> con;
        try {
            con = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Primary constructor lost for class " + type.getName(), e);
        }

        Packet p;
        try {
            p = con.newInstance();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Error in primary constructor for " + type.getSimpleName(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unknown reflection error while invoking primary constructor for " + type.getSimpleName(), e.getCause());
        }

        return p;
    }

    private static Map<PacketIdentifier, Class<? extends Packet>> buildRegistry() {
        Map<PacketIdentifier, Class<? extends Packet>> map = new HashMap<>();
        Set<Class<? extends Packet>> classes = findImpls();

        for (Class<? extends Packet> clazz : classes) {
            int mod = clazz.getModifiers();
            if (Modifier.isAbstract(mod) || (!Modifier.isPublic(mod))) continue;

            Constructor<? extends Packet> con;
            try {
                con = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException ignored) {
                continue;
            }

            Packet p;
            try {
                p = con.newInstance();
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                continue;
            }

            PacketIdentifier id = p.getIdentifier();
            if (map.containsKey(id)) System.out.println("WARN: ID (" + id + ") already assigned to " + map.get(id).getSimpleName() + " is overridden by " + clazz.getSimpleName());

            map.put(p.getIdentifier(), clazz);
        }

        return Collections.unmodifiableMap(map);
    }

    private static final char[] IMPL_EXT = new char[] { '/', 'i', 'm', 'p', 'l' };
    private static Set<Class<? extends Packet>> findImpls() {
        Set<Class<? extends Packet>> ret = new HashSet<>();

        String pkg = Packet.class.getPackageName();
        int pkgLen = pkg.length();
        char[] fullChars = new char[pkgLen + 5];
        char c;
        for (int i=0; i < pkgLen; i++) {
            c = pkg.charAt(i);
            if (c == '.') c = '/';
            fullChars[i] = c;
        }
        System.arraycopy(IMPL_EXT, 0, fullChars, pkgLen, 5);
        String path = new String(fullChars); // xyz/wasabicodes/jaws/packet/impl

        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("Package " + pkg + " not found in system class loader");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                Class<? extends Packet> impl;
                while ((line = br.readLine()) != null) {
                    if (!line.endsWith(".class")) continue;
                    impl = findImpl(pkg, line);
                    if (impl != null) ret.add(impl);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read package " + pkg + " from system class loader", e);
        }

        return Collections.unmodifiableSet(ret);
    }

    private static Class<? extends Packet> findImpl(String pkg, String classFile) {
        String name = pkg + ".impl." + classFile.substring(0, classFile.length() - 6);

        Class<?> clazz;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            System.out.println("WARN: Class " + name + " not found");
            return null;
        }

        if (!Packet.class.isAssignableFrom(clazz)) return null;
        return clazz.asSubclass(Packet.class);
    }

}
