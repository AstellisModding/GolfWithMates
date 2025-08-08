package net.astellismodding.golfwithmates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PutterPower(int level) {
    public static final int MAX_POWER = 10;
    public static final PutterPower DEFAULT = new PutterPower(0);

    public PutterPower cycle(int direction) {
        int newLevel = 0;
        if (direction == 1){
            newLevel = (this.level + 1) % (MAX_POWER + 1);
        }else if ( direction == 0 ) {
            newLevel = (this.level - 1 + MAX_POWER) % MAX_POWER;
        }
        return new PutterPower(newLevel);
    }

    public int value(){
        return level;
    }

    // Basic codec
    public static final Codec<PutterPower> BASIC_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(PutterPower::level)
            ).apply(instance, PutterPower::new)
    );


}
