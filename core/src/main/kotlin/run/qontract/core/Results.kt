package run.qontract.core

data class Results(val results: MutableList<Triple<Result, HttpRequest, HttpResponse?>> = mutableListOf()) {
    fun hasFailures(): Boolean = results.any { it.first is Result.Failure }
    fun hasSuccess(): Boolean = results.any { it.first is Result.Success }
    fun success(): Boolean = hasSuccess() && !hasFailures()

    val failureCount
        get(): Int = results.count { it.first is Result.Failure }

    val successCount
        get(): Int = results.count { it.first is Result.Success }

    fun add(result: Result, httpRequest: HttpRequest, httpResponse: HttpResponse?) {
        results.add(Triple(result, httpRequest, httpResponse))
    }

    fun generateErrorHttpResponse() =
            HttpResponse(400, generateErrorResponseBody(), java.util.HashMap())

    private fun generateErrorResponseBody() =
            generateFeedback()

    private fun generateFeedback(): String {
        return results.joinToString("\n\n") { (result, request, response) ->
            resultReport(result, request, response)
        }
    }

    fun report() = generateErrorResponseBody()
}

fun resultReport(result: Result, request: HttpRequest, response: HttpResponse?): String {
    val firstLine = """In ${result.scenario}"""

    val report = if (result is Result.Failure) {
        result.report().let { (breadCrumbs, errorMessages) ->
            val breadCrumbString = breadCrumbs.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(".")
            val errorMessagesString = errorMessages.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(".")
            ">> $breadCrumbString\n\n$errorMessagesString".trim()
        }
    } else ""

    val firstPart = "$firstLine\n$report".trim()

    val requestString = "Request: ${request.toLogString()}"
    val responseString = response?.let { "Response: ${it.toLogString()}" } ?: ""

    return "$firstPart\n\n$requestString\n$responseString".trim()
}
