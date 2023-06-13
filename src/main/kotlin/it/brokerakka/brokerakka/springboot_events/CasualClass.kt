package it.brokerakka.brokerakka.springboot_events

import org.springframework.stereotype.Service

/**
 * This class is annotated with Service so it is instantiated directly by SpringBoot
 */

@Service
class CasualClass {

    init {
        println("INIT CASUAL CLASS")
    }
}