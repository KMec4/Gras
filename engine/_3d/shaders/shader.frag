#version 450

layout (location = 0) in vec3 fragColor;
layout (location = 1) in vec3 normal;

layout (location = 0) out vec4 outColor;

layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;


const float AMBIENT = 0.02;
const vec3 directionToLight = vec3( 1.0f, 1.0f, 0.5f);

void main() {
  
  float lightIntensity = AMBIENT + max(dot(normal, directionToLight), 0);

  outColor = vec4(0.8f, 1.0f, 1.0f, 1.0f); //vec4( fragColor * lightIntensity, 1.0f);
}