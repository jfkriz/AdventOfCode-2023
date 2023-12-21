package day20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.lcm

@DisplayName("Day 20 - Pulse Propagation")
@TestMethodOrder(OrderAnnotation::class)
class PulsePropagationTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 32000000`() {
        assertEquals(32000000, sampleSolver.solvePartOne())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input 2 should return 11687500`() {
        val testInput2 = """
            broadcaster -> a
            %a -> inv, con
            &inv -> b
            %b -> con
            &con -> output
        """.trimIndent().split("\n")
        assertEquals(11687500, Solver(testInput2).solvePartOne())
    }

    @Test
    @Order(3)
    @Disabled("No sample input for solving part 2 - good luck!!")
    fun `Part 2 Sample Input - no sample for part 2`() {
        assertEquals(90210, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 666795063`() {
        assertEquals(666795063L, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 253302889093151`() {
        assertEquals(253302889093151L, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val inputModules = data.map { Module.fromInputLine(it) }.associateBy { it.name }

    fun solvePartOne(): Long {
        val pulses = mutableMapOf(false to 0, true to 0L)
        val modules = inputModules.ensureOutputsExist().setInputs()
        (1..1000).forEach { _ ->
            val stack = ArrayDeque<Message>().apply {
                this.add(Message("broadcaster", "button", false))
            }
            while (stack.isNotEmpty()) {
                send(stack.removeFirst(), modules, pulses, stack)
            }
        }

        return pulses[false]!! * pulses[true]!!
    }

    fun solvePartTwo(): Long {
        val pulses = mutableMapOf(false to 0, true to 0L)
        var modules = inputModules.ensureOutputsExist().setInputs()
        val rx = modules.getValue("rx")
        val rxSender = rx.inputs[0] // Assuming that there is only one module that sends directly to "rx"
        val rxSenderSenders = modules.getValue(rxSender).inputs // There are only 4 modules that send to the above rxSender in my input
        val counters = mutableListOf<Long>()

        rxSenderSenders.forEach { sender ->
            modules = inputModules.ensureOutputsExist().setInputs()
            var counter = 0L
            var done = false
            val stack = ArrayDeque<Message>()
            while (!done) {
                stack.add(Message("broadcaster", "button", false))
                while (stack.isNotEmpty()) {
                    val message = stack.removeFirst()
                    send(message, modules, pulses, stack)
                    if (message.target == rxSender && message.pulseType && message.sender == sender) {
                        done = true
                    }
                }
                counter++
            }
            counters.add(counter)
        }

        return counters.lcm()
    }

    private fun send(message: Message, modules: Map<String, Module>, pulses: MutableMap<Boolean, Long>, stack: ArrayDeque<Message>) {
        pulses[message.pulseType] = (pulses[message.pulseType] ?: 0) + 1

        val mod = modules.getValue(message.target)

        if (mod.type == ModuleType.BROADCAST) {
            mod.outputs.forEach { stack.add(Message(it, mod.name, message.pulseType)) }
            return
        }

        if (mod.type == ModuleType.FLIP_FLOP) {
            // "High" pulses are ignored by flip-flop
            if (message.pulseType) {
                return
            }

            mod.outputs.forEach { out ->
                stack.add(Message(out, mod.name, !mod.state))
            }
            mod.state = !mod.state
            return
        }

        if (mod.type == ModuleType.CONJUNCTION) {
            mod.inputStates[message.sender] = message.pulseType
            if (mod.inputStates.values.all { it }) {
                mod.outputs.forEach { stack.add(Message(it, mod.name, false)) }
            } else {
                mod.outputs.forEach { stack.add(Message(it, mod.name, true)) }
            }
            return
        }

        return
    }

    private fun Map<String, Module>.ensureOutputsExist(): Map<String, Module> {
        val outputsToAdd = this.values.map { it.outputs }.flatten().distinct().filter { !this.containsKey(it) }
        val outputs = outputsToAdd.map { name ->
            Module(ModuleType.NONE, name, emptyList())
        }.associateBy { it.name }

        return this.map { it.key to it.value.copy() }.toMap() + outputs
    }

    private fun Map<String, Module>.setInputs(): Map<String, Module> {
        this.forEach { (name, module) ->
            module.outputs.forEach { output ->
                this.getValue(output).addInput(name)
            }
        }
        return this
    }
}

data class Message(val target: String, val sender: String, val pulseType: Boolean)

enum class ModuleType {
    NONE,
    BROADCAST,
    FLIP_FLOP,
    CONJUNCTION;

    companion object {
        fun fromName(name: String): ModuleType =
            if (name == "broadcaster") {
                BROADCAST
            } else {
                when (name[0]) {
                    '%' -> FLIP_FLOP
                    '&' -> CONJUNCTION
                    else -> NONE
                }
            }
    }
}

data class Module(val type: ModuleType, val name: String, val outputs: List<String>, val inputs: MutableList<String> = mutableListOf()) {
    private val stateList: MutableMap<String, Boolean> = if (type == ModuleType.CONJUNCTION) {
        inputs.associateWith { false }.toMutableMap()
    } else {
        mutableMapOf(name to false)
    }

    var state: Boolean
        get() = if (type == ModuleType.CONJUNCTION) {
            false
        } else {
            stateList.getValue(name)
        }
        set(value) {
            if (type != ModuleType.CONJUNCTION) {
                stateList[name] = value
            }
        }

    val inputStates: MutableMap<String, Boolean>
        get() = stateList

    fun addInput(inputName: String) {
        if (!inputs.contains(inputName)) {
            inputs.add(inputName)
            if (type == ModuleType.CONJUNCTION) {
                stateList[inputName] = false
            }
        }
    }

    companion object {
        fun fromInputLine(line: String): Module {
            val parts = line.split(" -> ".toRegex())
            val type = ModuleType.fromName(parts[0])
            val name = parts[0].replace("^[%&]".toRegex(), "")
            val outputs = parts[1].split(", ".toRegex())
            return Module(type, name, outputs)
        }
    }
}
