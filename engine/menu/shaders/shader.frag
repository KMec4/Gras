#version 450

layout (location = 0) in vec3 position;

layout (location = 0) out vec4 outColor;

layout(push_constant) uniform Push {
  mat4 transform; // projection * view * model
  mat4 normalMatrix;
} push;

void main()
{

  //discard();
  if(position.x < -0.2 || position.x > 0.2 || position.y < -0.2 || position.y > 0.2)
  {
    discard;
  }
  else
  {
  outColor = vec4(position.xy, 0.0, 0.1);
  }
}