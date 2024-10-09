#version 450

layout(set = 0, binding = 0, row_major, std140) uniform UniformBufferObject {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inColor;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec3 fragColor;
layout(location = 1) out vec2 fragTexCoord;
layout(location = 2) out vec3 fragPosition;

void main() {
    gl_Position = ubo.proj*ubo.view*ubo.model*vec4(inPosition.xyz, 1.0);
    fragPosition = inPosition.xyz;
    fragColor = inColor;
    fragTexCoord = inTexCoord;
}