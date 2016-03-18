/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.grafika.gles;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * GL program and supporting functions for textured 2D shapes.
 */
public class Texture2dProgram {
    private static final String TAG = GlUtil.TAG;

    public enum ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW, TEXTURE_EXT_FILT, FRAGMENT_TEST
    }

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // Fragment shader that converts color to black & white with a simple transformation.
    private static final String FRAGMENT_SHADER_EXT_BW =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
            "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
            "}\n";

//    private static final String FRAGMENT_TEST =
//            "attribute vec4 a_position;\n" +
//                    "attribute vec2 a_texCoord;\n" +
//                    " \n" +
//                    "varying vec2 v_texCoord;\n" +
//                    "varying vec2 v_blurTexCoords[14];\n" +
//                    " \n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    gl_Position = a_position;\n" +
//                    "    v_texCoord = a_texCoord;\n" +
//                    "    v_blurTexCoords[ 0] = v_texCoord + vec2(-0.028, 0.0);\n" +
//                    "    v_blurTexCoords[ 1] = v_texCoord + vec2(-0.024, 0.0);\n" +
//                    "    v_blurTexCoords[ 2] = v_texCoord + vec2(-0.020, 0.0);\n" +
//                    "    v_blurTexCoords[ 3] = v_texCoord + vec2(-0.016, 0.0);\n" +
//                    "    v_blurTexCoords[ 4] = v_texCoord + vec2(-0.012, 0.0);\n" +
//                    "    v_blurTexCoords[ 5] = v_texCoord + vec2(-0.008, 0.0);\n" +
//                    "    v_blurTexCoords[ 6] = v_texCoord + vec2(-0.004, 0.0);\n" +
//                    "    v_blurTexCoords[ 7] = v_texCoord + vec2( 0.004, 0.0);\n" +
//                    "    v_blurTexCoords[ 8] = v_texCoord + vec2( 0.008, 0.0);\n" +
//                    "    v_blurTexCoords[ 9] = v_texCoord + vec2( 0.012, 0.0);\n" +
//                    "    v_blurTexCoords[10] = v_texCoord + vec2( 0.016, 0.0);\n" +
//                    "    v_blurTexCoords[11] = v_texCoord + vec2( 0.020, 0.0);\n" +
//                    "    v_blurTexCoords[12] = v_texCoord + vec2( 0.024, 0.0);\n" +
//                    "    v_blurTexCoords[13] = v_texCoord + vec2( 0.028, 0.0);\n" +
//                    "}\n";


    private static final String FRAGMENT_TEST =
            "float smoothNoise(vec2 p) {\n" +
                    "  vec2 nn = vec2(p.x, p.y+1.);\n" +
                    "  vec2 ne = vec2(p.x+1., p.y+1.);\n" +
                    "  vec2 ee = vec2(p.x+1., p.y);\n" +
                    "  vec2 se = vec2(p.x+1., p.y-1.);\n" +
                    "  vec2 ss = vec2(p.x, p.y-1.);\n" +
                    "  vec2 sw = vec2(p.x-1., p.y-1.);\n" +
                    "  vec2 ww = vec2(p.x-1., p.y);\n" +
                    "  vec2 nw = vec2(p.x-1., p.y+1.);\n" +
                    "  vec2 cc = vec2(p.x, p.y);\n" +
                    " \n" +
                    "  float sum = 0.;\n" +
                    "  sum += randomNoise(nn);\n" +
                    "  sum += randomNoise(ne);\n" +
                    "  sum += randomNoise(ee);\n" +
                    "  sum += randomNoise(se);\n" +
                    "  sum += randomNoise(ss);\n" +
                    "  sum += randomNoise(sw);\n" +
                    "  sum += randomNoise(ww);\n" +
                    "  sum += randomNoise(nw);\n" +
                    "  sum += randomNoise(cc);\n" +
                    "  sum /= 9.;\n" +
                    " \n" +
                    "  return sum;\n" +
                    "}" +
                    "void main(void) {\n" +
                    "    vec2 position = gl_FragCoord.xy/uResolution.xx;\n" +
                    "    float tiles = 128.;\n" +
                    "    position = floor(position*tiles);\n" +
                    "    float n = smoothNoise(position);\n" +
                    "    gl_FragColor = vec4(vec3(n), 1.);\n" +
                    "}\n";

