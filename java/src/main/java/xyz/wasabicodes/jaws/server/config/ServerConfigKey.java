package xyz.wasabicodes.jaws.server.config;

import xyz.wasabicodes.jaws.compress.CompressMethod;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.server.config.data.ServerConfigDataType;
import xyz.wasabicodes.jaws.util.ArgumentParser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class ServerConfigKey<T> {

    public static final ServerConfigKey<Integer> PORT = integer("port", 0xB015);

    /**
     * Absolute max is 16384 (65536 bytes). This limitation is synthetic.
      */
    public static final ServerConfigKey<Integer> CHAT_MAX_MESSAGE_LENGTH = integer("chatMaxMessageLength", 260);

    public static final ServerConfigKey<Integer> CHAT_HISTORY_LENGTH = integer("chatHistoryLength", 100);

    public static final ServerConfigKey<Boolean> LOBBY_JOIN_MESSAGES = flag("lobbyJoinMessages", true);

    public static final ServerConfigKey<Boolean> LOBBY_LEAVE_MESSAGES = flag("lobbyLeaveMessages", true);

    public static final ServerConfigKey<EncryptMethod> ENCRYPTION = enumeration("encryption", EncryptMethod.class, EncryptMethod.NACL);

    public static final ServerConfigKey<CompressMethod> COMPRESSION = enumeration("compression", CompressMethod.class, CompressMethod.NONE);

    //

    static ServerConfigKey<Integer> integer(String key, int value) {
        return new ServerConfigKey<>(key, ServerConfigDataType.INTEGER, value);
    }

    static <T extends Enum<T>> ServerConfigKey<T> enumeration(String key, Class<T> clazz, T value) {
        return new ServerConfigKey<>(key, ServerConfigDataType.enumeration(clazz), value);
    }

    static ServerConfigKey<Boolean> flag(String key, boolean value) {
        return new ServerConfigKey<>(key, ServerConfigDataType.BOOLEAN, value);
    }

    public static ServerConfigKey<?>[] values() {
        Field[] fields = ServerConfigKey.class.getDeclaredFields();
        ServerConfigKey<?>[] ret = new ServerConfigKey[fields.length - 4];
        int head = 0;

        for (Field f : fields) {
            int mod = f.getModifiers();
            if (!Modifier.isStatic(mod)) continue;
            if (!Modifier.isPublic(mod)) continue;
            Class<?> fType = f.getType();
            if (ServerConfigKey.class.isAssignableFrom(fType)) {
                ServerConfigKey<?> c;
                try {
                    c = (ServerConfigKey<?>) f.get(null);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
                ret[head++] = c;
            }
        }

        if (head < ret.length) {
            ServerConfigKey<?>[] shrink = new ServerConfigKey[head];
            System.arraycopy(ret, 0, shrink, 0, head);
            ret = shrink;
        }
        return ret;
    }

    //

    private final String key;
    private final ServerConfigDataType<T> type;
    private final Object defaultValue;
    private final boolean nullable;
    ServerConfigKey(String key, ServerConfigDataType<T> type, Object defaultValue, boolean nullable) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.nullable = nullable;
    }

    ServerConfigKey(String key, ServerConfigDataType<T> type, Object defaultValue) {
        this(key, type, defaultValue, defaultValue == null);
    }

    ServerConfigKey(String key, ServerConfigDataType<T> type) {
        this(key, type, null, true);
    }

    //

    public final Class<T> getTypeClass() {
        return this.type.getTypeClass();
    }

    @Override
    public String toString() {
        return this.key;
    }

    public T getValue(Map<String, Object> map) {
        Object ret = map.get(this.key);
        if (ret == null) ret = this.defaultValue;
        if (!this.nullable && ret == null) throw new IllegalStateException("Non-nullable key \"" + this.key + "\" is null in config map");
        Class<T> typeClass = this.getTypeClass();
        if (!typeClass.isInstance(ret)) throw new IllegalStateException("Value assigned to \"" + this.key + "\" in config map is not of type " + typeClass.getName());
        return typeClass.cast(ret);
    }

    public void setValue(Map<String, Object> map, Object value) throws NullPointerException {
        if (value == null) {
            if (!this.nullable) throw new NullPointerException("Attempt to set value as null for non-nullable key \"" + this.key + "\"");
            map.remove(this.key);
        } else {
            Class<T> typeClass = this.getTypeClass();
            if (!typeClass.isInstance(value)) throw new IllegalArgumentException("Attempt to set value of type " + value.getClass().getName() + " for key \"" + this.key + "\" of type " + typeClass.getName());
            map.put(this.key, value);
        }
    }

    public T parseValue(ArgumentParser args) {
        try {
            return this.type.parseArgument(args, this.key);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid value for command-line switch: " + this.key, e);
        }
    }

}
