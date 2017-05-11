scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %highlight(%-5level) | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}


logger("org.oasis_eu.spring.kernel.security", DEBUG)
logger("org.ozwillo.dcimporter.controller", INFO)

root(WARN, ["CONSOLE"])