//            "[Vertex_Shader]\n" +
//                    "void main(void)\n" +
//                    "{\n" +
//                    "  gl_Position = ftransform();\n" +
//                    "  gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
//                    "}\n" +
//                    "[Pixel_Shader]\n" +
//                    "uniform sampler2D sceneTex; // 0\n" +
//                    " \n" +
//                    "uniform float rt_w; // render target width\n" +
//                    "uniform float rt_h; // render target height\n" +
//                    "uniform float vx_offset;\n" +
//                    " \n" +
//                    "float offset[3] = float[]( 0.0, 1.3846153846, 3.2307692308 );\n" +
//                    "float weight[3] = float[]( 0.2270270270, 0.3162162162, 0.0702702703 );\n" +
//                    " \n" +
//                    "void main() \n" +
//                    "{ \n" +
//                    "  vec3 tc = vec3(1.0, 0.0, 0.0);\n" +
//                    "  if (gl_TexCoord[0].x<(vx_offset-0.01))\n" +
//                    "  {\n" +
//                    "    vec2 uv = gl_TexCoord[0].xy;\n" +
//                    "    tc = texture2D(sceneTex, uv).rgb * weight[0];\n" +
//                    "    for (int i=1; i<3; i++) \n" +
//                    "    {\n" +
//                    "      tc += texture2D(sceneTex, uv + vec2(offset[i])/rt_w, 0.0).rgb \\\n" +
//                    "              * weight[i];\n" +
//                    "      tc += texture2D(sceneTex, uv - vec2(offset[i])/rt_w, 0.0).rgb \\\n" +
//                    "              * weight[i];\n" +
//                    "    }\n" +
//                    "  }\n" +
//                    "  else if (gl_TexCoord[0].x>=(vx_offset+0.01))\n" +
//                    "  {\n" +
//                    "    tc = texture2D(sceneTex, gl_TexCoord[0].xy).rgb;\n" +
//                    "  }\n" +
//                    "  gl_FragColor = vec4(tc, 1.0);\n" +
//                    "}\n";

//            "[Vertex_Shader]\n" +
//                    "void main(void)\n" +
//                    "{\n" +
//                    "  gl_Position = ftransform();\n" +
//                    "  gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
//                    "}\n" +
//                    "[Pixel_Shader]\n" +
//                    "uniform sampler2D sceneTex; // 0\n" +
//                    " \n" +
//                    "uniform float rt_w; // render target width\n" +
//                    "uniform float rt_h; // render target height\n" +
//                    "uniform float vx_offset;\n" +
//                    " \n" +
//                    "float offset[3] = float[]( 0.0, 1.3846153846, 3.2307692308 );\n" +
//                    "float weight[3] = float[]( 0.2270270270, 0.3162162162, 0.0702702703 );\n" +
//                    " \n" +
//                    "void main() \n" +
//                    "{ \n" +
//                    "  vec3 tc = vec3(1.0, 0.0, 0.0);\n" +
//                    "  if (gl_TexCoord[0].x<(vx_offset-0.01))\n" +
//                    "  {\n" +
//                    "    vec2 uv = gl_TexCoord[0].xy;\n" +
//                    "    tc = texture2D(sceneTex, uv).rgb * weight[0];\n" +
//                    "    for (int i=1; i<3; i++) \n" +
//                    "    {\n" +
//                    "      tc += texture2D(sceneTex, uv + vec2(0.0, offset[i])/rt_h).rgb \\\n" +
//                    "              * weight[i];\n" +
//                    "      tc += texture2D(sceneTex, uv - vec2(0.0, offset[i])/rt_h).rgb \\\n" +
//                    "             * weight[i];\n" +
//                    "    }\n" +
//                    "  }\n" +
//                    "  else if (gl_TexCoord[0].x>=(vx_offset+0.01))\n" +
//                    "  {\n" +
//                    "    tc = texture2D(sceneTex, gl_TexCoord[0].xy).rgb;\n" +
//                    "  }\n" +
//                    "  gl_FragColor = vec4(tc, 1.0);\n" +
//                    "}\n";

