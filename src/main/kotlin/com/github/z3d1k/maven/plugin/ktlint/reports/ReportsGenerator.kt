package com.github.z3d1k.maven.plugin.ktlint.reports

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import java.util.ServiceLoader
import org.apache.maven.plugin.logging.Log

class ReportsGenerator(
    log: Log,
    reporterParameters: List<ReporterParameters> = emptyList(),
    reporterProviders: Iterable<ReporterProvider> = ServiceLoader.load(ReporterProvider::class.java)
) : Reporter {
    private val reporter: Reporter

    init {
        val reporterProvidersMap = reporterProviders.associateBy { it.id }
        val reportersList =
            reporterParameters.map { (name, printStream, parameters) ->
                reporterProvidersMap[name]?.get(printStream, parameters)
                    ?: throw IllegalArgumentException(
                        "Unable to initialize reporter with $name: unknown reporter name"
                    )
            }
        reporter = Reporter.from(*reportersList.toTypedArray(), MavenReporter(log))
    }

    fun generateReports(lintResults: Map<String, List<LintError>>) {
        reporter.beforeAll()
        lintResults.forEach { fileName, lintErrors ->
            reporter.before(fileName)
            lintErrors.map {
                reporter.onLintError(fileName, it, false)
            }
            reporter.after(fileName)
        }
        reporter.afterAll()
    }

    override fun after(file: String) {
        reporter.after(file)
    }

    override fun afterAll() {
        reporter.afterAll()
    }

    override fun before(file: String) {
        reporter.before(file)
    }

    override fun beforeAll() {
        reporter.beforeAll()
    }

    override fun onLintError(file: String, err: LintError, corrected: Boolean) {
        reporter.onLintError(file, err, corrected)
    }
}
