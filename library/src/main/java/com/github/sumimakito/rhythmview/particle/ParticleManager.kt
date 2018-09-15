package com.github.sumimakito.rhythmview.particle

import java.util.*

class ParticleManager {
    val particles = LinkedList<Particle>()
    var capacity = -1

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