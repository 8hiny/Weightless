package shiny.weightless.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import shiny.weightless.client.trail.Trail;
import shiny.weightless.client.trail.TrailPoint;
import shiny.weightless.common.component.WeightlessComponent;

import java.awt.*;
import java.util.List;

public class TrailRenderer {

    /**
     * Draws a triangular trail, usually behind an entity.
     * @param trail The trail to be rendered
     * @param color The color which is interpolated to at the end of the trail
     * @param size The size of the trail
     * @param alpha The maximum opacity of the trail
     */
    public static void render(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider provider, Trail trail, Color color, float size, int alpha) {
        VertexConsumer vertexConsumer = provider.getBuffer(ModRenderLayers.getTrail());
        List<TrailPoint> trailPoints = trail.getTrailPoints();

        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        if (trailPoints.size() >= 2) {
            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            Vec3d prevC = Vec3d.ZERO;
            Vec3d prevD = Vec3d.ZERO;

            //Split the trail up into seperate quads, which are each split into four triangles
            int count = trailPoints.size() - 1;
            for (int i = 0; i < count; i++) {
                Vec3d point = trailPoints.get(i).getPos();
                Vec3d next = trailPoints.get(i + 1).getPos();
                Vec3d between = point.lerp(next, 0.5);

                Vec3d dir = next.subtract(point).normalize();
                Vec3d normal = camPos.subtract(point).crossProduct(dir).normalize();

                Vec3d[] vertices = new Vec3d[4];
                int thisAlpha = alpha;
                int nextAlpha = alpha;
                Color thisColor = color;
                Color nextColor = color;

                //Generate two points for each stored point & the equivalent alpha and color values
                float maxDistance = (float) trailPoints.get(0).getPos().squaredDistanceTo(trailPoints.get(count).getPos());
                for (int j = 0; j < 4; j++) {
                    Vec3d vertex = j < 2 ? point : next;

                    float step = (float) vertex.squaredDistanceTo(trailPoints.get(count).getPos()) / maxDistance;
                    float width = size * (1.0f - step);

                    if (j % 2 == 0) vertex = vertex.add(normal.multiply(width));
                    else vertex = vertex.subtract(normal.multiply(width));

                    int alpha1 = (int) (alpha * step);
                    Color color1 = new Color(
                            MathHelper.clamp(MathHelper.lerp(step, 255, color.getRed()), 0, 255),
                            MathHelper.clamp(MathHelper.lerp(step, 255, color.getGreen()), 0, 255),
                            MathHelper.clamp(MathHelper.lerp(step, 255, color.getBlue()), 0, 255)
                    );
                    if (j == 0) {
                        thisAlpha = alpha1;
                        thisColor = color1;
                    }
                    else if (j == 3) {
                        nextAlpha = alpha1;
                        nextColor = color1;
                    }

                    vertices[j] = vertex;
                }

                //Gotta revisit this in the future to reduce the vertex count by using triangle fans
                //If not on the first step, use previous back edge vertices to fill in gaps

                //Transition color and alpha values for the center of the drawn quad
                int betweenAlpha = MathHelper.lerp(0.5f, thisAlpha, nextAlpha);
                Color betweenColor = new Color(
                        MathHelper.clamp(MathHelper.lerp(0.5f, thisColor.getRed(), nextColor.getRed()), 0, 255),
                        MathHelper.clamp(MathHelper.lerp(0.5f, thisColor.getGreen(), nextColor.getGreen()), 0, 255),
                        MathHelper.clamp(MathHelper.lerp(0.5f, thisColor.getBlue(), nextColor.getBlue()), 0, 255)
                );

                //Draw first triangle
                if (i > 0) vertex(matrices, vertexConsumer, prevC, thisColor, thisAlpha);
                else vertex(matrices, vertexConsumer, vertices[0], thisColor, thisAlpha);
                vertex(matrices, vertexConsumer, vertices[2], nextColor, nextAlpha);
                vertex(matrices, vertexConsumer, between, betweenColor, betweenAlpha);

                //Draw second triangle
                vertex(matrices, vertexConsumer, vertices[2], nextColor, nextAlpha);
                vertex(matrices, vertexConsumer, vertices[3], nextColor, nextAlpha);
                vertex(matrices, vertexConsumer, between, betweenColor, betweenAlpha);

                //Draw third triangle
                if (i > 0) vertex(matrices, vertexConsumer, prevD, thisColor, thisAlpha);
                else vertex(matrices, vertexConsumer, vertices[1], thisColor, thisAlpha);
                vertex(matrices, vertexConsumer, vertices[3], nextColor, nextAlpha);
                vertex(matrices, vertexConsumer, between, betweenColor, betweenAlpha);

                //Draw fourth triangle
                if (i > 0) {
                    vertex(matrices, vertexConsumer, prevC, thisColor, thisAlpha);
                    vertex(matrices, vertexConsumer, prevD, thisColor, thisAlpha);
                }
                else {
                    vertex(matrices, vertexConsumer, vertices[0], thisColor, thisAlpha);
                    vertex(matrices, vertexConsumer, vertices[1], thisColor, thisAlpha);
                }
                vertex(matrices, vertexConsumer, between, betweenColor, betweenAlpha);

                prevC = vertices[2];
                prevD = vertices[3];
            }
            matrices.pop();
        }
    }

    public static void vertex(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d pos, Color color, int alpha) {
        MatrixStack.Entry entry = matrices.peek();
        vertexConsumer.vertex(entry.getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color.getRed(), color.getGreen(), color.getBlue(), alpha)
                .next();
    }
}
