package com.cozmicgames.common.utils

import com.badlogic.gdx.utils.Disposable
import java.util.ArrayDeque
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write
import kotlin.system.exitProcess

class TaskManager : Updatable, Disposable {
    private class ScheduledTask(val time: Float, val isRepeating: Boolean, val isAsync: Boolean, val block: () -> Unit) {
        var timer = 0.0f
    }

    private val tasks = ArrayDeque<Task>()
    private val workingTasks = ArrayDeque<Task>()
    private val executor = Executors.newCachedThreadPool()
    private val scheduledTasks = arrayListOf<ScheduledTask>()
    private val workingScheduledTasks = arrayListOf<ScheduledTask>()
    private val scheduledTasksLock = ReentrantReadWriteLock()

    var balance = 0

    fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle {
        var isFinished = false

        if (dependencies.isEmpty()) {
            tasks += {
                task()
                isFinished = true
            }
        } else {
            tasks += {
                while (true) {
                    var dependenciesFinished = true

                    for (dependency in dependencies)
                        if (!dependency()) {
                            dependenciesFinished = false
                            break
                        }

                    if (dependenciesFinished)
                        break

                    processTask()
                }

                task()
                isFinished = true
            }
        }

        return { isFinished }
    }

    fun submitAsync(task: Task, vararg dependencies: TaskHandle): TaskHandle {
        var isFinished = false

        if (dependencies.isEmpty()) {
            executor.submit {
                balance++
                task()
                isFinished = true
                balance--
            }
        } else {
            executor.submit {
                balance++
                while (true) {
                    var dependenciesFinished = true

                    for (dependency in dependencies)
                        if (!dependency()) {
                            dependenciesFinished = false
                            break
                        }

                    if (dependenciesFinished)
                        break
                }

                task()
                isFinished = true
                balance--
            }
        }

        return { isFinished }
    }

    fun processTask(): Boolean {
        val task = tasks.poll() ?: return false
        task()
        return true
    }

    fun schedule(time: Float, isRepeating: Boolean = false, block: () -> Unit) = scheduledTasksLock.write {
        scheduledTasks += ScheduledTask(time, isRepeating, false, block)
    }

    fun scheduleAsync(time: Float, isRepeating: Boolean = false, block: () -> Unit) = scheduledTasksLock.write {
        scheduledTasks += ScheduledTask(time, isRepeating, true, block)
    }

    override fun update(delta: Float) {
        workingTasks.clear()
        workingTasks.addAll(tasks)
        tasks.clear()
        while (workingTasks.isNotEmpty())
            workingTasks.poll()()

        workingScheduledTasks.clear()

        scheduledTasksLock.read {
            workingScheduledTasks.addAll(scheduledTasks)
        }

        workingScheduledTasks.forEach {
            it.timer += delta
            if (it.timer >= it.time) {
                if (it.isAsync)
                    submitAsync(it.block)
                else
                    it.block()
                if (it.isRepeating)
                    it.timer -= it.time
                else
                    scheduledTasks -= it
            }
        }
    }

    override fun dispose() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow()

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    thread {
                        Thread.sleep(10000)
                        exitProcess(1504)
                    }
                }
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}

typealias Task = () -> Unit

typealias TaskHandle = () -> Boolean
