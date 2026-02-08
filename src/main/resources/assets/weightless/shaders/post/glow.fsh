#version 330

uniform sampler2D InSampler;
uniform sampler2D ShinySampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

bool isShiny(vec2 uv) {
    return texture(InSampler, uv).a < 0.01 && texture(ShinySampler, uv).a > 0.0;
}

void main() {
    if (isShiny(texCoord)) {
        float mask = texture(ShinySampler, texCoord).a;
        float intensity = mask * 12.0;
        //vec3 glowColor = vec3(1.0, 0.9, 0.6);
        vec3 glowColor = vec3(0.9, 0.69, 0.18);

        fragColor = vec4(glowColor * intensity, 1.0);
    }
    else {
        discard;
    }
}
