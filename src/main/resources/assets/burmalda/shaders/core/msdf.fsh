#version 150

uniform sampler2D Sampler0;

layout(std140) uniform Uniforms {
    vec2 params; // x = distanceRange, y = edge scale
};

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 OutColor;

float median(vec3 value) {
    return max(min(value.r, value.g), min(max(value.r, value.g), value.b));
}

vec3 sampleLinearMSDF(sampler2D tex, vec2 uv) {
    vec2 texSize = vec2(textureSize(tex, 0));
    vec2 pixel = uv * texSize;

    vec2 base = floor(pixel - 0.5) + 0.5;
    vec2 f = fract(pixel - 0.5);

    vec3 c00 = texture(tex, (base + vec2(0.0, 0.0)) / texSize).rgb;
    vec3 c10 = texture(tex, (base + vec2(1.0, 0.0)) / texSize).rgb;
    vec3 c01 = texture(tex, (base + vec2(0.0, 1.0)) / texSize).rgb;
    vec3 c11 = texture(tex, (base + vec2(1.0, 1.0)) / texSize).rgb;

    vec3 cx0 = mix(c00, c10, f.x);
    vec3 cx1 = mix(c01, c11, f.x);
    return mix(cx0, cx1, f.y);
}

void main() {
    vec3 msdfSample = sampleLinearMSDF(Sampler0, texCoord0);
    float signedDistance = median(msdfSample) - 0.5;

    vec2 unitRange = vec2(params.x) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord0), vec2(1e-5));
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);

    float alpha = clamp(signedDistance * screenPxRange * params.y + 0.5, 0.0, 1.0);

    OutColor = vec4(vertexColor.rgb, vertexColor.a * alpha);
}