//            "uniform float sigma;     // The sigma value for the gaussian function: higher value means more blur\n" +
//                    "                         // A good value for 9x9 is around 3 to 5\n" +
//                    "                         // A good value for 7x7 is around 2.5 to 4\n" +
//                    "                         // A good value for 5x5 is around 2 to 3.5\n" +
//                    "                         // ... play around with this based on what you need :)\n" +
//                    "\n" +
//                    "uniform float blurSize;  // This should usually be equal to\n" +
//                    "                         // 1.0f / texture_pixel_width for a horizontal blur, and\n" +
//                    "                         // 1.0f / texture_pixel_height for a vertical blur.\n" +
//                    "\n" +
//                    "uniform sampler2D blurSampler;  // Texture that will be blurred by this shader\n" +
//                    "\n" +
//                    "const float pi = 3.14159265f;\n" +
//                    "\n" +
//                    "// The following are all mutually exclusive macros for various \n" +
//                    "// seperable blurs of varying kernel size\n" +
//                    "#if defined(VERTICAL_BLUR_9)\n" +
//                    "const float numBlurPixelsPerSide = 4.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(0.0f, 1.0f);\n" +
//                    "#elif defined(HORIZONTAL_BLUR_9)\n" +
//                    "const float numBlurPixelsPerSide = 4.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(1.0f, 0.0f);\n" +
//                    "#elif defined(VERTICAL_BLUR_7)\n" +
//                    "const float numBlurPixelsPerSide = 3.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(0.0f, 1.0f);\n" +
//                    "#elif defined(HORIZONTAL_BLUR_7)\n" +
//                    "const float numBlurPixelsPerSide = 3.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(1.0f, 0.0f);\n" +
//                    "#elif defined(VERTICAL_BLUR_5)\n" +
//                    "const float numBlurPixelsPerSide = 2.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(0.0f, 1.0f);\n" +
//                    "#elif defined(HORIZONTAL_BLUR_5)\n" +
//                    "const float numBlurPixelsPerSide = 2.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(1.0f, 0.0f);\n" +
//                    "#else\n" +
//                    "// This only exists to get this shader to compile when no macros are defined\n" +
//                    "const float numBlurPixelsPerSide = 0.0f;\n" +
//                    "const vec2  blurMultiplyVec      = vec2(0.0f, 0.0f);\n" +
//                    "#endif\n" +
//                    "\n" +
//                    "void main() {\n" +
//                    "\n" +
//                    "  // Incremental Gaussian Coefficent Calculation (See GPU Gems 3 pp. 877 - 889)\n" +
//                    "  vec3 incrementalGaussian;\n" +
//                    "  incrementalGaussian.x = 1.0f / (sqrt(2.0f * pi) * sigma);\n" +
//                    "  incrementalGaussian.y = exp(-0.5f / (sigma * sigma));\n" +
//                    "  incrementalGaussian.z = incrementalGaussian.y * incrementalGaussian.y;\n" +
//                    "\n" +
//                    "  vec4 avgValue = vec4(0.0f, 0.0f, 0.0f, 0.0f);\n" +
//                    "  float coefficientSum = 0.0f;\n" +
//                    "\n" +
//                    "  // Take the central sample first...\n" +
//                    "  avgValue += texture2D(blurSampler, gl_TexCoord[0].xy) * incrementalGaussian.x;\n" +
//                    "  coefficientSum += incrementalGaussian.x;\n" +
//                    "  incrementalGaussian.xy *= incrementalGaussian.yz;\n" +
//                    "\n" +
//                    "  // Go through the remaining 8 vertical samples (4 on each side of the center)\n" +
//                    "  for (float i = 1.0f; i <= numBlurPixelsPerSide; i++) { \n" +
//                    "    avgValue += texture2D(blurSampler, gl_TexCoord[0].xy - i * blurSize * \n" +
//                    "                          blurMultiplyVec) * incrementalGaussian.x;         \n" +
//                    "    avgValue += texture2D(blurSampler, gl_TexCoord[0].xy + i * blurSize * \n" +
//                    "                          blurMultiplyVec) * incrementalGaussian.x;         \n" +
//                    "    coefficientSum += 2 * incrementalGaussian.x;\n" +
//                    "    incrementalGaussian.xy *= incrementalGaussian.yz;\n" +
//                    "  }\n" +
//                    "\n" +
//                    "  gl_FragColor = avgValue / coefficientSum;\n" +
//                    "}\n";

//            "void main() {\n" +
//                    "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
//                    "    gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
//                    "}\n";

//            "#ifdef GL_ES\n" +
//                    "precision mediump float;\n" +
//                    "#endif\n" +
//                    "varying vec2 vTexCoordA;\n" +
//                    "varying vec2 vTexCoordB;\n" +
//                    "uniform sampler2D u_texture;\n" +
//                    "uniform float lerp;\n" +
//                    "void main() {\n" +
//                    "    //sample the two texture regions\n" +
//                    "    vec4 texColorA = texture2D(u_texture, vTexCoordA);\n" +
//                    "    vec4 texColorB = texture2D(u_texture, vTexCoordB);\n" +
//                    "    //lerp between them\n" +
//                    "    gl_FragColor = mix(texColorA, texColorB, lerp);\n" +
//                    "}\n";

