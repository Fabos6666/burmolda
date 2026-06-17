#version 150

#moj_import <burmalda:common.glsl>

uniform sampler2D Sampler0;

layout(std140) uniform Uniforms {
    vec2 size;
    vec4 round;
    float smoothness;
};

in vec2 texCoord0;
in vec4 vertexColor;
in vec2 fragCoord;

out vec4 OutColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    float alpha = ralpha(size, fragCoord, round, smoothness);
    OutColor = vec4(tex.rgb * vertexColor.rgb, tex.a * vertexColor.a * alpha);
}
