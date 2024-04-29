package xyz.wasabicodes.jaws.server.config.data;

class StringServerConfigDataType implements ServerConfigDataType<String> {

    @Override
    public Class<String> getTypeClass() {
        return String.class;
    }

    @Override
    public String parseStringNotNull(String string) throws IllegalArgumentException {
        return string;
    }

    @Override
    public String parseString(String string) throws IllegalArgumentException {
        return string;
    }

    @Override
    public String stringify(String value) throws NullPointerException {
        return value;
    }

}
