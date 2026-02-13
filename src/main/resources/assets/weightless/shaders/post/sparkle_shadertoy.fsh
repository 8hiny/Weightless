//Sparkle Parameters
const float lineExp = 8.776; //Total sparkle size; inverse value

const float line1Mul = 1.0; //Size of line1
const float line2Mul = 1.0; //Size of line2
const vec2 lineDir = vec2(1.0, 0.0); //Rotation of the sparkle lines

const float glareMul = 1.1; //0.736; //Glare intensity; Mainly affects size of webbing between lines; values below 1.0 weaken the effect
const float sparkleMul = 1.0; //1.0; //Somehow also sparkle size / glare intensity


float udLine(vec2 texCoord, vec2 a, vec2 b) {
    vec2 pa = texCoord - a;
    vec2 ba = b - a;
    return length(pa - ba * dot(pa, ba) / dot(ba, ba));
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    //Scaling for pulsing effect
    float pulse = (sin(iTime * 2.0) / 4.0 + 1.0) / 2.0;

    //Screen sparkle position setup
    vec2 uv = fragCoord.xy / iResolution.xy;
    float aspect = iResolution.x / iResolution.y;
    vec2 pt = (uv * 2.0 - 1.0) * vec2(aspect, 1.0); //Current pixel position on screen


    vec3 color = vec3(0.0);


    //Sparkles
    vec2 glintPos = vec2(0.0);

    float dist = distance(pt, glintPos);

    float sparkle = dist * 2.0;
    if(sparkle < 1.0) {

        //Generate line in one direction
        float line1 = udLine(pt, glintPos, glintPos + lineDir) * -line1Mul; //Left diagonal
        //Generate line orthogonally to line1
        float line2 = udLine(pt, glintPos, glintPos + vec2(-lineDir.y, lineDir.x)) * -line2Mul; //Right diagonal

        float lines = 1.0 + (line1 + line2); //Add up lines color

        float glare = pow(lines, lineExp) * glareMul; //Same here

        color = color + glare * (1.0 - sparkle * -sparkleMul) * pulse * 2.0;
    }
    color *= vec3(1.0, 0.825, 0.4); //Can color the glint; this creates a soft gold color

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}