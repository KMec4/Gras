#version 450

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 color;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec2 uv;

layout(location = 0) out vec3 normalOut;


layout(set = 0, binding = 0) uniform GlobalUbo {
  mat4 projectionViewMatrix;
} ubo;

layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;

out gl_PerVertex {
    vec4 gl_Position;
};

void main() {
  gl_Position = /* ubo.projectionViewMatrix * push.modelMatrix * */ vec4(position, 1.0);

  //vec3 normalWorldSpace = (ubo.projectionViewMatrix * push.modelMatrix * vec4(normal, 1.0)).xyz;

  normalOut = normalize(normal);
}