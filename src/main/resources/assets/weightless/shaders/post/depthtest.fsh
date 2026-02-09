#version 330

uniform sampler2D MainDepthSampler;
uniform sampler2D ShinySampler;
uniform sampler2D ShinyDepthSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main() {
    vec4 shinyColor = texture(ShinySampler, texCoord);
    if (texture(MainDepthSampler, texCoord).r < texture(ShinyDepthSampler, texCoord).r && shinyColor.a > 0.0) {
        fragColor = vec4(0.0);
    }
    else {
        fragColor = shinyColor;
    }
}
