#version 330

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D GlowSampler;
uniform sampler2D ShinySampler;
uniform sampler2D ShinyDepthSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main() {
    float mainDepth = texture(MainDepthSampler, texCoord).r;
    float shinyDepth = texture(ShinyDepthSampler, texCoord).r;
    vec4 mainColor = texture(MainSampler, texCoord);
    vec4 shinyColor = texture(ShinySampler, texCoord);

    if (shinyColor.a > 0.0 && mainColor == vec4(1.0)) {
        if (mainDepth < shinyDepth) {
            fragColor = fragColor = mainColor;
        }
        else {
            fragColor = shinyColor;
        }
    }
    else {
        vec3 bloom = texture(GlowSampler, texCoord).rgb;

        if (mainColor == vec4(1.0)) { //Only adjust bloom amount for where shiny is if nothing is in front of shiny
            bloom *= (1.0 - texture(ShinySampler, texCoord).a);
        }
        fragColor = vec4(mainColor.rgb += bloom * 1.5, 1.0);
    }
}
