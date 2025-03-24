package com.guidewire

import com.guidewire.models.TestRun
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun sendTestRun(testRun: TestRun, fernUrl: String, verbose: Boolean): Result<Unit> {
  return runCatching {
    val json = Json {
      prettyPrint = true
      encodeDefaults = true
    }

    val payload = json.encodeToString(testRun)
    val endpoint = "$fernUrl/api/testrun/"

    if (verbose) {
      println("Sending POST request to $endpoint...")
    }

    val response = postTestRun(endpoint, fernUrl, payload)
    if (response.statusCode()>= 300) {
      throw RuntimeException("Unexpected response code: ${response.statusCode()}")
    }
  }
}

private fun postTestRun(endpoint: String, fernUrl: String, payload: String): HttpResponse<String> {
  val client = HttpClient.newBuilder().build()
  val request = HttpRequest.newBuilder()
    .uri(URI(endpoint).normalize())
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(payload))
    .build()

  var response = client.send(request, HttpResponse.BodyHandlers.ofString())

  if(response.statusCode() == 307) {
    val locationHeader = response.headers().firstValue("location")
    response = postTestRun(fernUrl + locationHeader, fernUrl, payload)
  }
  return response
}