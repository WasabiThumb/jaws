package xyz.wasabicodes.jaws.server.config.data;

import java.util.Objects;

class EnumServerConfigDataType<T extends Enum<T>> implements ServerConfigDataType<T> {

    private final Class<T> clazz;
    EnumServerConfigDataType(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> getTypeClass() {
        return this.clazz;
    }

    @Override
    public T parseStringNotNull(String string) throws IllegalArgumentException {
        IllegalArgumentException except;
        try {
            return Enum.valueOf(this.clazz, string);
        } catch (IllegalArgumentException e) {
            except = e;
        }
        for (T candidate : this.clazz.getEnumConstants()) {
            if (candidate.name().equalsIgnoreCase(string)) return candidate;
        }
        throw except;
    }

    @Override
    public String stringify(T value) throws NullPointerException {
        return Objects.requireNonNull(value).name();
    }

}
