#version 330

uniform sampler2D MainDepthSampler;
uniform sampler2D ItemDepthSampler;
uniform sampler2D ShinySampler;
uniform sampler2D ShinyDepthSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MaskConfig {
    int UseTransparency;
};

out vec4 fragColor;

void main() {
    fragColor = texture(ShinySampler, texCoord);
    float shinyDepth = texture(ShinyDepthSampler, texCoord).r;

    if (fragColor.a > 0.0) {
        if (UseTransparency == 1) {
            float itemDepth = texture(ItemDepthSampler, texCoord).r;
            if (texture(ItemDepthSampler, texCoord).r < shinyDepth) fragColor = vec4(0.0);
        }
        if (texture(MainDepthSampler, texCoord).r < shinyDepth) {
            fragColor = vec4(0.0);
        }
    }
}
