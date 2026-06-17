#version 150

#moj_import <burmalda:common.glsl>

out vec4 OutColor;

in vec2 FragCoord;
in vec4 FragColor;

layout(std140) uniform Uniforms {
    vec2 size;
    vec4 round;
    float thickness;
    float smoothness;
};

void main() {
    vec2 center = size * 0.5;
    float dist = rdist(center - (FragCoord * size), center - 1.0, round);
    float sd = dist - 1.0;

    float aa = max(fwidth(sd), 1e-4) * max(smoothness, 0.5);

    float outer = 1.0 - smoothstep(-aa, aa, sd);
    float inner = 1.0 - smoothstep(-aa, aa, sd + thickness);
    float alpha = clamp(outer - inner, 0.0, 1.0);

    OutColor = vec4(FragColor.rgb, FragColor.a * alpha);
}
