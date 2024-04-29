package xyz.wasabicodes.jaws.server.config.data;

import xyz.wasabicodes.jaws.util.ArgumentParser;

import java.util.Objects;

class BooleanServerConfigDataType implements ServerConfigDataType<Boolean> {

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    @Override
    public Boolean parseStringNotNull(String string) throws IllegalArgumentException {
        return switch (string.length()) {
            case 0 -> false;
            case 1 -> string.charAt(0) != '0';
            default -> !string.equalsIgnoreCase("false");
        };
    }

    @Override
    public Boolean parseArgument(ArgumentParser parser, String key) throws IllegalArgumentException {
        return parser.getFlag(key);
    }

    @Override
    public String stringify(Boolean value) throws NullPointerException {
        return Boolean.toString(Objects.requireNonNull(value));
    }

}
