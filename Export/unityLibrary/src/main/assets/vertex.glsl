attribute vec4 aPosition;
attribute mediump vec2 aTextureCoord;

varying mediump vec2 vTextureCoord;

void main() {
  gl_Position = aPosition;
  vTextureCoord = aTextureCoord.xy;
}