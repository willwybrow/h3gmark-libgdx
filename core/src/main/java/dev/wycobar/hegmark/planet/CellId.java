package dev.wycobar.hegmark.planet;

/** Project-owned spatial address; its representation is deliberately opaque outside grid adapters. */
public record CellId(long value) {
    public String asHexString() {
        return Long.toHexString(value);
    }

    public static CellId fromHexString(String value) {
        return new CellId(Long.parseUnsignedLong(value, 16));
    }
}
