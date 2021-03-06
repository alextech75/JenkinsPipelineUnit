package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.MethodSignature.method

import com.lesfurets.jenkins.unit.BasePipelineTest

abstract class DeclarativePipelineTest extends BasePipelineTest {

    def pipelineInterceptor = { Closure closure ->
        DeclarativePipeline.createComponent(DeclarativePipeline, closure).execute(delegate)
    }

    def paramInterceptor = { Map desc ->
        addParam(desc.name, desc.defaultValue, false)
    }

    @Override
    void setUp() throws Exception {
        super.setUp()

        /**
         * Job params - may need to override in specific tests
         */
        binding.setVariable('params', [:])
        binding.setVariable('credentials', [:])

        helper.registerAllowedMethod(method("pipeline", Closure), pipelineInterceptor)

        helper.registerAllowedMethod('pollSCM', [String.class], null)
        helper.registerAllowedMethod('cron', [String.class], null)
        helper.registerAllowedMethod('timestamps', [], null)

        helper.registerAllowedMethod('skipDefaultCheckout', [], null)

        helper.registerAllowedMethod('script', [Closure.class], null)

        helper.registerAllowedMethod('timeout', [Integer.class, Closure.class], null)

        helper.registerAllowedMethod('waitUntil', [Closure.class], null)
        helper.registerAllowedMethod('writeFile', [Map.class], null)
        helper.registerAllowedMethod('build', [Map.class], null)
        helper.registerAllowedMethod('tool', [Map.class], { t -> "${t.name}_HOME" })

        helper.registerAllowedMethod('withCredentials', [Map.class, Closure.class], null)
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], null)
        helper.registerAllowedMethod('usernamePassword', [Map.class], { creds -> return creds })

        helper.registerAllowedMethod('deleteDir', [], null)
        helper.registerAllowedMethod('pwd', [], { 'workspaceDirMocked' })

        helper.registerAllowedMethod('stash', [Map.class], null)
        helper.registerAllowedMethod('unstash', [Map.class], null)

        helper.registerAllowedMethod('checkout', [Closure.class], null)

        helper.registerAllowedMethod('string', [Map.class], paramInterceptor)
        helper.registerAllowedMethod('booleanParam', [Map.class], paramInterceptor)

        helper.registerAllowedMethod('withEnv', [List.class, Closure.class], { List list, Closure c ->

            list.each {
                //def env = helper.get
                def item = it.split('=')
                assert item.size() == 2, "withEnv list does not look right: ${list.toString()}"
                addEnvVar(item[0], item[1])
                c.delegate = binding
                c.call()
            }
        })

        helper.registerAllowedMethod('credentials', [String], { String credName ->
            return binding.getVariable('credentials')[credName]
        })
    }
}
