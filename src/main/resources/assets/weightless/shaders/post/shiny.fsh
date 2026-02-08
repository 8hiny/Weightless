#version 330

uniform sampler2D InSampler;
uniform sampler2D ShinySampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main() {
    vec4 mainColor = texture(InSampler, texCoord);
    vec4 shinyColor = texture(ShinySampler, texCoord);

    if (mainColor.rgb == (0.0, 0.0, 0.0) && shinyColor.rgb != (0.0, 0.0, 0.0)) {
        fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
    else {
        discard;
    }
}
