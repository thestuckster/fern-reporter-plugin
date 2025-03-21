package com.guidewire

import com.guidewire.models.TestRun
import kotlinx.serialization.encodeToString
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

    val endpoint = URI("$fernUrl/api/testrun").normalize()

    if (verbose) {
      println("Sending POST request to $endpoint...")
    }

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
      .uri(endpoint)
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(payload))
      .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() >= 300) {
      throw RuntimeException("Unexpected response code: ${response.statusCode()}")
    }
  }
}