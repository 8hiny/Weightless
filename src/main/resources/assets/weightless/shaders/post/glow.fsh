#version 330

uniform sampler2D ShinySampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

const float INTENSITY = 2.0;

void main() {
    vec4 shinyColor = texture(ShinySampler, texCoord);
    fragColor = vec4(shinyColor.rgb * INTENSITY, 1.0);
}
