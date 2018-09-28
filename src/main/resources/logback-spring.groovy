scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %highlight(%-5level) | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

logger("org.ozwillo.dcimporter", DEBUG)
logger("reactor.ipc.netty.channel.ChannelOperationsHandler", DEBUG)
logger("org.springframework.web.client", DEBUG)
//logger("reactor.ipc.netty", DEBUG)

root(WARN, ["CONSOLE"])
