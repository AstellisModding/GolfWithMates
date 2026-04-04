package net.astellismodding.golfwithmates.util;

public enum ShotType {
    PUTTER,   // flat XZ, full block rebound, high friction
    IRON,     // shallow parabola (~15-25 degrees), moderate distance
    WEDGE,    // steep parabola (~45 degrees), short, high friction on land
    DRIVER    // long flat-ish parabola (~10-15 degrees), low friction on land
}