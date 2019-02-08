package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.*
import org.quartz.TriggerBuilder.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.JobDataMap



@PersistJobDataAfterExecution
@DisallowConcurrentExecution
class LisJobHandler : org.quartz.Job {

    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        System.err.println("Hello World!  MyJob is executing.")
    }

    fun LisDirectoryScanner(args :ArgDto) {
        val schedFact = StdSchedulerFactory()
        try {

            val sched = schedFact.scheduler
            val jobDataMap = JobDataMap()
            jobDataMap.put("user", args.smbUser)
            jobDataMap.put("pass", args.smbPass)
            jobDataMap.put("smb", args.smbUrl)

            val jobA = JobBuilder.newJob(LisDirectoryScannerJob::class.java).withIdentity("jobA", "group1")
                    .usingJobData(jobDataMap)
                    .build()

            var trigger = newTrigger()
                    .withIdentity("triggerA", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60)
                            .repeatForever())
                    .build()

            sched.scheduleJob(jobA, trigger)
            sched.start()

//            Thread.sleep(60L * 1000L)
  //          sched.shutdown(true)

        }catch (e :SchedulerException){e.printStackTrace()}

    }





}