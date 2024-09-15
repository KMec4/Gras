#version 450
/*
layout(std140, set = 0, binding = 0) uniform buf {
    mat4 View;
    mat4 Projection;
    vec4 lightDir;
    vec4 lightColor;
} uBuf;

layout(std140, set = 0, binding = 1) uniform dynbuf {
    mat4 World;
    vec4 solidColor;
} dynBuf;
*/

layout(std140, set = 0, binding = 0) uniform buf
{
    mat4 projectionViewMatrix;
} ubo;

layout(push_constant) uniform Push
{
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;


layout(triangles) in;
layout(location = 0) in vec3 fragColorIN[];
layout(location = 1) in vec3 fragNormalIN[];

layout(triangle_strip, max_vertices = 6) out;

layout(location = 0) out vec3 fragColor;
layout(location = 1) out vec3 fragNormal;


vec4 generatePos(vec3 pos)
{
    return ubo.projectionViewMatrix * push.modelMatrix * vec4(pos, 1.0);
}

void main()
{
    vec4 v0 = gl_in[0].gl_Position;
    vec4 v1 = gl_in[1].gl_Position;
    vec4 v2 = gl_in[2].gl_Position;

    vec3 normal = normalize(fragNormalIN[0] + fragNormalIN[1] + fragNormalIN[2]);
    vec4 center = (v0 + v1 + v2) / 3.0f;

    for(int i = 0; i < 3; ++i)
    {
        fragNormal = fragNormalIN[i];
        fragColor = fragColorIN[i];
        gl_Position = generatePos(gl_in[i].gl_Position.xyz);
        EmitVertex();
    }
    EndPrimitive();

    vec4 bottom;
    if(normal.z != 0)
        bottom = vec4( 1, 1 , -( (normal.x + normal.y) / normal.z ), 1.0f);
    else
        bottom = vec4( -normal.y, normal.x, normal.z , 1.0f);

    bottom = normalize(bottom) * 0.03f;

    //Left Vertex
    fragNormal = normal;
    fragColor = vec3(1.0, 1.0, 0.0);
    gl_Position = generatePos( (center + bottom).xyz );
    EmitVertex();

    //Right Vertex
    fragNormal = normal;
    fragColor = vec3(1.0, 1.0, 0.0);
    gl_Position = generatePos( (center - bottom).xyz );
    EmitVertex();

    // Top Vertex
    fragNormal = normal;
    fragColor = vec3(1.0, 1.0, 0.0);
    gl_Position = generatePos( center.xyz + (normal * 0.5));
    EmitVertex();
}
