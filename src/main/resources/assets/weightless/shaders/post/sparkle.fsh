#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform SparkleConfig {
    vec2 SparklePos;
    float SourceDistance;
    int WorldTime;
};

out vec4 fragColor;

//Sparkle Parameters
const float lineExp = 8.776; //Total sparkle size; inverse value

const float line1Mul = 0.95; //Size of line1
const float line2Mul = 1.1; //Size of line2
const vec2 lineDir = vec2(1.0, 0.0); //Rotation of the sparkle lines

const float glareMul = 1.1; //0.736; //Glare intensity; Mainly affects size of webbing between lines; values below 1.0 weaken the effect
const float sparkleMul = 1.0; //1.0; //Somehow also sparkle size / glare intensity
const vec3 sparkleColor = vec3(1.0, 0.825, 0.4);


float udLine(vec2 uv, vec2 a, vec2 b) {
    vec2 pa = uv - a;
    vec2 ba = b - a;
    return length(pa - ba * dot(pa, ba) / dot(ba, ba));
}

void main() {
    vec4 mainColor = texture(InSampler, texCoord);
    vec3 color = mainColor.rgb;
    vec2 uv = (texCoord * 2.0) - 1.0;

    //Scaling for pulsing effect
    float pulse = (sin(WorldTime * 0.1) / 3.0 + 1.0) / 2.0;

    //Sparkles
    float strength = 1.0 - SourceDistance;
    float sparkle = distance(uv, SparklePos) * 2.0;
    if(sparkle < 1.0) {
        float line = udLine(uv, SparklePos, SparklePos + lineDir) * -line1Mul; //Generate line in one direction
        float line2 = udLine(uv, SparklePos, SparklePos + vec2(-lineDir.y, lineDir.x)) * (-line2Mul + SourceDistance * SourceDistance * 0.4); //Generate line orthogonally to the first line

        float lines = 1.0 + (line + line2); //Add up lines color
        float glare = pow(lines, lineExp) * glareMul * strength; //Same here

        color = color + glare * (1.0 - sparkle * -sparkleMul * SourceDistance) * pulse * 2.0 * sparkleColor;
    }
    fragColor = vec4(clamp(color.rgb, 0.0, 1.0), mainColor.a);
}
