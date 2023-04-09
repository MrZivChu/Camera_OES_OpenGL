attribute vec4 aPosition;
attribute mediump vec2 aTextureCoord;

uniform mat4 uMVPMatrix;

varying mediump vec2 vTextureCoord;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  vTextureCoord = aTextureCoord.xy;
}