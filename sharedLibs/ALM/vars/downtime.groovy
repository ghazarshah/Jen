#!/usr/bin/env groovy


import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.time.temporal.ChronoUnit;

def getDowntimeSchedule(String jenkinsInstance, String prodQa) {
    def request = libraryResource 'org/alm/maintenance.yaml'
    def yaml = new Yaml()
    def data = yaml.load(request)
    def schedule = data.Variables.find { it.containsKey(jenkinsInstance) }?.get(jenkinsInstance)?.get(prodQa)
    if (schedule) {
        def downtimeList = []
        for (def downtime : schedule) {
            def startTime = LocalDateTime.parse(downtime.start, DateTimeFormatter.ofPattern('dd/MM/yyyy, H:mm:ss'))
            def endTime = LocalDateTime.parse(downtime.end, DateTimeFormatter.ofPattern('dd/MM/yyyy, H:mm:ss'))
            downtimeList << [startTime, endTime]
        }
        return downtimeList
    } else {
        return []
    }
}



def getNextDowntimeDelay(String jenkinsInstance, String prodQa) {
    def downtimeSchedule = getDowntimeSchedule(jenkinsInstance, prodQa)
    def now = LocalDateTime.now()
    def nextDowntime = downtimeSchedule.find { it[0].isAfter(now) }
    if (nextDowntime) {
        def duration = Duration.between(now, nextDowntime[0])
        def days = duration.toDays()
        def hours = duration.toHours() % 24
        def minutes = duration.toMinutes() % 60
        println "Next downtime is ${nextDowntime[0]}"
        println "Next downtime in ${days} days, ${hours} hours, and ${minutes} minutes."
        return duration.getSeconds()
    } else {
        println "No downtime scheduled."
        return null
    }
}

def getNextDowntimeEndDelay(String jenkinsInstance, String prodQa) {
    def downtimeSchedule = getDowntimeSchedule(jenkinsInstance, prodQa)
    def now = LocalDateTime.now()
    def nextDowntime = downtimeSchedule.find { it[0].isAfter(now) }
    if (nextDowntime) {
        def duration = Duration.between(now, nextDowntime[1])
        def days = duration.toDays()
        def hours = duration.toHours() % 24
        def minutes = duration.toMinutes() % 60
        println "Next downtime ends at ${nextDowntime[1]}"
        println "Next downtime ends in ${days} days, ${hours} hours, and ${minutes} minutes."
        return duration.getSeconds()
    } else {
        println "No downtime scheduled."
        return null
    }
}



