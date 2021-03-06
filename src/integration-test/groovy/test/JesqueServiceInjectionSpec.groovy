package test

import grails.plugins.redis.RedisService
import grails.plugins.jesque.JesqueService
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import net.greghaines.jesque.meta.dao.FailureDAO
import net.greghaines.jesque.meta.dao.QueueInfoDAO
import spock.lang.Specification

@Integration
@Rollback
class JesqueServiceInjectionSpec extends Specification {

    JesqueService jesqueService
    QueueInfoDAO queueInfoDao
    FailureDAO failureDao
    RedisService redisService

    void setup() {
        redisService.flushDB()
    }

    void tearDown() {
        redisService.flushDB()
    }

    void "test autowirejob with redis service injection with worker"() {
        given:
        def queueName = 'redisAutoWireJob'
        def existingProcessedCount = queueInfoDao.processedCount
        def existingFailureCount = failureDao.count
        redisService.hello = ""
        redisService.worked = ""

        when:
        jesqueService.enqueue(queueName, RedisAutoWireJob.simpleName)
        jesqueService.withWorker(queueName, RedisAutoWireJob.simpleName, RedisAutoWireJob) {
            sleep(2000)
        }

        then:
        existingProcessedCount + 1 == queueInfoDao.processedCount
        existingFailureCount == failureDao.count
        redisService.hello == "world"
        redisService.worked == "true"
    }

    void "test autowirejob with redis service injection via config"() {
        given:
        def queueName = 'redisAutoWireJobQueueName'
        def existingProcessedCount = queueInfoDao.processedCount
        def existingFailureCount = failureDao.count
        redisService.hello = ""
        redisService.worked = ""

        when:
        jesqueService.enqueue(queueName, RedisAutoWireJob.simpleName)
        sleep(2000)

        then:
        existingProcessedCount + 1 == queueInfoDao.processedCount
        existingFailureCount == failureDao.count
        redisService.hello == "world"
        redisService.worked == "true"
    }
}
