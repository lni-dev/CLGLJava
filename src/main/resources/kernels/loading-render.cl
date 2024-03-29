#define int2(X, Y) ((int2)(X, Y))
#define int3(X, Y, Z) ((int3)(X, Y, Z))
#define int4(X, Y, Z, W) ((int4)(X, Y, Z, W))

#define toFloat2(X) ((float2)(X))
#define toFloat3(X) ((float3)(X))
#define toFloat4(X) ((float4)(X))

#define float2(X, Y) ((float2)(X, Y))
#define float3(X, Y, Z) ((float3)(X, Y, Z))
#define float4(X, Y, Z, W) ((float4)(X, Y, Z, W))



float4 mainImage(float2 uv) {
    return float4(0.f, 0.f, 0.f, 2.f);
}

__kernel void render(
    __write_only image2d_t img,
    const int2 screenSize,
    __read_only image2d_t ui
    )
{

    const int2 cordi = int2(get_global_id(0), get_global_id(1));
    const float2 uv = float2(
            ((float) (cordi.x - (screenSize.x / 2))) / ((float) screenSize.y),
            ((float) (cordi.y - (screenSize.y / 2))) / ((float) screenSize.y)
        );

    float4 uiColor = read_imagef(ui, cordi);

    if(uiColor.w < 1.0f) {
        float4 renderColor = mainImage(uv);
        renderColor.xyz = mix(renderColor.xyz, uiColor.xyz, uiColor.w);
        write_imagef(img, cordi, renderColor);
    } else {
        write_imagef(img, cordi, uiColor);
    }


}