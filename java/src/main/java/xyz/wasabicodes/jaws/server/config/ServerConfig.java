package xyz.wasabicodes.jaws.server.config;

import xyz.wasabicodes.jaws.util.ArgumentParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServerConfig {

    public static Builder builder() {
        return new Builder();
    }

    //

    private final Map<String, Object> map;

    ServerConfig(Builder builder) {
        this.map = Collections.unmodifiableMap(builder.map);
    }

    public ServerConfig() {
        this.map = Collections.emptyMap();
    }

    //

    public <T> T get(ServerConfigKey<T> key) {
        return key.getValue(this.map);
    }

    public <T> T getObject(ServerConfigKey<T> key) throws NullPointerException {
        return Objects.requireNonNull(key.getValue(this.map));
    }

    public int getInt(ServerConfigKey<Integer> key) {
        return (int) this.getObject(key);
    }

    public float getFloat(ServerConfigKey<Float> key) {
        return (float) this.getObject(key);
    }

    public double getDouble(ServerConfigKey<Double> key) {
        return (double) this.getObject(key);
    }

    public long getLong(ServerConfigKey<Long> key) {
        return (long) this.getObject(key);
    }

    public short getShort(ServerConfigKey<Short> key) {
        return (short) this.getObject(key);
    }

    public byte getByte(ServerConfigKey<Byte> key) {
        return (byte) this.getObject(key);
    }

    public char getChar(ServerConfigKey<Character> key) {
        return (char) this.getObject(key);
    }

    public boolean getBoolean(ServerConfigKey<Boolean> key) {
        return (boolean) this.getObject(key);
    }

    //

    public static class Builder {

        private final Map<String, Object> map;
        Builder() {
            this.map = new HashMap<>();
        }

        public Builder args(String[] args) {
            return this.args(new ArgumentParser(args));
        }

        public Builder args(ArgumentParser parser) {
            for (ServerConfigKey<?> key : ServerConfigKey.values()) this.args0(parser, key);
            return this;
        }

        private <T> void args0(ArgumentParser parser, ServerConfigKey<T> key) {
            key.setValue(this.map, key.parseValue(parser));
        }

        public <T> Builder set(ServerConfigKey<T> key, T value) throws NullPointerException {
            key.setValue(this.map, value);
            return this;
        }

        public Builder clear() {
            return this;
        }

        public ServerConfig build() {
            return new ServerConfig(this);
        }

    }

}
