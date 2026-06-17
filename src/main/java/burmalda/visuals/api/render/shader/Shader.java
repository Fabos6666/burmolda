package burmalda.visuals.api.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader {
    private final int programId;
    private final Map<String, Integer> uniformCache = new HashMap<>();

    protected Shader(String folder, String shaderName) {
        int program = glCreateProgram();
        int vertexShader = 0;
        int fragmentShader = 0;

        try {
            String basePath = "/assets/untitled/shaders/core/" + folder + "/";
            String vertexSource = readResource(basePath + shaderName + ".vsh");
            String fragmentSource = readResource(basePath + shaderName + ".fsh");

            vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
            fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

            glAttachShader(program, vertexShader);
            glAttachShader(program, fragmentShader);
            glLinkProgram(program);

            if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
                throw new IllegalStateException(glGetProgramInfoLog(program));
            }
        } catch (Exception exception) {
            if (vertexShader != 0) {
                glDeleteShader(vertexShader);
            }
            if (fragmentShader != 0) {
                glDeleteShader(fragmentShader);
            }
            glDeleteProgram(program);
            throw new IllegalStateException("Failed to create shader " + shaderName, exception);
        }

        if (vertexShader != 0) {
            glDetachShader(program, vertexShader);
            glDeleteShader(vertexShader);
        }
        if (fragmentShader != 0) {
            glDetachShader(program, fragmentShader);
            glDeleteShader(fragmentShader);
        }

        this.programId = program;
    }

    public final void bind() {
        RenderSystem.assertOnRenderThread();
        glUseProgram(programId);
    }

    public final void unbind() {
        RenderSystem.assertOnRenderThread();
        glUseProgram(0);
    }

    public final void delete() {
        RenderSystem.assertOnRenderThread();
        glDeleteProgram(programId);
    }

    protected final void setUniform1i(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    protected final void setUniform1f(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }

    protected final void setUniform2f(String name, float x, float y) {
        glUniform2f(getUniformLocation(name), x, y);
    }

    protected final void setUniform3f(String name, float x, float y, float z) {
        glUniform3f(getUniformLocation(name), x, y, z);
    }

    protected final void setUniformMatrix4f(String name, boolean transpose, org.joml.Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(getUniformLocation(name), transpose, buffer);
        }
    }

    protected final void setUniformBool(String name, boolean value) {
        setUniform1i(name, value ? 1 : 0);
    }

    private int getUniformLocation(String name) {
        return uniformCache.computeIfAbsent(name, key -> glGetUniformLocation(programId, key));
    }

    private static int compileShader(String source, int type) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new IllegalStateException("Unable to create shader type " + type);
        }

        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException(glGetShaderInfoLog(shader, 1024));
        }

        return shader;
    }

    private static String readResource(String path) throws Exception {
        try (InputStream stream = Shader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new Exception("Missing shader resource: " + path);
            }
            ByteBuffer buffer = ByteBuffer.wrap(stream.readAllBytes());
            return StandardCharsets.UTF_8.decode(buffer).toString();
        }
    }
}
