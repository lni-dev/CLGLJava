#define uint2(X, Y) ((uint2)(X, Y))

#define int2(X, Y) ((int2)(X, Y))
#define int3(X, Y, Z) ((int3)(X, Y, Z))
#define int4(X, Y, Z, W) ((int4)(X, Y, Z, W))

#define float2(...) ((float2)(__VA_ARGS__))
#define float3(...) ((float3)(__VA_ARGS__))
#define float4(...) ((float4)(__VA_ARGS__))

float4 mainImage(float2 uv) {
    float4 col = float4(length(uv));
    col.a = 1.f;
    return col;
}


__kernel void render(
    __write_only image2d_t img,
    const int2 screenSize
    //__read_only float3 cameraPos
    )
{
    const int2 cordi = int2(get_global_id(0), get_global_id(1));
    const float2 uv = float2(
                ((float) (cordi.x - (screenSize.x / 2))) / ((float) screenSize.y),
                ((float) (cordi.y - (screenSize.y / 2))) / ((float) screenSize.y)
            );

    write_imagef(img, cordi, mainImage(uv));
}