package shiny.weightless.client.trail;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Trail {

    private final List<TrailPoint> trailPoints = new ArrayList<>();
    public final int length;

    public Trail(int length) {
        this.length = length;
    }

    public void tick() {
        this.trailPoints.forEach(TrailPoint::tick);
        this.trailPoints.removeIf(point -> point.getAge() > length || point.finished());
    }

    public void addPoint(TrailPoint point) {
        if (this.trailPoints.size() == this.length) {
            this.trailPoints.remove(this.trailPoints.size() - 1);
        }
        this.trailPoints.add(point);
    }

    public void addPoint(Vec3d pos) {
        this.addPoint(new TrailPoint(pos));
    }

    public void addPoint(Vec3d pos, int maxAge) {
        this.addPoint(new TrailPoint(pos, maxAge));
    }

    public void addPoint(Entity entity) {
        Vec3d pos = entity.getPos().add(0, entity.getHeight() * 0.5, 0);
        addPoint(pos);
    }

    public void addLerpedPoint(Entity entity, float tickDelta) {
        Vec3d pos = entity.getLerpedPos(tickDelta).add(0, entity.getHeight() * 0.5, 0);
        addPoint(pos);
    }

    public List<TrailPoint> getTrailPoints() {
        return this.trailPoints;
    }

    public TrailPoint getLast() {
        if (!this.trailPoints.isEmpty()) return this.trailPoints.get(this.trailPoints.size() - 1);
        return null;
    }

    public boolean isEmpty() {
        return this.trailPoints.isEmpty();
    }
}
