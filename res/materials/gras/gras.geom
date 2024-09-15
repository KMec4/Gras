#version 450

const int verticlesCount = 12;

const float bottomScaler = 0.05f;
const float heightScaler = 0.5f;

layout(std140, set = 0, binding = 0) uniform buf {
    mat4 projectionViewMatrix;
} ubo;

layout(push_constant) uniform Push {
  mat4 modelMatrix;
  mat4 normalMatrix;
} push;

layout(points) in;
layout(triangle_strip, max_vertices = verticlesCount) out;

layout (location = 0) in vec3 inPos[];
layout (location = 1) in vec3 normals[];

layout(location = 0) out vec3 color;
layout(location = 1) out vec3 normalOut;

vec4 generatePos(vec3 pos)
{
    return ubo.projectionViewMatrix * push.modelMatrix * vec4(pos, 1.0);
}

vec3 calcColor(int i)
{
    return vec3( .4f, 1.0f * (i + 5) / (verticlesCount + 5)  , .2f);
}

float rand( vec3 pos)
{
    return fract(sin(dot(pos.xz, vec2(12.9898, 78.233))) * 43758.5453);
}

void main()
{
    color = vec3(.5f, 1.0f, .2f);

    float angel = 3.141 * rand(inPos[0]);
    float angelF = 3.141 * ( rand(inPos[0]*256) - 0.5 ) * 0.25;

    vec3 normal = normalize(normals[0]);
    vec3 bottom;
    if(normal.z != 0)
        bottom = vec3( 1, 1 , -( (normal.x + normal.y) / normal.z ));
    else
        bottom = vec3( -normal.y, normal.x, normal.z );

    bottom = normalize(bottom);
    bottom = bottom * cos(angel) + normal * (bottom * normal) * (1 - cos(angel)) - cross(bottom, normal) * sin(angel); // rotation
    normal = normal * cos(angelF) + bottom * (normal * bottom) * (1 - cos(angelF)) - cross(normal, bottom) * sin(angelF); // rotation forward bend

    vec3 outNormal = cross(bottom, normal);
    bottom = bottom * bottomScaler / verticlesCount;

    normal = normal * heightScaler / verticlesCount;

    for(int i = 1; i < verticlesCount - 1; i+=2)
    {
        //Left Vertex
        color = calcColor(i);
        normalOut = outNormal;
        gl_Position = generatePos( inPos[0] + (bottom * (verticlesCount - i)) + ( normal * (i - 1) ));
        EmitVertex();

        //Right Vertex
        color = calcColor(i);
        normalOut = outNormal;
        gl_Position = generatePos ( inPos[0] - (bottom * (verticlesCount - i)) + ( normal * (i - 1) ));
        EmitVertex();
    }
    // Top Vertex
    color = vec3(0.2f, 1.0f, 0.3f);
        normalOut = outNormal;
    gl_Position = generatePos( inPos[0] + (normal * verticlesCount) );
    EmitVertex();
}

