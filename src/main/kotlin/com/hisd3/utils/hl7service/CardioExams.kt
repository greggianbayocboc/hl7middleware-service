package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.JobDataMap
import java.util.*
import org.eclipse.jetty.websocket.common.events.annotated.InvalidSignatureException.build
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.SchedulerFactory
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
import org.quartz.Job
import org.quartz.DisallowConcurrentExecution
import org.quartz.PersistJobDataAfterExecution


@PersistJobDataAfterExecution
@DisallowConcurrentExecution
class CardioExams : Job {
    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        System.err.println("---" + context.jobDetail.key
                + " executing.[" + Date() + "]")

        val map = context.jobDetail.jobDataMap

        var executeCount = 0
        if (map.containsKey(NUM_EXECUTIONS)) {
            executeCount = map.getInt(NUM_EXECUTIONS)
        }

        executeCount++
        map[NUM_EXECUTIONS] = executeCount

        var delay = 5000L
        if (map.containsKey(EXECUTION_DELAY)) {
            delay = map.getLong(EXECUTION_DELAY)
        }

        try {
            Thread.sleep(delay)
        } catch (ignore: Exception) {
        }

        System.err.println("  -" + context.jobDetail.key
                + " complete (" + executeCount + ").")
    }

    companion object {

        val NUM_EXECUTIONS = "NumExecutions"
        val EXECUTION_DELAY = "ExecutionDelay"
    }

    fun cardiosched() {
        val schedFact = StdSchedulerFactory()
        try {

            val sched = schedFact.scheduler

            val job = newJob(CardioExams::class.java)
                    .withIdentity("statefulJob1", "group1")
                    .usingJobData(CardioExams.EXECUTION_DELAY, 10000L)

                    .build()

            var trigger2 = TriggerBuilder.newTrigger()
                    .withIdentity("triggerA", "group2")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3)
                            .repeatForever())
                    .build()

            sched.scheduleJob(job, trigger2)
            sched.start()
        } catch (e: SchedulerException) {
            e.printStackTrace()
        }
    }
}