//            "uniform float bias;\n" +
//                    "void main() {\n" +
//                    "    //sample from the texture using bias to influence LOD\n" +
//                    "    vec4 texColor = texture2D(u_texture, vTexCoord, bias);\n" +
//                    "    gl_FragColor = texColor * vColor;\n" +
//                    "}\n";

//            "precision mediump float;\n" +
//                    "uniform sampler2D s_texture;\n" +
//                    "varying vec2 v_texCoord;\n" +
//                    "varying vec2 v_blurTexCoords[14];\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "    gl_FragColor = vec4(0.0);\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 0])*0.0044299121055113265;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 1])*0.00895781211794;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 2])*0.0215963866053;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 3])*0.0443683338718;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 4])*0.0776744219933;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 5])*0.115876621105;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 6])*0.147308056121;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_texCoord         )*0.159576912161;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 7])*0.147308056121;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 8])*0.115876621105;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[ 9])*0.0776744219933;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[10])*0.0443683338718;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[11])*0.0215963866053;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[12])*0.00895781211794;\n" +
//                    "    gl_FragColor += texture2D(s_texture, v_blurTexCoords[13])*0.0044299121055113265;\n" +
//                    "}\n";

//            "attribute vec4 position;\n" +
//                    "attribute vec4 inputTextureCoordinate;\n" +
//                    "uniform float texelWidthOffset;\n" +
//                    "uniform float texelHeightOffset;\n" +
//                    "varying vec2 blurCoordinates[5];\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "gl_Position = position;\n" +
//                    "vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
//                    "blurCoordinates[0] = inputTextureCoordinate.xy;\n" +
//                    "blurCoordinates[1] = inputTextureCoordinate.xy + singleStepOffset * 1.407333;\n" +
//                    "blurCoordinates[2] = inputTextureCoordinate.xy - singleStepOffset * 1.407333;\n" +
//                    "blurCoordinates[3] = inputTextureCoordinate.xy + singleStepOffset * 3.294215;\n" +
//                    "blurCoordinates[4] = inputTextureCoordinate.xy - singleStepOffset * 3.294215;\n" +
//                    "}\n";

