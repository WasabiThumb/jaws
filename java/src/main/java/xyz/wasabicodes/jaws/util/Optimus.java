package xyz.wasabicodes.jaws.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID obfuscation based on Knuth's multiplicative hashing method. Inspired by
 * <a href="https://github.com/jenssegers/optimus">jenssegers/optimus</a>.
 * Partially based on
 * <a href="https://github.com/jadrio/optimus-java/blob/master/src/main/java/com/joseangeldiazruiz/optimus/Optimus.java">
 *     this implementation by jadrio
 * </a> with changes to allow for custom size and serialization, and without a primality test. When using the constructor,
 * you accept that a non-prime will create garbage output. Otherwise, see {@link #generate()}.
 */
public class Optimus {

    /**
     * Generates an Optimus instance with the default random generator and a size of 31 (the maximum).
     * @see #generate(int)
     */
    public static Optimus generate() {
        return generate(ThreadLocalRandom.current(), 31);
    }

    /**
     * Generates an optimus instance with the specified random generator and a size of 31 (the maximum).
     * @param r Random generator
     * @see #generate(Random, int)
     */
    public static Optimus generate(Random r) {
        return generate(r, 31);
    }

    /**
     * Generates an optimus instance with the default random generator and the specified size.
     * @param size The size (bit length) from 3 to 31
     * @see #generate(Random, int)
     */
    public static Optimus generate(int size) {
        return generate(ThreadLocalRandom.current(), size);
    }

    /**
     * Generates an optimus instance with the specified random generator and the specified size.
     * @param r Random generator
     * @param size The size (bit length) from 3 to 31
     */
    public static Optimus generate(Random r, int size) {
        int max = getMax(size);
        int prime = BigInteger.probablePrime(size, r).intValue();
        int random = r.nextInt() & max;
        return new Optimus(prime, modInverse(prime, size), random, max);
    }

    private static int modInverse(int n, int size) {
        return BigInteger.valueOf(n)
                .modInverse(BigInteger.ONE.shiftLeft(size))
                .intValue();
    }

    private static int getMax(int size) {
        if (size < 3) throw new IllegalArgumentException("Optimus size (" + size + ") cannot be less than 3");
        if (size > 31) throw new IllegalArgumentException("Optimus size greater than 31 (" + size + ") not supported");
        return size == 31 ? Integer.MAX_VALUE : ((1 << size) - 1);
    }

    //

    private final long prime;
    private final long modInverse;
    private final int random;
    private final long max;
    protected Optimus(int prime, int modInverse, int random, int max) {
        this.prime = prime;
        this.modInverse = modInverse;
        this.random = random;
        this.max = max;
    }

    /**
     * Creates an optimus instance
     * @param prime A prime number (non-primes will create garbage) less than 2 ^ size
     * @param random A random number less than 2 ^ size
     * @param size The size (bit length) from 3 to 31
     */
    public Optimus(int prime, int random, int size) {
        this(prime, modInverse(prime, size), random, getMax(size));
    }

    /**
     * Creates an optimus instance
     * @param prime A prime number (non-primes will create garbage) less than 2 ^ 31
     * @param random A random number less than 2 ^ 31
     * @see Optimus#Optimus(int, int, int)
     */
    public Optimus(int prime, int random) {
        this(prime, modInverse(prime, 31), random, 31);
    }

    /**
     * Gets the prime number stored in this instance
     */
    public final int prime() {
        return (int) this.prime;
    }

    /**
     * Gets the mod inverse stored in this instance. The mod inverse is defined as
     * <pre>{@code BigInteger.valueOf(prime).modInverse(BigInteger.ONE.shiftLeft(size)).intValue() }</pre>
     * @see BigInteger#modInverse(BigInteger)
     */
    public final int modInverse() {
        return (int) this.modInverse;
    }

    /**
     * Gets the random number stored in this instance
     */
    public final int random() {
        return this.random;
    }

    /**
     * Gets the maximum number stored in this instance. The max is defined as
     * <pre>{@code size == 31 ? Integer.MAX_VALUE : ((1 << size) - 1) }</pre>
     */
    public final int max() {
        return (int) this.max;
    }

    /**
     * Obfuscates an ID. The formula for obfuscation is defined as:
     * <pre>{@code ((int) ((((long) n) * this.prime) & this.max)) ^ this.random}</pre>
     * @param n The ID to obfuscate
     * @return The obfuscated ID
     */
    public int encode(int n) {
        return ((int) ((((long) n) * this.prime) & this.max)) ^ this.random;
    }

    /**
     * Deobfuscates an obfuscated ID. The formula for deobfuscation is defined as:
     * <pre>{@code (int) ((((long) (n ^ this.random)) * this.modInverse) & this.max)}</pre>
     * @param n The obfuscated ID to deobfuscate
     * @return The deobfuscated ID
     */
    public int decode(int n) {
        return (int) ((((long) (n ^ this.random)) * this.modInverse) & this.max);
    }

    /**
     * Serializes this instance into a 3, 5 or 9-long byte sequence. The sequence will be as short as
     * possible given the size of this instance.
     * @param order The byte order to use
     */
    public byte[] serialize(ByteOrder order) {
        int size = 64 - Long.numberOfLeadingZeros(this.max);
        int octets = ((size - 1) >> 3) + 1;

        final byte[] arr = new byte[(octets << 1) | 1]; // octets * 2 + 1;
        ByteBuffer buf = ByteBuffer.wrap(arr);
        buf.order(order);
        if (order == ByteOrder.BIG_ENDIAN) size |= 0x20;
        buf.position(1);
        switch (octets) {
            case 1:
                buf.put((byte) this.prime);
                buf.put((byte) this.random);
                break;
            case 2:
                buf.putShort((short) this.prime);
                buf.putShort((short) this.random);
                break;
            case 3:
                if (order == ByteOrder.BIG_ENDIAN) {
                    buf.putInt(3, this.random());
                    buf.putInt(0, this.prime());
                } else {
                    buf.putInt(0, this.prime());
                    buf.putInt(3, this.random());
                    //noinspection SuspiciousSystemArraycopy
                    System.arraycopy(arr, 0, arr, 1, 6);
                }
                break;
            case 4:
                buf.putInt(this.prime());
                buf.putInt(this.random());
                break;
        }
        arr[0] = (byte) size;

        return arr;
    }

    /**
     * Alias for {@code serialize(ByteOrder.BIG_ENDIAN)}
     * @see #serialize(ByteOrder) 
     */
    public byte[] serialize() {
        return this.serialize(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Deserializes an Optimus instance from a 3 to 9-long byte sequence. The endianness to use is determined from
     * the sequence.
     * @see #serialize()
     * @throws IllegalArgumentException The sequence is invalid (wrong length, out of bounds). Reserved bits (0-1) are not checked.
     */
    public static Optimus deserialize(byte[] bytes) throws IllegalArgumentException {
        if (bytes.length < 3) throw new IllegalArgumentException("Data must be at least 3 bytes (received " + bytes.length + ")");

        final byte header = bytes[0];
        final int size = header & 0x1F;
        if (size < 3) throw new IllegalArgumentException("Invalid header (" + Byte.toUnsignedInt(header) + ")");

        final ByteOrder order = ((header & 0x20) == 0) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        final int octets = ((size - 1) >> 3) + 1;

        final int requiredLength = (octets << 1) | 1;
        if (bytes.length < requiredLength) {
            throw new IllegalArgumentException("Invalid header (" + Byte.toUnsignedInt(header) + ") for length " + bytes.length);
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(order);
        buf.position(1);

        int prime;
        int random;
        switch (octets) {
            case 1:
                prime = Byte.toUnsignedInt(buf.get());
                random = Byte.toUnsignedInt(buf.get());
                break;
            case 2:
                prime = Short.toUnsignedInt(buf.getShort());
                random = Short.toUnsignedInt(buf.getShort());
                break;
            case 3:
                prime = buf.getInt(0);
                random = buf.getInt(3);
                if (order == ByteOrder.BIG_ENDIAN) {
                    prime &= 0xFFFFFF;
                    random &= 0xFFFFFF;
                } else {
                    prime >>>= 8;
                    random >>>= 8;
                }
                break;
            case 4:
                prime = buf.getInt();
                random = buf.getInt();
                break;
            default:
                throw new IllegalStateException("Computed number of octets (" + octets + ") cannot be handled");
        }

        return new Optimus(prime, modInverse(prime, size), random, getMax(size));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new long[] { this.prime, this.modInverse, (long) this.random, this.max });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Optimus other) {
            if ((this.prime == other.prime) &&
                    (this.modInverse == other.modInverse) &&
                    (this.random == other.random) &&
                    (this.max == other.max)) return true;
        }
        return super.equals(obj);
    }

}
