#version 450

//layout (location = 0) in vec3 fragColor;

layout (location = 0) in vec3 color;
layout (location = 1) in vec3 normal;

layout (location = 0) out vec4 outColor;

layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;


const float AMBIENT = 0.02;
const vec3 directionToLight = vec3( 1.0f, 1.0f, 0.5f);

void main() {
  
  float lightIntensity = AMBIENT + max(dot(normal, directionToLight), 0) + max(dot(normal, -directionToLight), 0); // should be lighted from both sides (;

  outColor = vec4( color * lightIntensity, 1.0f);

  //outColor = vec4(0.3f, 1.0f, 0.5f, 1.0f);
}