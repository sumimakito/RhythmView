package com.github.sumimakito.rhythmview.particle

import java.util.*

/**
 * A manager for auto managing particles in the particle system.
 */
class ParticleManager {
    val particles = LinkedList<Particle>()
    /**
     * Capacity for the particle system.
     *
     * When the total number of particles exist in the system exceeds this value, earlier added
     * particles will be recycled first to ensure the performance.
     *
     * Set to a value less than zero to eliminate the limitation. (not recommended)
     */
    var capacity = 200

    fun add(particle: Particle) {
        particles.addLast(particle)
        internalCleanUp()
    }

    fun remove(particle: Particle): Boolean {
        val returnValue = particles.remove(particle)
        internalCleanUp()
        return returnValue
    }

    fun clear() {
        particles.clear()
    }

    /**
     * Remember to call `nextTick()` after rendering each frame.
     */
    fun nextTick() {
        internalCleanUp()
        val recyclable = ArrayList<Particle>()
        for (particle in particles) {
            particle.nextTick()
            if (particle.recyclable) {
                recyclable.add(particle)
            }
        }
        for (particle in recyclable) {
            particles.remove(particle)
        }
    }

    private fun internalCleanUp() {
        if (capacity >= 0) {
            while (particles.size > capacity) {
                particles.removeFirst()
            }
        }
    }
}