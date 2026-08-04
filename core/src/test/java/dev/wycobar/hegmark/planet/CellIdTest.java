package dev.wycobar.hegmark.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellIdTest {
    @Test
    void roundTripsUnsignedHexAddresses() {
        CellId cell = new CellId(0x8a2a10728907fffL);
        assertEquals(cell, CellId.fromHexString(cell.asHexString()));
    }
}
