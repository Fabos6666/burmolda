#version 150

#moj_import <burmalda:common.glsl>

uniform sampler2D Sampler0;

layout(std140) uniform Uniforms {
    vec2 size;
    vec4 round;
    float smoothness;
    float alpha;
};

in vec2 texCoord0;
in vec2 fragCoord;

out vec4 OutColor;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    vec3 color = texture(Sampler0, uv).rgb;
    float shapeAlpha = ralpha(size, fragCoord, round, smoothness);

    float dither = (hash12(gl_FragCoord.xy) - 0.5) / 255.0;
    color = clamp(color + vec3(dither), 0.0, 1.0);

    OutColor = vec4(color, shapeAlpha * alpha);
}