//            "uniform sampler2D inputImageTexture;\n" +
//                    "uniform highp float texelWidthOffset;\n" +
//                    "uniform highp float texelHeightOffset;\n" +
//                    "varying highp vec2 blurCoordinates[5];\n" +
//                    "void main()\n" +
//                    "{\n" +
//                    "lowp vec4 sum = vec4(0.0);\n" +
//                    "sum += texture2D(inputImageTexture, blurCoordinates[0]) * 0.204164;\n" +
//                    "sum += texture2D(inputImageTexture, blurCoordinates[1]) * 0.304005;\n" +
//                    "sum += texture2D(inputImageTexture, blurCoordinates[2]) * 0.304005;\n" +
//                    "sum += texture2D(inputImageTexture, blurCoordinates[3]) * 0.093913;\n" +
//                    "sum += texture2D(inputImageTexture, blurCoordinates[4]) * 0.093913;\n" +
//                    "gl_FragColor = sum;\n" +
//                    "}\n";


    // Fragment shader with a convolution filter.  The upper-left half will be drawn normally,
    // the lower-right half will have the filter applied, and a thin red line will be drawn
    // at the border.
    //
    // This is not optimized for performance.  Some things that might make this faster:
    // - Remove the conditionals.  They're used to present a half & half view with a red
    //   stripe across the middle, but that's only useful for a demo.
    // - Unroll the loop.  Ideally the compiler does this for you when it's beneficial.
    // - Bake the filter kernel into the shader, instead of passing it through a uniform
    //   array.  That, combined with loop unrolling, should reduce memory accesses.
    public static final int KERNEL_SIZE = 9;
    private static final String FRAGMENT_SHADER_EXT_FILT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "#define KERNEL_SIZE " + KERNEL_SIZE + "\n" +
            "precision highp float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform float uKernel[KERNEL_SIZE];\n" +
            "uniform vec2 uTexOffset[KERNEL_SIZE];\n" +
            "uniform float uColorAdjust;\n" +
            "void main() {\n" +
            "    int i = 0;\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    if (vTextureCoord.x < vTextureCoord.y - 0.005) {\n" +
            "        for (i = 0; i < KERNEL_SIZE; i++) {\n" +
            "            vec4 texc = texture2D(sTexture, vTextureCoord + uTexOffset[i]);\n" +
            "            sum += texc * uKernel[i];\n" +
            "        }\n" +
            "    sum += uColorAdjust;\n" +
            "    } else if (vTextureCoord.x > vTextureCoord.y + 0.005) {\n" +
            "        sum = texture2D(sTexture, vTextureCoord);\n" +
            "    } else {\n" +
            "        sum.r = 1.0;\n" +
            "    }\n" +
            "    gl_FragColor = sum;\n" +
            "}\n";

    private ProgramType mProgramType;

    // Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int muKernelLoc;
    private int muTexOffsetLoc;
    private int muColorAdjustLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    private int mTextureTarget;

    private float[] mKernel = new float[KERNEL_SIZE];
    private float[] mTexOffset;
    private float mColorAdjust;


    /**
     * Prepares the program in the current EGL context.
     */
    public Texture2dProgram(ProgramType programType) {
        mProgramType = programType;

        switch (programType) {
            case TEXTURE_2D:
                mTextureTarget = GLES20.GL_TEXTURE_2D;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
                break;
            case TEXTURE_EXT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
                break;
            case TEXTURE_EXT_BW:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_BW);
                break;
            case FRAGMENT_TEST:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_TEST);
                break;
            case TEXTURE_EXT_FILT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_FILT);
                break;
            default:
                throw new RuntimeException("Unhandled type " + programType);
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        Log.d(TAG, "Created program " + mProgramHandle + " (" + programType + ")");

        // get locations of attributes and uniforms

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel");
        if (muKernelLoc < 0) {
            // no kernel in this one
            muKernelLoc = -1;
            muTexOffsetLoc = -1;
            muColorAdjustLoc = -1;
        } else {
            // has kernel, must also have tex offset and color adj
            muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
            GlUtil.checkLocation(muTexOffsetLoc, "uTexOffset");
            muColorAdjustLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColorAdjust");
            GlUtil.checkLocation(muColorAdjustLoc, "uColorAdjust");

            // initialize default values
            setKernel(new float[] {0f, 0f, 0f,  0f, 1f, 0f,  0f, 0f, 0f}, 0f);
            setTexSize(256, 256);
        }
    }

    /**
     * Releases the program.
     * <p>
     * The appropriate EGL context must be current (i.e. the one that was used to create
     * the program).
     */
    public void release() {
        Log.d(TAG, "deleting program " + mProgramHandle);
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Returns the program type.
     */
    public ProgramType getProgramType() {
        return mProgramType;
    }

    /**
     * Creates a texture object suitable for use with this program.
     * <p>
     * On exit, the texture will be bound.
     */
    public int createTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
        GLES20.glBindTexture(mTextureTarget, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        return texId;
    }

    /**
     * Configures the convolution filter values.
     *
     * @param values Normalized filter values; must be KERNEL_SIZE elements.
     */
    public void setKernel(float[] values, float colorAdj) {
        if (values.length != KERNEL_SIZE) {
            throw new IllegalArgumentException("Kernel size is " + values.length +
                    " vs. " + KERNEL_SIZE);
        }
        System.arraycopy(values, 0, mKernel, 0, KERNEL_SIZE);
        mColorAdjust = colorAdj;
        //Log.d(TAG, "filt kernel: " + Arrays.toString(mKernel) + ", adj=" + colorAdj);
    }

    /**
     * Sets the size of the texture.  This is used to find adjacent texels when filtering.
     */
    public void setTexSize(int width, int height) {
        float rw = 1.0f / width;
        float rh = 1.0f / height;

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = new float[] {
            -rw, -rh,   0f, -rh,    rw, -rh,
            -rw, 0f,    0f, 0f,     rw, 0f,
            -rw, rh,    0f, rh,     rw, rh
        };
        //Log.d(TAG, "filt size: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
    }

    /**
     * Issues the draw call.  Does the full setup on every call.
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param vertexBuffer Buffer with vertex position data.
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the position data for each vertex (often
     *        vertexCount * sizeof(float)).
     * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
     *        for use with SurfaceTexture.)
     * @param texBuffer Buffer with vertex texture data.
     * @param texStride Width, in bytes, of the texture data for each vertex.
     */
    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
            int vertexCount, int coordsPerVertex, int vertexStride,
            float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
            GlUtil.checkGlError("glVertexAttribPointer");

        // Populate the convolution kernel, if present.
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0);
            GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0);
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust);
        }

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
