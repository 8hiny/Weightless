package shiny.weightless.client.trail;

import net.minecraft.util.math.Vec3d;

public class TrailPoint {

    private final Vec3d pos;
    private int age;
    private final int maxAge;

    public TrailPoint(Vec3d pos) {
        this.pos = pos;
        this.maxAge = 20;
    }

    public TrailPoint(Vec3d pos, int maxAge) {
        this.pos = pos;
        this.maxAge = maxAge;
    }

    public void tick() {
        this.age++;
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public int getAge() {
        return this.age;
    }

    public boolean finished() {
        return this.age >= this.maxAge;
    }
}
