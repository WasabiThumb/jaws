package xyz.wasabicodes.jaws.server.config.data;

import xyz.wasabicodes.jaws.util.ArgumentParser;

public interface ServerConfigDataType<T> {

    ServerConfigDataType<Integer> INTEGER = new IntegerServerConfigDataType();

    ServerConfigDataType<String> STRING = new StringServerConfigDataType();

    ServerConfigDataType<Boolean> BOOLEAN = new BooleanServerConfigDataType();

    static <T extends Enum<T>> ServerConfigDataType<T> enumeration(Class<T> clazz) {
        return new EnumServerConfigDataType<>(clazz);
    }

    //

    Class<T> getTypeClass();

    T parseStringNotNull(String string) throws IllegalArgumentException;

    default T parseString(String string) throws IllegalArgumentException {
        if (string == null || string.equalsIgnoreCase("null")) return null;
        return this.parseStringNotNull(string);
    }

    default T parseArgument(ArgumentParser parser, String key) throws IllegalArgumentException {
        return this.parseString(parser.get(key));
    }

    String stringify(T value) throws NullPointerException;

}
