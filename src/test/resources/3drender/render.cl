

// Copyright (c) 2023 Linus Andera

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
 * returns biggest component of any 3-component type
 */
#define vmax(A) (max(A.z, max(A.x, A.y)))

/**
 * absolute value of A
 */
#define absolute(A) (A * sign(A))

/**
 * if(thisId != ignoreId && currentDis > thisDis) {
 *     currentDis = thisDis; currentId = thisId;
 * }
 */
#define mmin(currentDis, thisDis, currentId, thisId, ignoreId) if(thisId != ignoreId && currentDis > thisDis) {currentDis = thisDis; (*currentId) = thisId;}


//CURRENTLY FIXED VALUES
#define MIN_DISTANCE 0.0001f
#define MAX_STEPS 100u
#define RENDER_DISTANCE 100.f

#define UP_VECTOR float3(0.f, 1.f, 0.f)

#define NO_HIT_ID 0u
#define BOX_1_ID 1u
#define GROUND_ID 4u

typedef struct camera
{
    float3 position;
    float3 lookAtVector;
    float distanceToScreen;
} camera;
//size 48

float sdfBox(float3 pos, float3 center, float3 size) {
    pos -= center;
    float3 d = absolute(pos) - size*.5f;
    float m = (vmax(d));
    return max(length(min(sign(d) + toFloat3(1.f), 1.f) * d), absolute(m)) * sign(m);
}

inline float sdfSphere(float3 pos, float3 center, float radius){
    return length(pos-center) - radius;
}


float getDistance(float3 pos, uint* hitId) {
    (*hitId) = NO_HIT_ID;
    float dis = RENDER_DISTANCE;
    
    float disYPlane = absolute(pos.y+0.1)-0.1;
    float disBox1 = sdfBox(pos, float3(1., 1.5, 0.), toFloat3(1.));
    
    mmin(dis, disYPlane, hitId, GROUND_ID, 0u);
    mmin(dis, disBox1, hitId, BOX_1_ID, 0u);
    
    return dis;
}

uint unsued;
float3 getNormal(float3 p){
 	float d = getDistance(p, &unsued);
    float2 e = float2(.01, 0);
    
    float3 n = d - float3(getDistance(p - e.xyy, &unsued), getDistance(p - e.yxy, &unsued), getDistance(p - e.yyx, &unsued));
    return normalize(n);
}

typedef struct Ray {
    float3 o;//origin
    float3 d;//distance
    float md;//max distance
} Ray;

typedef struct Hit {
    float3 pos;
    float dis;
    uint id;
} Hit;

typedef struct Light{
    float3 position;
    float3 color;
    float3 direction;  //spotlight only
    float angle;     //spotlight only
    
}Light;

Light lights[1] = { 
  (Light){float3(0., 13., 0.), float3(1., 1., 1.), toFloat3(0.0), 0.0}
};

Hit rayMarch(Ray r){
    
    Hit hit = {r.o, 0.0, NO_HIT_ID};
    
    float dis = 1000.;
    for(uint i = 0u; i < MAX_STEPS; i++){
        dis = getDistance(hit.pos, &hit.id);
        hit.dis += dis;
        
        hit.pos = r.o + r.d * hit.dis;
        
        if(dis < MIN_DISTANCE || hit.dis >= r.md){
            break;
        }
    }
    
    if(dis > MIN_DISTANCE) {
        hit.id = NO_HIT_ID;
    }
    
    return hit;
}

float4 mainImage(float2 uv, camera cam) {
    float4 col = toFloat4(0.0);

    //read Vars
    float4 camPosition = toFloat4(1.f); //xyz = pos; w = distance to screen
    camPosition.xyz = cam.position;
    float4 viewDirection = toFloat4(1.f);
    viewDirection.xyz = cam.lookAtVector - camPosition.xyz;
    viewDirection.xyz = normalize(viewDirection.xyz);
    
    //calc ray Direction
    float3 screenXVector = cross(viewDirection.xyz, UP_VECTOR);
    float3 screenYVector = -cross(viewDirection.xyz, screenXVector);
    
    float3 rayDirection = viewDirection.xyz * camPosition.w + screenXVector * uv.x + screenYVector  * uv.y;   
    rayDirection = normalize(rayDirection);
    
    
    Hit hit = rayMarch((Ray){camPosition.xyz, rayDirection, RENDER_DISTANCE});
    
    if(hit.id != NO_HIT_ID){
        if(hit.id == GROUND_ID){
            col.xyz = float3(1.0f, .1f, 1.f);
        }else if(hit.id == BOX_1_ID){
            col.xyz = float3(0.9, 0.0, 0.0);
        }
        
        float3 normal = getNormal(hit.pos);
        float3 lightColor = toFloat3(0.0);
        
        for(int i = 0; i < 1; i++){
            Light light = lights[i];
         
            
            float3 dir = light.position - hit.pos;
            float dis = length(dir);
            dir = normalize(dir);
            Hit lHit = rayMarch((Ray){hit.pos + normal*0.001f, dir, dis});
            
            if(lHit.id == NO_HIT_ID){
                
                float angleFact = dot(normal, dir);
                float disFact = 1. / (1. + dis*dis*.001);
                
                float inte = angleFact*disFact;

                lightColor += light.color * toFloat3(inte);
            }
            
        }
        
        col.xyz *= lightColor;
    }else {
        col.xyz = float3(0.3, 0.6, 0.8)*3.f;
    }

    return col;
}



__kernel void render(
    __write_only image2d_t img,
    const int2 screenSize,
    __global camera* cam
    )
{

    const int2 cordi = int2(get_global_id(0), get_global_id(1));
    const float2 uv = float2(
            ((float) (cordi.x - (screenSize.x / 2))) / ((float) screenSize.y),
            ((float) (cordi.y - (screenSize.y / 2))) / ((float) screenSize.y)
        );

    write_imagef(img, cordi, mainImage(uv, *cam));
}