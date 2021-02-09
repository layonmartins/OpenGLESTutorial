package com.layonf.opengltutorial

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

    private lateinit var glView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Create a GLSurfaceView instance and set it
        //as the ContentView for this Activity
        glView = MyGLSurfaceView(this)
        setContentView(glView)
    }

    class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
        private val renderer: MyGLRenderer

        private var previousX: Float = 0f
        private var previousY: Float = 0f

        override fun onTouchEvent(e: MotionEvent): Boolean {
            // MotionEvent reports input details from the touch screen
            // and other input controls. In this case, you are only
            // interested in events where the touch position changed.

            val x: Float = e.x
            val y: Float = e.y

            when (e.action) {
                MotionEvent.ACTION_MOVE -> {

                    var dx: Float = x - previousX
                    var dy: Float = y - previousY

                    // reverse direction of rotation above the mid-line
                    if (y > height / 2) {
                        dx *= -1
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < width / 2) {
                        dy *= -1
                    }

                    renderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR
                    requestRender()
                }
            }

            previousX = x
            previousY = y
            return true
        }

        init {
            //Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2)
            renderer = MyGLRenderer()
            //Set the Renderer for drawing on the GLSurfaceView
            setRenderer(renderer)

            // Render the view only when there is a change in the drawing data.
            // To allow the triangle to rotate automatically, this line is commented out:
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }

        companion object {
            private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
        }
    }

    class MyGLRenderer : GLSurfaceView.Renderer {

        //It is needed initialize the draw forms on onSurfaceCreated()
        private lateinit var mTriangle: Triangle
        private lateinit var mSquare: Square

        @Volatile
        var angle: Float = 0f

        // vPMatrix is an abbreviation for "Model View Projection Matrix"
        private val vPMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)

        private val rotationMatrix = FloatArray(16)

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the background frame color
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

            //initialize a triangle ans square
            mTriangle = Triangle()
            mSquare = Square()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0,0, width, height)

            //define a projection
            val ratio: Float = width.toFloat() / height.toFloat()

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        }

        override fun onDrawFrame(gl: GL10?) {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            val scratch = FloatArray(16)

            // Create a rotation for the triangle
            // long time = SystemClock.uptimeMillis() % 4000L;
            // float angle = 0.090f * ((int) time);
            Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

            //define a camera view
            // Set the camera position (View matrix)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

            // Calculate the projection and view transformation
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

            // Combine the rotation matrix with the projection and camera view
            // Note that the vPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

            // Draw shape
            mTriangle.draw(scratch)
        }

        //useful method to compile OpenGL Shading Language(GLSL)
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                //add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}