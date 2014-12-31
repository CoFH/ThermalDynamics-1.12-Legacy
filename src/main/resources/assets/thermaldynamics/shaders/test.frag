#version 120

uniform sampler2D texture;
uniform int time;

// I don't use these anymore, they just didn't look right when used on a small non-contiguous surface like a pipes
uniform float xpos;
uniform float zpos;

// this allows conduits to flash, normally set to 0
uniform float t;

void main() {
    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 color = texture2D(texture, texcoord / 2);
    
    float ix = xpos;
    float iy = zpos;


    float old_alpha = gl_TexCoord[0].a;

    //adds slight purple tint with random noise
    vec4 col = vec4(0.047, 0.035, 0.063, 1) + noise1(gl_FragCoord.xy) * vec4(0.0196, 0.0216, 0.0235, 0);

    for(int i=0; i<16; i++) {
    	float mult = 1.0/(16-i);
    	float imult = 1.0/(i+1);
    	
    	float angle = (i * i * 4321 + i * 8) * 2.0F;
    	float dx = sin(angle * 57.295F);
    	float dy = cos(angle * 57.295F);
    	
    	float ox = gl_FragCoord.x + ix * (1.5-mult*0.5) - (time * dx * 0.5) * mult;
    	float oy = gl_FragCoord.y + iy * (1.5-mult*0.5) + (time * dy * 0.5) * mult;
    	
    	float tx = (ox / 48) * imult + dx;
    	float ty = (oy / 48) * imult + dy;
    	
    	vec2 tex = vec2(tx*dy + ty*dx, ty*dy - tx*dx);
    	
    	vec4 tcol = texture2D(texture, tex / 2);
    	
    	float a = tcol.r * (0.1 + mult * 0.9);
    	
    	float r = (mod(angle, 29.0)/29.0) * 0.5 + 0.1;
    	float g = (mod(angle, 35.0)/35.0) * 0.5 + 0.4;
    	float b = (mod(angle, 17.0)/17.0) * 0.5 + 0.5;
    	
    	col = col*(1-a) + vec4(r,g,b,1)*a;
    }

    // this controls the brightness and is determined by whether the conduit is flashing
    // and also how far away it is
    float br = clamp(2.5*gl_FragCoord.w + t,0,1);
    
    col = col * br + vec4(0.047, 0.035, 0.063, 1) * (1-br);

    // increase the brightness of flashing conduits
    col.rgb = clamp(col.rgb * (1+t*4),0,1);
    
    col.a = 1;
    
    gl_FragColor = col;
}