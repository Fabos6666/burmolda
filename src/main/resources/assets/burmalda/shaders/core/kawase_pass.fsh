#version 150

uniform sampler2D Sampler0;

layout(std140) uniform Uniforms {
    vec2 texelSize;
    vec2 offset;
};

in vec2 texCoord0;

out vec4 OutColor;

void main() {
    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    vec2 sampleOffset = texelSize * offset;

    vec3 sum = texture(Sampler0, uv).rgb * 4.0;

    sum += texture(Sampler0, uv + vec2(sampleOffset.x, 0.0)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(-sampleOffset.x, 0.0)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(0.0, sampleOffset.y)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(0.0, -sampleOffset.y)).rgb * 2.0;

    sum += texture(Sampler0, uv + vec2(sampleOffset.x, sampleOffset.y)).rgb;
    sum += texture(Sampler0, uv + vec2(-sampleOffset.x, sampleOffset.y)).rgb;
    sum += texture(Sampler0, uv + vec2(sampleOffset.x, -sampleOffset.y)).rgb;
    sum += texture(Sampler0, uv + vec2(-sampleOffset.x, -sampleOffset.y)).rgb;

    OutColor = vec4(sum / 16.0, 1.0);
}
