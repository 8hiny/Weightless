package shiny.weightless.client.trail;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Trail {

    public final int length;
    private List<TrailPoint> trailPoints = new ArrayList<>();
    private List<Vec3d> vertices = new ArrayList<>();

    public Trail(int length) {
        this.length = length;
    }

    public void tick() {
        this.trailPoints.forEach(TrailPoint::tick);
        this.trailPoints.removeIf(point -> point.getAge() > length || point.finished());
    }

    public void addPoint(Vec3d pos) {
        this.trailPoints.add(new TrailPoint(pos));
    }

    public void addPoint(Vec3d pos, int maxAge) {
        this.trailPoints.add(new TrailPoint(pos, maxAge));
    }

    public List<TrailPoint> getTrailPoints() {
        return this.trailPoints;
    }

    public List<Vec3d> getVertices() {
        return this.vertices;
    }
}
