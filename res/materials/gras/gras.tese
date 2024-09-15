#version 450 core


layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;

layout(set = 0, binding = 0) uniform GlobalUbo {
  mat4 projectionViewMatrix;
} ubo;

layout(triangles, equal_spacing, cw) in;

layout(location = 0) in vec3 normalIn[];

layout (location = 0) out vec3  pos;
layout (location = 1) out vec3  normal;


vec3 interpolate3D(vec3 v0, vec3 v1, vec3 v2)
{
    return gl_TessCoord.x * v0 + gl_TessCoord.y * v1 + gl_TessCoord.z * v2;
}

void main()
{ 
    gl_Position = /*ubo.projectionViewMatrix * push.modelMatrix */ vec4(interpolate3D(gl_in[0].gl_Position.xyz, gl_in[1].gl_Position.xyz, gl_in[2].gl_Position.xyz), 1.0f);
	pos = gl_Position.xyz;
	normal = interpolate3D(normalIn[0], normalIn[1], normalIn[2]);
}
