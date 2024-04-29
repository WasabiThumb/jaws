package xyz.wasabicodes.jaws.server.config.data;

import java.util.Objects;

class IntegerServerConfigDataType implements ServerConfigDataType<Integer> {

    @Override
    public Class<Integer> getTypeClass() {
        return Integer.class;
    }

    @Override
    public Integer parseStringNotNull(String string) throws IllegalArgumentException {
        return Integer.parseInt(string);
    }

    @Override
    public String stringify(Integer value) throws NullPointerException {
        return Integer.toString(Objects.requireNonNull(value));
    }

}
