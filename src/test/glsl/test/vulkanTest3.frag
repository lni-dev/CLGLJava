#version 450

layout(location = 0) in vec3 fragColor;
layout(location = 1) in vec2 fragTexCoord;
layout(location = 2) in vec3 fragPosition;

layout(location = 0) out vec4 outColor;

layout(set = 1, binding = 0) uniform sampler2D texSampler;

void main() {
    outColor = vec4(fragTexCoord, 0.0, 1.0);
    outColor.rgb = texture(texSampler, fragTexCoord).rgb;
    //outColor.rgb = -normalize(cross(dFdx(fragPosition.xyz), dFdy(fragPosition.xyz)));
}