package shiny.weightless.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import shiny.weightless.Weightless;
import shiny.weightless.client.trail.Trail;
import shiny.weightless.client.trail.TrailPoint;
import shiny.weightless.common.component.WeightlessComponent;

import java.util.List;

public class TrailRenderer {

    public static Identifier TEXTURE = Weightless.id("textures/trail.png");

    //For the current trail point:
    //Get the point's position
    //Get the next point's position

    //Get the rotation vector from the camera entity
    //Get the cross product of the camera rotation and the direction from the point to the next (normal vector)
    //Position a vertex at the pos + the normal vector times half the width and at the pos - the normal vector times half the width (these are the first two vertices)

    public static void render(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider provider, PlayerEntity player, float width, int light) {
        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE, false));
        Trail trail = WeightlessComponent.get(player).getTrail();
        List<TrailPoint> trailPoints = trail.getTrailPoints();

        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Vec3d camRot = getRotationVector(camera.getPitch(), camera.getYaw());

        if (trailPoints.size() >= 2) {
            matrices.push();

            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            Vec3d last = trailPoints.get(0).getPos();
            for (int i = 1; i < trailPoints.size(); i++) {
                Vec3d point = trailPoints.get(i).getPos();

                Vec3d dir = last.subtract(point).normalize();
                Vec3d normal = camPos.subtract(point).crossProduct(dir).normalize();

                Vec3d dir1 = point.subtract(last).normalize();
                Vec3d normal1 = camPos.subtract(last).crossProduct(dir1).normalize();

                Vec3d a = last.add(normal);
                Vec3d b = last.subtract(normal);
                Vec3d c = point.add(normal);
                Vec3d d = point.subtract(normal);

                //Actual rendering
                vertex(matrices, vertexConsumer, d, 0, 1, light);
                vertex(matrices, vertexConsumer, c, 0, 0, light);
                vertex(matrices, vertexConsumer, b, 1, 1, light);
                vertex(matrices, vertexConsumer, a, 1, 0, light);
            }
            matrices.pop();
        }
    }

    public static void vertex(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d pos, int u, int v, int light) {
        MatrixStack.Entry entry = matrices.peek();
        vertexConsumer.vertex(entry.getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry.getNormalMatrix(), 1, 0, 0)
                .next();
    }

    public static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
}
