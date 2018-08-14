package munch.restful.core;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomUtils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 6:23 PM
 * Project: restful-api
 */
public final class KeyUtils {
    private static final Pattern HTTP_PATTERN = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final Base64.Encoder ENCODER = java.util.Base64.getUrlEncoder().withoutPadding();

    /**
     * @return random uuid4 in String
     */
    public static String randomUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * @return MostSignificantBits = System.currentTimeMillis(), LeastSignificantBits = randomLong()
     */
    public static String randomMillisUUID() {
        return createUUID(System.currentTimeMillis(), RandomUtils.nextLong());
    }

    /**
     * 64bit Long x 2
     *
     * @param left  MostSignificantBits
     * @param right LeastSignificantBits
     * @return UUID
     */
    public static String createUUID(long left, long right) {
        return new UUID(left, right).toString();
    }

    /**
     * 32bit Integer x 4
     *
     * @param i1 integer 1
     * @param i2 integer 2
     * @param i3 integer 3
     * @param i4 integer 4
     * @return 128 bit UUID
     */
    public static String createUUID(int i1, int i2, int i3, int i4) {
        ByteBuffer buffer = ByteBuffer.allocate(16)
                .putInt(i1)
                .putInt(i2)
                .putInt(i3)
                .putInt(i4);

        buffer.position(0);

        long left = buffer.getLong();
        long right = buffer.getLong();
        return createUUID(left, right);
    }

    /**
     * @param bytes 16 length
     * @return 128 bit UUID
     */
    public static String createUUID(byte[] bytes) {
        if (bytes.length != 16) throw new IllegalArgumentException("bytes.length must be 16");

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(0);

        long left = buffer.getLong();
        long right = buffer.getLong();
        return createUUID(left, right);
    }

    /**
     * @return random uuid4 in String Base64 Url Safe
     */
    public static String randomUUIDBase64() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        return ENCODER.encodeToString(uuidBytes.array());
    }

    /**
     * @param text to sha 256
     * @return sha 256 in hex
     */
    public static String sha256(String text) {
        return DigestUtils.sha256Hex(text);
    }

    /**
     * @param text to sha 256
     * @return sha 256 in base 64
     */
    public static String sha256Base64(String text) {
        return ENCODER.encodeToString(DigestUtils.sha256(text));
    }

    /**
     * @param url to sha 256, with protocol trimmed
     * @return sha 256 in hex
     */
    public static String sha256Url(String url) {
        String trimmed = HTTP_PATTERN.matcher(url).replaceFirst("");
        return DigestUtils.sha256Hex(trimmed);
    }

    /**
     * @param url to sha 256, with protocol trimmed
     * @return sha 256 in base 64
     */
    public static String sha256Base64Url(String url) {
        String trimmed = HTTP_PATTERN.matcher(url).replaceFirst("");
        return ENCODER.encodeToString(DigestUtils.sha256(trimmed));
    }
}
