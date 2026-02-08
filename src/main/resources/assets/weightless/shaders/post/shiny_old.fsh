#version 330

uniform sampler2D InSampler;
uniform sampler2D FullSampler;
uniform sampler2D ShinySampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

const float TAU = 6.2831853;
const float RADIUS = 32.0;
const float STRENGTH = 2.0;
const int DIRECTIONS = 16;
const int STEPS = 6;

bool isShiny(vec2 uv) {
    return texture(InSampler, uv).a < 0.01 && texture(ShinySampler, uv).a > 0.0;
}

float distanceToShiny(vec2 uv, vec2 oneTexel, float radius) {
    if (isShiny(uv)) return -1.0;

    float closest = radius;
    for (int d = 0; d < DIRECTIONS; d++) {
        float angle = (float(d) / float(DIRECTIONS)) * TAU;
        vec2 dir = vec2(cos(angle), sin(angle));

        for (int s = 1; s <= STEPS; s++) {
            float t = float(s) / float(STEPS);
            float dist = t * radius;

            vec2 offset = dir * dist * oneTexel;

            if (isShiny(uv + offset)) {
                closest = min(closest, dist);
                break;
            }
        }
    }
    if (closest >= radius) return -1.0;

    return closest;
}

float playerBloomMask(vec2 uv, float radius) {
    vec2 oneTexel = 1.0 / InSize;
    float d = distanceToShiny(uv, oneTexel, radius);

    if (d < 0.0) return 0.0;

    float x = d / radius;
    return exp(-x * x * STRENGTH);
}


void main() {
    if (isShiny(texCoord)) {
        fragColor = texture(ShinySampler, texCoord);
    }
    else {
        float bloom = playerBloomMask(texCoord, RADIUS);
        if (bloom > 0.0) {
            vec3 scene = texture(FullSampler, texCoord).rgb;
            vec3 glowColor = vec3(1.0, 0.9, 0.6);

            fragColor = vec4(scene += bloom * glowColor * 1.5, 1.0);
        }
        else {
            discard;
        }
    }
}
