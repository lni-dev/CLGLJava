#define int2(X, Y) ((int2)(X, Y))
#define int3(X, Y, Z) ((int3)(X, Y, Z))
#define int4(X, Y, Z, W) ((int4)(X, Y, Z, W))

#define toFloat2(X) ((float2)(X))
#define toFloat3(X) ((float3)(X))
#define toFloat4(X) ((float4)(X))

#define float2(X, Y) ((float2)(X, Y))
#define float3(X, Y, Z) ((float3)(X, Y, Z))
#define float4(X, Y, Z, W) ((float4)(X, Y, Z, W))

/**
 * absolute value of A
 */
#define absolute(A) (A * sign(A))


float4 mainImage(const float2 uv, const int2 pixel, const int2 resolution, const float loadingProgress) {
    float transformed = loadingProgress -.5f; //move it to match uv
    if(uv.y > transformed) {
        return float4(0.f, 0.f, 0.f, 1.f);
    }

    return float4(0.65f, 0.3f, 0.96f, 1.f);
}

__kernel void render(
    __write_only image2d_t ui,
    const int2 screenSize,
    const float loadingProgress
    )
{

    const int2 cordi = int2(get_global_id(0), get_global_id(1));
    const float2 uv = float2(
            ((float) (cordi.x - (screenSize.x / 2))) / ((float) screenSize.y),
            ((float) (cordi.y - (screenSize.y / 2))) / ((float) screenSize.y)
        );

    write_imagef(ui, cordi, mainImage(uv, cordi, screenSize, loadingProgress));
}