#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ScaleConfig {
    int UpOrDown;
};

out vec4 fragColor;

vec3 downsample(vec2 uv, vec2 oneTexel) {
    vec3 color = vec3(0.0);
    color += texture(InSampler, uv + oneTexel * vec2(-1,-1)).rgb;
    color += texture(InSampler, uv + oneTexel * vec2(1,-1)).rgb;
    color += texture(InSampler, uv + oneTexel * vec2(-1, 1)).rgb;
    color += texture(InSampler, uv + oneTexel * vec2(1, 1)).rgb;
    return color * 0.25;
}

vec3 upsample(vec2 uv, vec2 oneTexel) {
    vec3 color = texture(InSampler, uv).rgb * 4.0;
    color += texture(InSampler, uv + vec2(oneTexel.x, 0)).rgb;
    color += texture(InSampler, uv - vec2(oneTexel.x, 0)).rgb;
    color += texture(InSampler, uv + vec2(0, oneTexel.y)).rgb;
    color += texture(InSampler, uv - vec2(0, oneTexel.y)).rgb;
    return color / 8.0;
}

void main() {
    vec2 oneTexel = 1.0 / InSize;
    if (UpOrDown == 0) {
        fragColor = vec4(downsample(texCoord, oneTexel), 1.0);
    }
    else if (UpOrDown == 1) {
        fragColor = vec4(upsample(texCoord, oneTexel), 1.0);
    }
}
