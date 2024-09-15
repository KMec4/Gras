#version 450 core

layout(vertices = 3) out;


layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;


in gl_PerVertex {
    vec4 gl_Position;
} gl_in[];


layout(location = 0) in vec3 normals[];

layout(location = 0) out vec3 normalsOut[];


void main(void)
{
 gl_TessLevelOuter[0] = 16.0;
 gl_TessLevelOuter[1] = 16.0;
 gl_TessLevelOuter[2] = 16.0;

 gl_TessLevelInner[0] = 16.0;
/*
 position [gl_InvocationID] = positionIN[0];
 top      [gl_InvocationID] = topIN[0];
 scaler   [gl_InvocationID] = scalerIN[0];*/

 gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
 normalsOut[gl_InvocationID] = normals[gl_InvocationID];
}