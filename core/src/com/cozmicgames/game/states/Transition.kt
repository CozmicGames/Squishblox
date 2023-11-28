package com.cozmicgames.game.states

import com.badlogic.gdx.graphics.glutils.ShaderProgram

sealed class Transition(val uniforms: String, val source: String) {
    abstract fun setUniforms(shaderProgram: ShaderProgram)
}

class LinearTransition(val direction: Direction): Transition("""
    uniform vec2 u_direction;
""".trimIndent(), """
    vec4 effect() {
        vec2 uv = v_texcoord + u_progress * sign(u_direction);
        vec2 f = fract(uv);
        return mix(getToColor(f), getFromColor(f), step(0.0, uv.y) * step(uv.y, 1.0) * step(0.0, uv.x) * step(uv.x, 1.0));
    }
""".trimIndent()) {
    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    override fun setUniforms(shaderProgram: ShaderProgram) {
        when (direction) {
            Direction.UP -> shaderProgram.setUniformf("u_direction", 0.0f, 1.0f)
            Direction.DOWN -> shaderProgram.setUniformf("u_direction", 0.0f, -1.0f)
            Direction.LEFT -> shaderProgram.setUniformf("u_direction", -1.0f, 0.0f)
            Direction.RIGHT -> shaderProgram.setUniformf("u_direction", 1.0f, 0.0f)
        }
    }
}

class GlitchTransition: Transition("""
    uniform vec2 u_direction;
""".trimIndent(), """
    vec4 effect() {
        vec2 block = floor(v_texcoord.xy / vec2(16.0));
        vec2 uv_noise = block / vec2(64.0);
        uv_noise += floor(vec2(u_progress) * vec2(1200.0, 3500.0)) / vec2(64.0);
        vec2 dist = u_progress > 0.0 ? (fract(uv_noise) - 0.5) * 0.3 * (1.0 - u_progress) : vec2(0.0);
        vec2 red = v_texcoord + dist * 0.2;
        vec2 green = v_texcoord + dist * 0.3;
        vec2 blue = v_texcoord + dist * 0.5;
        
        return vec4(mix(getFromColor(red), getToColor(red), u_progress).r,mix(getFromColor(green), getToColor(green), u_progress).g,mix(getFromColor(blue), getToColor(blue), u_progress).b, 1.0);
    }
""".trimIndent()) {

    override fun setUniforms(shaderProgram: ShaderProgram) {
    }
}

class CrossFadeTransition(val strength: Float = 0.3f): Transition("""
    uniform float u_strength;
""".trimIndent(), """
    // Modified from https://gist.github.com/rectalogic/b86b90161503a0023231
    const float PI = 3.141592653589793;
            
    float Linear_ease(float begin, float change, float duration, float time) {
        return change * time / duration + begin;
    }

    float Exponential_easeInOut(float begin, float change, float duration, float time) {
        if (time == 0.0)
            return begin;
        else if (time == duration)
            return begin + change;
        time = time / (duration / 2.0);
        if (time < 1.0)
            return change / 2.0 * pow(2.0, 10.0 * (time - 1.0)) + begin;
        return change / 2.0 * (-pow(2.0, -10.0 * (time - 1.0)) + 2.0) + begin;
    }

    float Sinusoidal_easeInOut(float begin, float change, float duration, float time) {
        return -change / 2.0 * (cos(PI * time / duration) - 1.0) + begin;
    }

    float rand (vec2 co) {
      return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
    }

    vec3 crossFade(vec2 uv, float dissolve) {
        return mix(getFromColor(uv).rgb, getToColor(uv).rgb, dissolve);
    }
    
    vec4 effect() {
        vec2 center = vec2(Linear_ease(0.25, 0.5, 1.0, u_progress), 0.5);
        float dissolve = Exponential_easeInOut(0.0, 1.0, 1.0, u_progress);
        float strength = Sinusoidal_easeInOut(0.0, u_strength, 0.5, u_progress);

        vec3 color = vec3(0.0);
        float total = 0.0;
        vec2 toCenter = center - v_texcoord;
    
        float offset = rand(v_texcoord);
    
        for (float t = 0.0; t <= 10.0; t++) {
            float percent = (t + offset) / 10.0;
            float weight = 4.0 * (percent - percent * percent);
            color += crossFade(v_texcoord + toCenter * percent * strength, dissolve) * weight;
            total += weight;
        }
        return vec4(color / total, 1.0);
    }
""".trimIndent()) {

    override fun setUniforms(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformf("u_strength", strength)
    }
}

class CircleTransition: Transition("""
""".trimIndent(), """
    vec4 effect() {
        float distance = length(v_texcoord - vec2(0.5, 0.5));
        float radius = sqrt(8.0) * abs(u_progress - 0.5);
  
        if (distance > radius)
            return vec4(0.0, 0.0, 0.0, 1.0);
        else {
            if (u_progress < 0.5) 
                return getFromColor(v_texcoord);
            else 
                return getToColor(v_texcoord);
        }
    }
""".trimIndent()) {
    override fun setUniforms(shaderProgram: ShaderProgram) {
    }
}
