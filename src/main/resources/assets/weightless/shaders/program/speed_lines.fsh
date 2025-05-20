#version 150

uniform sampler2D DiffuseSampler;
uniform vec3 InSize;
uniform vec2 OutSize;
uniform float WorldTime;
uniform float FlySpeed;

in vec2 texCoord;
out vec4 fragColor;

//From Giorgi Azmaipharashvili (MIT licenced): https://www.shadertoy.com/view/ctdfzN
//Adapted to Minecraft by: Shiny (8hiny)

// From David Hoskins (MIT licensed): https://www.shadertoy.com/view/4djSRW
vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yxz + 33.33);
    return fract((p3.xxy + p3.yxx) * p3.zyx) - 0.5;
}

// From Nikita Miropolskiy (MIT licensed): https://www.shadertoy.com/view/XsX3zB
float simplex3d(vec3 p) {
    vec3 s = floor(p + dot(p, vec3(1.0 / 3.0)));
    vec3 x = p - s + dot(s, vec3(1.0 / 6.0));
    vec3 e = step(vec3(0), x - x.yzx);
    vec3 i1 = e * (1.0 - e.zxy);
    vec3 i2 = 1.0 - e.zxy * (1.0 - e);
    vec3 x1 = x - i1 + 1.0 / 6.0;
    vec3 x2 = x - i2 + 1.0 / 3.0;
    vec3 x3 = x - 0.5;
    vec4 w = max(0.6 - vec4(dot(x, x), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    w *= w;
    return dot(vec4(dot(hash33(s), x),
    dot(hash33(s + i1), x1),
    dot(hash33(s + i2), x2),
    dot(hash33(s + 1.0), x3)) * w * w, vec4(52));
}

void main() {
    vec2 uv = (texCoord.xy * 2.0) - 1.0;
    vec4 baseColor = texture(DiffuseSampler, texCoord);

    float distance = distance(uv.xy, vec2(0));
    float time = WorldTime * 0.15;

    vec2 p = vec2(0.5) + normalize(uv) * min(length(uv), 0.05);
    vec3 p3 = 13.0 * vec3(p.xy, 0) + vec3(0, 0, time * 0.025);

    float noise = simplex3d(p3 * 32.0) * 0.5 + 0.5;
    float dist = abs(clamp(length(uv) / 12.0, 0.0, 1.0) * noise * 2.0 - 1.0);

    const float e = 0.3;
    float stepped = smoothstep(e - 0.5, e + 0.5, noise * (1.0 - pow(dist, 4.0)));
    float final = smoothstep(e - 0.05, e + 0.05, noise * stepped);
    float strength = FlySpeed * (distance * distance * distance * distance) * 0.16;

    fragColor = vec4(baseColor.rgb + final * strength, baseColor.a);
}