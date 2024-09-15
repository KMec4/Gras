#version 450

layout (location = 0) in vec2 uv;

layout (location = 0) out vec4 outColor;

layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;


void main()
{
    const float scale = 15.0f;
    bvec2 toDiscard = greaterThan( fract(uv * scale),
    vec2(0.1,0.1) );

    if( all(toDiscard) )
      discard;

    if( gl_FrontFacing )
      outColor = vec4(0.8f, 0.8f, 1.0f, 1.0f);
    else
      outColor = vec4(0.6f, 0.5f, 0.8f, 1.0f);
}