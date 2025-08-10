package net.astellismodding.golfwithmates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PutterPower(double level) {
    public static final double MAX_POWER = 1;
    public static final PutterPower DEFAULT = new PutterPower(0);

    public PutterPower cycle(int direction) {
        double newLevel = 0;
        double inc = 0.05;
        if (direction == 1){
            newLevel = (this.level + inc) % (MAX_POWER + inc);
        }else if ( direction == -1 ) {
            newLevel = (this.level - inc + MAX_POWER) % MAX_POWER;
        }
        return new PutterPower(newLevel);
    }

    public double value(){
        return level;
    }

    // Basic codec
    public static final Codec<PutterPower> BASIC_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("level").forGetter(PutterPower::level)
            ).apply(instance, PutterPower::new)
    );


}
