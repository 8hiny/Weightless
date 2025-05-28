package shiny.weightless.client.trail;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import shiny.weightless.client.render.ModRenderLayers;
import shiny.weightless.common.component.WeightlessComponent;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class TrailRenderer {

    /**
     * Renders a trail from a set of points, usually behind an entity.
     * @param trail The set of points to be drawn
     * @param color The color of the trail
     * @param widthModifier The modifier which should be applied to the trail's width. Function value is normalized
     * @param alphaModifier The modifier which should be applied to the trail's opacity. Function value is normalized
     */

    public static void render(MatrixStack matrices, VertexConsumerProvider provider, Trail trail, Color color, Function<Float, Float> widthModifier, Function<Float, Float> alphaModifier) {
        List<TrailPoint> points = trail.getTrailPoints();

        if (points.size() >= 2) {
            VertexConsumer vertexConsumer = provider.getBuffer(ModRenderLayers.getTrail());

            MinecraftClient client = MinecraftClient.getInstance();
            Camera camera = client.gameRenderer.getCamera();
            Vec3d camPos = camera.getPos();
            Vector3f camRot = new Vector3f(0, 0, -1).rotate(camera.getRotation());

            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            Vec3d prevC = Vec3d.ZERO;
            Vec3d prevD = Vec3d.ZERO;

            int count = points.size() - 1;
            float maxDistance = (float) points.get(0).getPos().squaredDistanceTo(points.get(count).getPos());
            for (int i = 0; i < count; i++) {
                Vec3d point = points.get(i).getPos();
                Vec3d next = points.get(i + 1).getPos();

                Vec3d dir = next.subtract(point).normalize();
                Vec3d normal = dir.crossProduct(new Vec3d(camRot)).normalize();

                float width;
                float alpha = 1.0f;
                float nextAlpha = 1.0f;

                //Generate two vertices for each stored point
                Vec3d[] vertices = new Vec3d[4];
                for (int j = 0; j < 4; j++) {
                    Vec3d vertex = j < 2 ? point : next;

                    float step = (float) vertex.squaredDistanceTo(points.get(count).getPos()) / maxDistance;
                    width = widthModifier.apply(step);

                    if (j == 0) alpha = alphaModifier.apply(step);
                    else if (j == 3) nextAlpha = alphaModifier.apply(step);

                    if (j % 2 == 0) vertex = vertex.add(normal.multiply(width));
                    else vertex = vertex.subtract(normal.multiply(width));

                    vertices[j] = vertex;
                }

                if (i > 0) {
                    vertex(matrices, vertexConsumer, prevC, color, alpha);
                    vertex(matrices, vertexConsumer, prevD, color, alpha);
                }
                else {
                    vertex(matrices, vertexConsumer, vertices[0], color, alpha);
                    vertex(matrices, vertexConsumer, vertices[1], color, alpha);
                }
                vertex(matrices, vertexConsumer, vertices[3], color, nextAlpha);
                vertex(matrices, vertexConsumer, vertices[2], color, nextAlpha);

                prevC = vertices[2];
                prevD = vertices[3];
            }
            matrices.pop();
        }
    }

    public static void render(Entity entity, MatrixStack matrices, VertexConsumerProvider provider, Trail trail, float width, float alpha) {
        Color color = new Color(1.0f, 1.0f, 1.0f);
        if (entity instanceof PlayerEntity player) {
            color = WeightlessComponent.get(player).getTrailColor();
        }
        render(matrices, provider, trail, color, f -> MathHelper.lerp(f, width, 0), f -> alpha * f);
    }

    public static void vertex(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d pos, Color color, float alpha) {
        MatrixStack.Entry entry = matrices.peek();
        vertexConsumer.vertex(entry.getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, alpha)
                .next();
    }
}
