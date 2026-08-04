package dev.wycobar.hegmark.planet;

import dev.wycobar.hegmark.feature.ResolutionRange;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public enum Layer {

    CONTINENT(0),
    SUBCONTINENT(1),
    COUNTRY(2),
    PROVINCE(3),
    METROPOLITAN(4),
    CITY(5),
    TOWN(6),
    NEIGHBOURHOOD(7),
    FARM(8),
    ARENA(9),
    SPORTS_PITCH(10),
    CAR_PARK(11),
    OFFICE_BLOCK(12),
    HOUSE(13),
    ROOM(14),
    CUPBOARD(15);


    private final int value;

    Layer(int value) {
        this.value = value;
    }

    public int intValue() {
        return this.value;
    }

    public static Layer smallerOf(Layer... layers) {
        return Stream.of(layers).min(Comparator.comparing(Layer::intValue)).orElseThrow(IllegalArgumentException::new);
    }

    public static Layer biggerOf(Layer... layers) {
        return Stream.of(layers).max(Comparator.comparing(Layer::intValue)).orElseThrow(IllegalArgumentException::new);
    }

    public static ResolutionRange anywhere() {
        return new ResolutionRange(CONTINENT, CUPBOARD);
    }

    public static Layer valueOf(int value) {
        return Arrays.stream(values()).filter(l -> l.intValue() == value).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
