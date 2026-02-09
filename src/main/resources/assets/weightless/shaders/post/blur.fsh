#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
};

out vec4 fragColor;

const float STRENGTH = 6.0;

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec2 offset = oneTexel * BlurDir * STRENGTH;

    vec3 result = texture(InSampler, texCoord).rgb * 0.227027;
    result += texture(InSampler, texCoord + offset).rgb * 0.1945946;
    result += texture(InSampler, texCoord - offset).rgb * 0.1945946;

    result += texture(InSampler, texCoord + offset * 2.0).rgb * 0.1216216;
    result += texture(InSampler, texCoord - offset * 2.0).rgb * 0.1216216;

    result += texture(InSampler, texCoord + offset * 3.0).rgb * 0.054054;
    result += texture(InSampler, texCoord - offset * 3.0).rgb * 0.054054;

    result += texture(InSampler, texCoord + offset * 4.0).rgb * 0.016216;
    result += texture(InSampler, texCoord - offset * 4.0).rgb * 0.016216;

    fragColor = vec4(result, 1.0);
}
