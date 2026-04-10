package net.astellismodding.golfwithmates;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    // -------------------------------------------------------------------------
    // Server config — physics values, consistent across all players on a server
    // -------------------------------------------------------------------------
    public static class Server {
        private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.DoubleValue GRAVITY = BUILDER
                .comment("Gravity applied per simulation step. Lower = floatier shots. Default: 0.08")
                .defineInRange("gravity", 0.08, 0.01, 1.0);

        public static final ModConfigSpec.DoubleValue STOP_THRESHOLD = BUILDER
                .comment("Ball stops when speed drops below this value. Default: 0.05")
                .defineInRange("stopThreshold", 0.05, 0.001, 1.0);

        public static final ModConfigSpec.IntValue MAX_BOUNCES = BUILDER
                .comment("Maximum bounces before the ball is forced to rest. Default: 16")
                .defineInRange("maxBounces", 16, 1, 64);

        public static final ModConfigSpec.IntValue MAX_ITERATIONS = BUILDER
                .comment("Hard cap on simulation iterations per shot. Raise if long shots cut off early. Default: 10000")
                .defineInRange("maxIterations", 10000, 1000, 100000);

        public static final ModConfigSpec SPEC = BUILDER.build();
    }

    // -------------------------------------------------------------------------
    // Client config — per-player preferences
    // -------------------------------------------------------------------------
    public static class Client {
        private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.BooleanValue SHOW_BEAM_BY_DEFAULT = BUILDER
                .comment("Show the trajectory beam when a ball lands. Can be toggled any time with /golf togglebeam. Default: false")
                .define("showBeamByDefault", false);

        public static final ModConfigSpec SPEC = BUILDER.build();
    }
}
